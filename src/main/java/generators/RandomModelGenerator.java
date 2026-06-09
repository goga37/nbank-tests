package generators;

import com.mifmif.common.regex.Generex;
import models.BaseModel;
import org.apache.commons.lang3.RandomStringUtils;

import java.lang.reflect.Field;

public class RandomModelGenerator {

    public static <T extends BaseModel> T generate(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
                if (rule != null) {
                    field.set(instance, new Generex(rule.regex()).random());
                } else {
                    field.set(instance, generateDefault(field.getType()));
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate instance of " + clazz.getSimpleName(), e);
        }
    }

    private static Object generateDefault(Class<?> type) {
        if (type == String.class) return RandomStringUtils.randomAlphabetic(10);
        if (type == int.class || type == Integer.class) return (int) (Math.random() * 1000);
        if (type == long.class || type == Long.class) return (long) (Math.random() * 1000);
        if (type == boolean.class || type == Boolean.class) return true;
        return null;
    }
}