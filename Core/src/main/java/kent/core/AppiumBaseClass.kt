package kent.core

import io.appium.java_client.AppiumDriver

abstract class AppiumBaseClass {
    protected fun driver(): AppiumDriver<*>? {
        return AppiumController.instance.getDriver()
    }
}
