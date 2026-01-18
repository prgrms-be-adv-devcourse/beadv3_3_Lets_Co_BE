package co.kr.user.util;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import java.util.Base64;

@Component
public class BCryptUtil {

    public String encode(String password) {
        String basePW = Base64.getEncoder().encodeToString(password.getBytes());

        return BCrypt.hashpw(basePW, BCrypt.gensalt());
    }

    public boolean check(String password, String dbPassword) {
        String basePW = Base64.getEncoder().encodeToString(password.getBytes());

        return BCrypt.checkpw(basePW, dbPassword);
    }
}