package com.com.thoughtworks.challenge.utility;

import java.util.stream.Stream;

public class EncryptLogic {

    private static int counter = 65;
    private static int[] arrayToCheck = new int[26];

    private static String input =
            "GOVV GO MKX NY SD WI GKI, YB GO MKX KVV MYWO LKMU SX DSWO PYB DRO XOHD KVSQXWOXD KXN IYE'BO GOVMYWO DY DBI KXN USVV WO DROX, SX YR, CKI, KXYDROB 5,000 IOKBC?";
    private static int key = 20;

    private static String output =
            "A FAMOUS EXPLORER ONCE SAID, THAT THE EXTRAORDINARY IS IN WHAT WE DO, NOT WHO WE ARE. GO EXPLORE!";


    public static void main(String[] args) {
        Stream.iterate(0, e -> e + 1).limit(26).forEachOrdered(e -> {
            arrayToCheck[e] = counter;
            counter = counter + 1;
        });

        encryptCode(input, key, arrayToCheck);


    }

    private static char[] encryptCode(String input, int key, int[] arrayToCheck) {
        String output = null;

        if (input == null) {
            return null;
        }

        char[] chars = input.toCharArray();
        int[] asciValues = new int[chars.length];
        Stream.iterate(0, e -> e + 1).limit(chars.length).forEachOrdered(e -> {
            asciValues[e] = chars[e];
        });

        Stream.iterate(0, e -> e + 1).limit(chars.length).forEachOrdered(e -> {
            if (asciValues[e] >= arrayToCheck[0] && asciValues[e] <= arrayToCheck[25]) {
                int newAscii = updateAscii(asciValues[e], arrayToCheck, key);
                asciValues[e] = newAscii;
            }else {
                asciValues[e] = asciValues[e];
            }
        });

        char[] result = new char[chars.length];
        Stream.iterate(0, e->e+1).limit(chars.length).forEachOrdered(e -> {
            result[e] = (char) asciValues[e];
        });
        Stream.of(result).forEach(System.out::println);
        return result;
    }

    private static int updateAscii(int asciValue, int[] arrayToCheck, int key) {
        int counter = 1;
        while (counter <= key) {
            asciValue = asciValue - 1;
            if (asciValue < EncryptLogic.arrayToCheck[0]) {
                asciValue = EncryptLogic.arrayToCheck[25];
            }
            counter++;
        }
        return asciValue;
    }


    private static void checkEnd(int asciValue, int[] arrayToCheck) {
        int i = asciValue - 1 % arrayToCheck[0];

        asciValue = asciValue - 1;
        if (i > 90) {

        }

    }
}


