package kent.core.selenium

import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver

interface DriverSetup {
    fun getWebDriverObject(capabilities: DesiredCapabilities?, isHeadLess: Boolean): RemoteWebDriver
}