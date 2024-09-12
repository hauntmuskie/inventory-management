package com.lestarieragemilang.desktop.utils;

import java.util.Random;

public class IdGenerator {

    public static String generateRandomId(String prefix, int bound) {
        Random random = new Random();
        int randomNumber = random.nextInt(bound);
        return String.format("%s-%03d", prefix, randomNumber);
    }
}