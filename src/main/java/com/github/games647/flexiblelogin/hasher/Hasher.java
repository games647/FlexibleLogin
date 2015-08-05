package com.github.games647.flexiblelogin.hasher;

public interface Hasher {

    String hash(String rawPassword) throws Exception;

    boolean checkPassword(String passwordHash, String userInput) throws Exception;
}
