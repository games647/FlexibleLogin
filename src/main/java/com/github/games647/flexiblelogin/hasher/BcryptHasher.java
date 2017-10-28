/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2017 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.games647.flexiblelogin.hasher;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptHasher implements Hasher {

    @Override
    public String hash(String rawPassword) {
        //generate a different salt for each user
        String hashpw = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        if (hashpw.startsWith("$2a")) {
            return "$2y" + hashpw.substring(3);
        }

        return hashpw;
    }

    @Override
    public boolean checkPassword(String passwordHash, String userInput) {
        String hash = passwordHash;
        if (hash.startsWith("$2y")) {
            hash = "$2a" + hash.substring(3);
        }

        return BCrypt.checkpw(userInput, hash);
    }
}
