package com.healthcarenow.core.utils;

import org.bson.types.ObjectId;

public class IdUtils {

    /**
     * Generates a new unique MongoDB ObjectId string.
     * 
     * @return 24-character hex string
     */
    public static String generateId() {
        return new ObjectId().toHexString();
    }

    /**
     * Validates if a string is a valid MongoDB ObjectId.
     * 
     * @param hexString the string to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidId(String hexString) {
        return ObjectId.isValid(hexString);
    }
}
