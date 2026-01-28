package com.healthcarenow.core.utils;

import org.bson.types.ObjectId;

/**
 * Utility class for generating shared IDs across different databases.
 * Ensures that PostgreSQL entities use the same ID format as MongoDB documents.
 */
public class IdUtils {

    /**
     * Generates a new unique MongoDB ObjectId string.
     * Use this for setting IDs in PostgreSQL entities before saving.
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
