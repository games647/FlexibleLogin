package com.github.games647.flexiblelogin.hasher;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Hasher implements Hasher {

    @Override
    public String hash(String rawPassword) throws Exception {
        return DigestUtils.md5Hex(rawPassword);
    }

    @Override
    public boolean checkPassword(String passwordHash, String userInput) throws Exception {
        return passwordHash.equals(DigestUtils.md5Hex(userInput));
    }
}
