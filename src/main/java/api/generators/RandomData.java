package api.generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class RandomData {
    private RandomData(){}

    public static String getUsername(){
        return RandomStringUtils.randomAlphabetic(10);
    }
    public static String getPassword(){
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(3).toLowerCase() + "!" +
                RandomStringUtils.randomNumeric(2);
    }
    public static double randomDeposit() {
        Random random = new Random();
        return Math.round((0.01 + random.nextDouble() * 4999.99) * 100.0) / 100.0;
    }
}
