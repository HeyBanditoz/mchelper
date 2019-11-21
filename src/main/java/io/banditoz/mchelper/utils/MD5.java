package io.banditoz.mchelper.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    /**
     * Computes the MD5 hash given a string. See
     * @param toHash The String to be hashed.
     * @return The MD5 hash of the string.
     * @see <a href="https://stackoverflow.com/a/30119004">https://stackoverflow.com/a/30119004</a>
     */
    public static String computeMD5(String toHash) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(StandardCharsets.UTF_8.encode(toHash));
        return String.format("%032x", new BigInteger(1, md5.digest()));
    }
}
