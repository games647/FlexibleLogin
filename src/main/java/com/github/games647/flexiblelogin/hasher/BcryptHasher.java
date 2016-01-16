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
        String checkedHash = passwordHash;
        //$2y - Replace the prefix to $2a in order to provide compatibility with bcrypt generated passwords
        //from other implementations like in PHP
        if (passwordHash.charAt(2) == 'y') {
            //http://stackoverflow.com/questions/27418597/bcrypt-version-1-1-2y-for-java
            //skip one position in order to ignore the 'y'
            checkedHash = passwordHash.substring(0, 2) + 'a' + passwordHash.substring(3, passwordHash.length());
        }

        return BCrypt.checkpw(userInput, checkedHash);
    }
}
