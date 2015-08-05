package com.github.games647.flexiblelogin.hasher;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptHasher implements Hasher {

    @Override
    public String hash(String rawPassword) {
        //generate a different salt for each user
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    @Override
    public boolean checkPassword(String passwordHash, String userInput) {
        return BCrypt.checkpw(userInput, passwordHash);
    }
}
