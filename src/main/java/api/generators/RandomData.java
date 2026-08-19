package api.generators;

import com.mifmif.common.regex.Generex;
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

    // Валидное имя — два слова через пробел, см. CustomerProfileRequest.name.
    // Ниже — те же "два слова", но каждый раз ломающие ровно одно правило валидации.

    public static String randomSingleWordName() {
        return new Generex("[A-Z][a-z]{1,9}").random();
    }

    public static String randomNameWithDigits() {
        return new Generex("[A-Z][a-z]{1,9}[0-9]{1,3} [A-Z][a-z]{1,9}").random();
    }

    public static String randomNameWithoutSpace() {
        return new Generex("[A-Z][a-z]{1,9}[A-Z][a-z]{1,9}").random();
    }
}
