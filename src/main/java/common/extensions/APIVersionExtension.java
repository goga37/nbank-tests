package common.extensions;

import api.configs.Config;
import common.annotations.APIVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

public class APIVersionExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        APIVersion annotation = extensionContext.getElement()
                .map(el -> el.getAnnotation(APIVersion.class))
                .orElse(null);

        if (annotation == null) {
            annotation = extensionContext.getTestClass()
                    .map(cls -> cls.getAnnotation(APIVersion.class))
                    .orElse(null);
        }

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("Нет ограничений по версии бэкенда");
        }

        String currentVersion = Config.getProperty("backendVersion");
        boolean matches = Arrays.stream(annotation.value())
                .anyMatch(version -> version.equals(currentVersion));

        if (matches) {
            return ConditionEvaluationResult.enabled("Текущая версия бэкенда удовлетворяет условию: " + currentVersion);
        }

        return ConditionEvaluationResult.disabled("Тест пропущен, так как текущая версия бэкенда " + currentVersion +
                " не входит в список поддерживаемых версий для теста: " + Arrays.toString(annotation.value()));
    }
}