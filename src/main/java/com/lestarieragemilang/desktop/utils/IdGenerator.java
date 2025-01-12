package com.lestarieragemilang.desktop.utils;

import java.util.concurrent.ThreadLocalRandom;
import com.google.common.base.Preconditions;

/**
 * Utility class for generating unique identifiers with custom prefixes.
 * This class uses Google Guava's preconditions and thread-safe random number generation.
 * 
 * @since 1.0
 */
public class IdGenerator {

    /**
     * Generates a random ID string with a specified prefix and bound.
     * The generated ID follows the format: "prefix-XXX" where XXX is a zero-padded number.
     *
     * @param prefix the string prefix to use for the ID (e.g., "PROD", "CUST")
     * @param bound the upper bound (exclusive) for the random number generation
     * @return a formatted string containing the prefix and a random number
     * @throws NullPointerException if prefix is null
     * @throws IllegalArgumentException if bound is not positive
     * 
     * @example 
     * generateRandomId("PROD", 100) might return "PROD-042"
     * generateRandomId("CUST", 1000) might return "CUST-531"
     */
    public static String generateRandomId(String prefix, int bound) {
        Preconditions.checkNotNull(prefix, "Prefix cannot be null");
        Preconditions.checkArgument(bound > 0, "Bound must be positive");
        
        int randomNumber = com.google.common.primitives.Ints.constrainToRange(
            ThreadLocalRandom.current().nextInt(bound),
            0,
            bound - 1
        );
        return String.format("%s-%03d", prefix, randomNumber);
    }
}