package com.eme22.bolo.nsfw;

import java.util.Random;

public class NSFWStrings {

    public static final String[] kisses = {
            " Ha besado a ",
            " Le ha dado un chape a ",
            " Le ha robado un beso a "
    };

    public static final String[] bites = {
            " Le ha dado una probada a ",
            " Ha mordido a ",
            " Se ha querido comer a "
    };

    public static final String[] lick = {
            " Ha saboreado a ",
            " Ha lamido a "
    };

    public static final String[] slap = {
            " Ha abofeteado a ",
            " Ha cacheteado a "
    };

    private static final Random rand = new Random();

    public static String getRandomKiss(){
        return kisses[rand.nextInt(kisses.length)];
    }

    public static String getRandomBite() {
        return bites[rand.nextInt(bites.length)];
    }

    public static String getRandomLick() {
        return lick[rand.nextInt(lick.length)];
    }

    public static String getRandomSlap() {
        return slap[rand.nextInt(slap.length)];
    }
}
