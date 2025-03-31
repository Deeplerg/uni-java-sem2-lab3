#!/bin/bash

set -o errexit
set -o nounset

user=$(whoami)
if [ "$user" != "root" ]; then
    echo "User must be root!"
	exit 1
fi

if [ ! -f *.war ]; then
    echo "No .war file found!"
	exit 1
fi

# first file, full path
war_file_path=$(readlink -f $(ls -1 *.war))

apt update
apt install -y openjdk-21-jdk nginx

# download and extract
temp_dir=$(mktemp -d)
cd $temp_dir
mkdir -p /opt/tomcat/bin
wget -O tomcat.tgz https://dlcdn.apache.org/tomcat/tomcat-11/v11.0.5/bin/apache-tomcat-11.0.5.tar.gz
tar xzvf tomcat.tgz -C /opt/tomcat --strip-components=1
chmod -R +x /opt/tomcat/bin
cd /opt/tomcat
rm -rf $temp_dir

# tomcat user
useradd -m -d /opt/tomcat -U -s /bin/false tomcat
chown -R tomcat:tomcat /opt/tomcat/

cat <<EOF > conf/tomcat-users.xml
<?xml version="1.0" encoding="UTF-8"?>
<tomcat-users>
  <role rolename="manager-gui"/>
  <user username="admin" password="admin" roles="manager-gui,manager-script"/>
</tomcat-users>
EOF

# java paths -> first one -> third column -> remove spaces
java_path=$(update-java-alternatives -l | head -n 1 | awk -F '      ' '{ print $3 }' - | tr -d ' ')

cat <<EOF >/etc/systemd/system/tomcat.service
[Unit]
Description=Tomcat
After=network.target

[Service]
Type=forking

User=tomcat
Group=tomcat

Environment="JAVA_HOME=$java_path"
Environment="JAVA_OPTS=-Djava.security.egd=file:///dev/urandom"
Environment="CATALINA_BASE=/opt/tomcat"
Environment="CATALINA_HOME=/opt/tomcat"
Environment="CATALINA_PID=/opt/tomcat/temp/tomcat.pid"
Environment="CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC"

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable tomcat
systemctl start tomcat

echo "Waiting for Tomcat to start..."
sleep 5 # wait for tomcat to start - crude but works
rm -rf /opt/tomcat/webapps/ROOT
curl \
	-u admin:admin \
	-T $war_file_path \
	"http://localhost:8080/manager/text/deploy?path=/&update=true"
mkdir -p /opt/tomcat/webapps/ROOT/fileroot

cat <<EOF >/etc/nginx/sites-enabled/default
server {
        listen 80;
        location / {
                proxy_pass http://localhost:8080;
        }
        location /tomcat {
                proxy_pass http://localhost:8080/;
        }
}
EOF
nginx -s reload