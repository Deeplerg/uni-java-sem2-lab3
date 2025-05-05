package labs.dirbrowser.infrastructure;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.types.Argon2;
import labs.dirbrowser.domain.PasswordHasher;

public class Argon2PasswordHasher implements PasswordHasher {
    private final Argon2Function argon2;

    public Argon2PasswordHasher(
            int memoryKiB,
            int iterations,
            int parallelismThreads,
            int keyLengthBytes) {
        int version = 19; // 0x13

        this.argon2 = Argon2Function.getInstance(
                memoryKiB, iterations, parallelismThreads, keyLengthBytes, Argon2.ID, version);
    }

    @Override
    public String hash(String password) {
        var hash = Password.hash(password).addRandomSalt().with(argon2);
        return hash.getResult();
    }

    @Override
    public boolean verify(String password, String hash) {
        return Password.check(password, hash).with(argon2);
    }
}
