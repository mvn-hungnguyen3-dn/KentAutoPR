package kent.core.selenium

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.ie.InternetExplorerDriver
import org.openqa.selenium.ie.InternetExplorerOptions
import org.openqa.selenium.opera.OperaDriver
import org.openqa.selenium.opera.OperaOptions
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.safari.SafariOptions

enum class DriverType : DriverSetup {
    FIREFOX {
        override fun getWebDriverObject(capabilities: DesiredCapabilities?, isHeadLess: Boolean): RemoteWebDriver {
            WebDriverManager.firefoxdriver().setup()
            val options = FirefoxOptions()
            options.merge(capabilities)
            options.setHeadless(HEADLESS)
            if (isHeadLess) {
                options.addArguments("--headless")
                options.addArguments("--window-size=1920,1080")
            }
            return FirefoxDriver(options)
        }
    },
    CHROME {
        override fun getWebDriverObject(capabilities: DesiredCapabilities?, isHeadLess: Boolean): RemoteWebDriver {
            WebDriverManager.chromedriver().setup()
            val chromePreferences = HashMap<String, Any>()
            chromePreferences["profile.password_manager_enabled"] = false
            val options = ChromeOptions()
            options.merge(capabilities)
            options.setHeadless(HEADLESS)
            options.addArguments("--no-default-browser-check")
            if (isHeadLess) {
                options.addArguments("--headless")
                options.addArguments("--window-size=1920,1080")
            }
            options.setExperimentalOption("prefs", chromePreferences)
            return ChromeDriver(options)
        }
    },
    IE {
        override fun getWebDriverObject(capabilities: DesiredCapabilities?, isHeadLess: Boolean): RemoteWebDriver {
            WebDriverManager.iedriver().setup()
            val options = InternetExplorerOptions()
            options.merge(capabilities)
            options.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true)
            options.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true)
            options.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true)
            return InternetExplorerDriver(options)
        }
    },
    EDGE {
        override fun getWebDriverObject(capabilities: DesiredCapabilities?, isHeadLess: Boolean): RemoteWebDriver {
            WebDriverManager.edgedriver().setup()
            val options = EdgeOptions()
            options.merge(capabilities)
            return EdgeDriver(options)
        }
    },
    SAFARI {
        override fun getWebDriverObject(capabilities: DesiredCapabilities?, isHeadLess: Boolean): RemoteWebDriver {
            val options = SafariOptions()
            options.merge(capabilities)
            return SafariDriver(options)
        }
    },
    OPERA {
        override fun getWebDriverObject(capabilities: DesiredCapabilities?, isHeadLess: Boolean): RemoteWebDriver {
            WebDriverManager.operadriver().setup()
            val options = OperaOptions()
            options.merge(capabilities)
            if (isHeadLess) {
                options.addArguments("--headless")
                options.addArguments("--window-size=1920,1080")
            }
            return OperaDriver(options)
        }
    };

    override fun toString(): String {
        return super.toString().toLowerCase()
    }

    companion object {
        val HEADLESS = java.lang.Boolean.getBoolean("headless")
    }
}