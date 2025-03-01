package util;

/**
 * HashUtil provides utility methods for generating hash values from strings.
 */
public class HashUtil {

    /**
     * Computes a hash value for a given string using a modified FNV-1a algorithm.
     *
     * @param str The input string to hash.
     * @return A non-negative hash value.
     */
    public static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
            hash += hash << 13;
            hash ^= hash >> 7;
            hash += hash << 3;
            hash ^= hash >> 17;
            hash += hash << 5;
        }

        // Ensure the hash is non-negative
        return Math.abs(hash);
    }
}
