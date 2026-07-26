package ui;

import api.configs.Config;
import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import common.extensions.AdminSessionExtension;
import common.extensions.BrowserMatchExtension;
import common.extensions.UserSessionExtension;
import iteration1.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
@ResourceLock(value = "selenoid-browser", mode = ResourceAccessMode.READ_WRITE)
public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getProperty("uiRemoteUrl");
        Configuration.baseUrl = Config.getProperty("uiBaseUrl");
        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("uiBrowserSize");
        Configuration.timeout = Long.parseLong(Config.getProperty("uiTimeout"));

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }
}


