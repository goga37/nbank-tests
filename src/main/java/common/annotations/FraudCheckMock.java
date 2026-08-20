package common.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FraudCheckMock {

    /**
     * Сценарий, который вернёт замоканный фрод-сервис, и ожидаемый в ответ на него ответ банка.
     * Игнорируется, если httpStatus() != 200 — тогда фрод-сервис имитирует отказ целиком.
     */
    FraudCheckScenario scenario() default FraudCheckScenario.LOW_RISK;

    /**
     * The WireMock port to use
     */
    int port() default 8080;

    /**
     * The endpoint path to mock
     */
    String endpoint() default "/fraud-check";

    /**
     * HTTP status the mock responds with. Use a non-200 value (e.g. 500) to simulate
     * the fraud detection service being unavailable/broken.
     */
    int httpStatus() default 200;
}
