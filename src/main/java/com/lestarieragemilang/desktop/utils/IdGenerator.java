package com.lestarieragemilang.desktop.utils;

import java.util.concurrent.ThreadLocalRandom;
import com.google.common.base.Preconditions;

public class IdGenerator {

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