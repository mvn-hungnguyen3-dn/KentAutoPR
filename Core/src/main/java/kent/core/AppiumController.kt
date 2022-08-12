package kent.core

import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.remote.AndroidMobileCapabilityType
import io.appium.java_client.remote.IOSMobileCapabilityType
import io.appium.java_client.remote.MobileCapabilityType
import kent.base.Constant
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.DesiredCapabilities
import org.testng.xml.XmlTest
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit

class AppiumController {
    private val driverFactoryThread = ThreadLocal<AppiumDriver<*>?>()
    private lateinit var driver: AppiumDriver<*>

    /**
     * Get Appium driver #synchronized
     *
     * @return appium driver
     */
    @Synchronized
    fun getDriver(): AppiumDriver<*>? {
        if (driverFactoryThread.get() == null) {
            try {
                startDefaultServer()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
        }
        return driverFactoryThread.get()
    }

    /**
     * Start test session
     *
     * @param xmlTest
     * @throws MalformedURLException
     */
    @Synchronized
    @Throws(MalformedURLException::class)
    fun start(xmlTest: XmlTest) {
        val capabilities = parseCapabilities(xmlTest)
        //        capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
        val platformName = capabilities.getCapability(MobileCapabilityType.PLATFORM_NAME)
        if (platformName.toString().equals("android", ignoreCase = true)) {
            // Android platform
            driver = AndroidDriver<MobileElement>(URL(xmlTest.getParameter("server")), capabilities)
            (driver as AndroidDriver<*>?)?.unlockDevice()
        } else if (platformName.toString().equals("ios", ignoreCase = true)) {
            //                capabilities.setCapability("useNewWDA", true)
//                capabilities.setCapability("usePrebuiltWDA", true)
            // iOS platform
            driver = IOSDriver<WebElement>(URL(xmlTest.getParameter("server")), capabilities)
        } else {
            println(
                "Error: Unknown platform " + capabilities.getCapability(MobileCapabilityType.PLATFORM_NAME).toString()
            )
        }
        driver.manage().timeouts().implicitlyWait(Constant.TIME_OUT_MIN_ELEMENT.toLong(), TimeUnit.SECONDS)
        val contextNames = driver.contextHandles
        for (contextName in contextNames) {
            if (contextName.contains("WEBVIEW") || contextName.contains("NATIVE_APP")) {
                driver.context(contextName)
                break
            }
        }
        driverFactoryThread.set(driver)
    }

    /**
     * Stop test session
     */
    fun stop() {
        driver.quit()
    }

    @Synchronized
    @Throws(MalformedURLException::class)
    private fun startDefaultServer() {
        val xmlTest = XmlTest()
        xmlTest.setParameters(defaultCMPlusIosParameters())
        start(xmlTest)
    }

    private fun parseCapabilities(xmlTest: XmlTest): DesiredCapabilities {
        var workSpace: String
        if (System.getProperty("workSpace") != null) {
            workSpace = System.getProperty("workSpace")
        } else {
            workSpace = System.getProperty("user.dir")
            if (workSpace.contains("/CM")) {
                workSpace = workSpace.replace("/CM", "")
            }
        }
        val capabilities = DesiredCapabilities()
        for (key in xmlTest.localParameters.keys) {
            var value = xmlTest.localParameters[key]
            if (key.equals(MobileCapabilityType.APP, ignoreCase = true)) {
                if (value?.startsWith("http") == false) {
                    value = workSpace + value
                }
            }
            if (!key.equals("server", ignoreCase = true)) {
                println("DesiredCapabilities: $key: $value")
                if (value == "false" || value == "true") {
                    capabilities.setCapability(key, value.toBoolean())
                } else {
                    capabilities.setCapability(key, value)
                }
            }
        }
        return capabilities
    }

    private fun defaultCMPlusAndroidParameters(): Map<String, String> {
        val parameters: MutableMap<String, String> = HashMap()
        parameters[MobileCapabilityType.PLATFORM_NAME] = "android"
        parameters[MobileCapabilityType.DEVICE_NAME] = "SamSung Galaxy"
        //        parameters.put(MobileCapabilityType.UDID, "R58N22PKRQV");
        parameters[MobileCapabilityType.PLATFORM_VERSION] = "10"
        parameters[MobileCapabilityType.AUTOMATION_NAME] = "UiAutomator2"
        parameters[AndroidMobileCapabilityType.APP_PACKAGE] = "jp.co.lc.karadakawarunavi"
        parameters[AndroidMobileCapabilityType.APP_ACTIVITY] = "jp.co.lc.platform.ui.splash.DeepLinkActivity"
        parameters[MobileCapabilityType.APP] = "/appfile/android/production_jp.co.lc.karadakawarunavi_v6.0.9.apk"
        parameters["server"] = "http://127.0.0.1:4723/wd/hub"
        return parameters
    }

    private fun defaultCMPlusIosParameters(): Map<String, String> {
        val parameters: MutableMap<String, String> = HashMap()
        parameters[MobileCapabilityType.PLATFORM_NAME] = "ios"
        parameters[MobileCapabilityType.DEVICE_NAME] = "iPhone 12"
        parameters[IOSMobileCapabilityType.XCODE_ORG_ID] = "G72ZCJZ827"
        parameters[IOSMobileCapabilityType.XCODE_SIGNING_ID] = "iPhone Developer"
        parameters[MobileCapabilityType.PLATFORM_VERSION] = "14.6"
        parameters[MobileCapabilityType.UDID] = "00008101-000600E62EB9003A"
        parameters[IOSMobileCapabilityType.UPDATE_WDA_BUNDLEID] = "vn.ml.webdriveragent"
        parameters[MobileCapabilityType.AUTOMATION_NAME] = "XCUITest"
        parameters[MobileCapabilityType.APP] = "/appfile/ios/KNAVI.ipa"
        parameters["server"] = "http://127.0.0.1:4723/wd/hub"
        return parameters
    }

    companion object {
        @JvmField
        var instance = AppiumController()
    }
}
