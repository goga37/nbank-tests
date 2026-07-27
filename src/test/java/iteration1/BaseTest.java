package iteration1;

import common.extensions.APIVersionExtension;
import common.extensions.TimingExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TimingExtension.class)
@ExtendWith(APIVersionExtension.class)
public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setUpTest(){
        softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest(){
        softly.assertAll();
    }
}
