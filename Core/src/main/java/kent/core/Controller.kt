package kent.core

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.remote.AndroidMobileCapabilityType
import io.appium.java_client.remote.MobileCapabilityType
import kent.core.selenium.Browser.BROWSER_CHROME
import kent.core.selenium.Browser.BROWSER_EDGE
import kent.core.selenium.Browser.BROWSER_FIREFOX
import kent.core.selenium.Browser.BROWSER_IE
import kent.core.selenium.Browser.BROWSER_OPERA
import kent.core.selenium.Browser.BROWSER_SAFARI
import kent.core.selenium.DriverType
import org.openqa.selenium.Proxy
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.testng.ITestContext
import org.testng.xml.XmlTest
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class Controller {

    companion object{
        var instance = Controller()
        var driver : RemoteWebDriver? = null
    }

    private val operatingSystem = System.getProperty("os.name").uppercase(Locale.getDefault())
    private val systemArchitecture = System.getProperty("os.arch")
    private val proxyEnabled = java.lang.Boolean.getBoolean("proxyEnabled")
    private val proxyHostname = System.getProperty("proxyHost")
    private val proxyPort = Integer.getInteger("proxyPort")
    private val proxyDetails = String.format("%s:%d", proxyHostname, proxyPort)
    private val driverFactoryThread = ThreadLocal<RemoteWebDriver>()
    private var context: ITestContext? = null
    private var isMobileDriver = false

    @Synchronized
    fun getDriver(): RemoteWebDriver? {
        if (driverFactoryThread.get() == null) {
            if (this.context == null) {
                println("Start driver from junit")
                startDefaultServer()
            } else {
                /*  Start driver from xml test suite */
                println("Start driver from xml test suite")
                this.context?.currentXmlTest?.run {
                    start(this)
                }
            }
        }
        return driverFactoryThread.get()
    }

    @Synchronized
    fun isMobileDriver() = isMobileDriver

    @Synchronized
    fun setContext(context: ITestContext) {
        this.context = context
    }

    fun quitDriver() {
        driver?.quit()
        driverFactoryThread.get()?.quit()
        driverFactoryThread.remove()
    }

    fun resetContext() {
        println("resetContext")
        this.context = null
        quitDriver()
    }

    private fun getOfficialBrowserName(browserName: String): String {
        return when (browserName) {
            BROWSER_IE -> "internet explorer"
            BROWSER_EDGE -> "MicrosoftEdge"
            BROWSER_OPERA -> "operablink"
            BROWSER_CHROME, BROWSER_FIREFOX, BROWSER_SAFARI -> browserName
            else -> browserName
        }
    }

    private fun getDriverType(browserName: String): DriverType {
        return when (browserName) {
            BROWSER_FIREFOX -> DriverType.FIREFOX
            BROWSER_SAFARI -> DriverType.SAFARI
            BROWSER_IE -> DriverType.IE
            BROWSER_EDGE -> DriverType.EDGE
            BROWSER_OPERA -> DriverType.OPERA
            else -> DriverType.CHROME
        }
    }

    @Throws(MalformedURLException::class)
    private fun instantiateWebDriver(driverType: DriverType, server: String?, isHeadLess: Boolean) {
        println(" ")
        println("Local Operating System: $operatingSystem")
        println("Local Architecture: $systemArchitecture")
        println("Selected Browser: $driverType")
        println("Selected server: $server")
        println(" ")
        val desiredCapabilities = DesiredCapabilities()
        if (proxyEnabled) {
            val proxy = Proxy()
            proxy.proxyType = Proxy.ProxyType.MANUAL
            proxy.httpProxy = proxyDetails
            proxy.sslProxy = proxyDetails
            desiredCapabilities.setCapability(CapabilityType.PROXY, proxy)
        }
        if (server != null && server.isNotEmpty()) {
            val seleniumGridURL = URL(server)
            desiredCapabilities.browserName = getOfficialBrowserName(driverType.toString())
            driver = RemoteWebDriver(seleniumGridURL, desiredCapabilities)
        } else {
            driver = driverType.getWebDriverObject(desiredCapabilities, isHeadLess)
        }

        /* Set screen size to demo */
//        driver?.manage()?.window()?.position = Point(490, 47)
//        driver?.manage()?.window()?.size =
//            Dimension(driver?.manage()?.window()?.size?.height!! + 200, driver?.manage()?.window()?.size?.height!!)
        driverFactoryThread.set(driver)
    }

    @Synchronized
    @Throws(MalformedURLException::class)
    private fun startDefaultServer() {
        val xmlTest = XmlTest()
        xmlTest.setParameters(defaultWebParameters())
        start(xmlTest)
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
        val platformName = capabilities.getCapability(MobileCapabilityType.PLATFORM_NAME)
        val browserName = capabilities.getCapability(MobileCapabilityType.BROWSER_NAME)
        if (platformName != null && browserName != null) {
            // Mobile web
            isMobileDriver = false
            if (platformName.toString().equals("android", ignoreCase = true))
            // Android platform
            {
                capabilities.setCapability(MobileCapabilityType.NO_RESET, true)
                val url = xmlTest.getParameter("server")
                println("Android driver")

                /*  Set Android Chrome to fix issue not using FindElementById  */
                val chromeOption = ChromeOptions()
                chromeOption.setExperimentalOption("w3c", false)
                capabilities.setCapability(AndroidMobileCapabilityType.CHROME_OPTIONS, chromeOption)
                driver = AndroidDriver<WebElement>(URL(url), capabilities)
                (driver as AndroidDriver<*>?)?.unlockDevice()
            }
            // iOS platform
            else if (platformName.toString().equals("ios", ignoreCase = true)) {
                println("IOS driver")
                driver = IOSDriver<WebElement>(URL(xmlTest.getParameter("server")), capabilities)
            } else {
                println("Error: Unknown platform $platformName")
            }
        } else {
            if (platformName == null) {
                isMobileDriver = false
                if (browserName != null) {
                    //Selenium
                    val server = xmlTest.getParameter("server")
                    val headLess = xmlTest.getParameter("headless")
                    val driverType = getDriverType(browserName.toString())
                    try {
                        instantiateWebDriver(driverType, server, java.lang.Boolean.parseBoolean(headLess))
                    } catch (e: MalformedURLException) {
                        e.printStackTrace()
                    }
                } else {
                    println("Error: Unknown browserName $browserName")
                }
            } else {
                isMobileDriver = true
                capabilities.setCapability(MobileCapabilityType.NO_RESET, true)
                when {
                    // Android platform
                    platformName.toString().equals("android", ignoreCase = true) -> {
                        val url = xmlTest.getParameter("server")
                        driver = AndroidDriver<MobileElement>(URL(url), capabilities)
                        (driver as AndroidDriver<*>?)?.unlockDevice()
                    }
                    // iOS platform
                    platformName.toString().equals("ios", ignoreCase = true) -> {
                        driver = IOSDriver<MobileElement>(URL(xmlTest.getParameter("server")), capabilities)
                    }
                    else -> {
                        println("Error: Unknown platform $platformName")
                    }
                }
            }
        }
        driverFactoryThread.set(driver)
        context?.setAttribute("driver", driver)
    }

    private fun parseCapabilities(xmlTest: XmlTest): DesiredCapabilities {
        var workSpace: String
        if (System.getProperty("workSpace") != null) {
            workSpace = System.getProperty("workSpace")
        } else {
            workSpace = System.getProperty("user.dir")
            if (workSpace.contains("/KT")) {
                workSpace = workSpace.replace("/KT", "")
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

    private fun defaultWebParameters(): Map<String, String>? {
        val parameters: MutableMap<String, String> = HashMap()
        parameters[MobileCapabilityType.BROWSER_NAME] = BROWSER_CHROME
        parameters["headless"] = "false"
        parameters["server"] = ""
        if (context == null) {
            return parameters
        } else {
            quitDriver()
        }
        return null
    }

    private fun defaultAndroidWebParameters(): Map<String, String> {
        val parameters: MutableMap<String, String> = HashMap()
        val capabilities = DesiredCapabilities()
        parameters[MobileCapabilityType.PLATFORM_NAME] = "Android"
        parameters[MobileCapabilityType.DEVICE_NAME] = "Galaxy S7"
        parameters[MobileCapabilityType.PLATFORM_VERSION] = "10"
        parameters[MobileCapabilityType.AUTOMATION_NAME] = "UiAutomator2"
        // BROWSER_FIREFOX, BROWSER_SAFARI, BROWSER_IE, BROWSER_EDGE, BROWSER_OPERA
        parameters[MobileCapabilityType.BROWSER_NAME] = BROWSER_CHROME
        parameters["server"] = "http://127.0.0.1:4723/wd/hub"
        return parameters
    }

    private fun defaultIosWebParameters(): Map<String, String> {
        val parameters: MutableMap<String, String> = HashMap()
        parameters[MobileCapabilityType.PLATFORM_NAME] = "ios"
        parameters[MobileCapabilityType.DEVICE_NAME] = "iPhone 12"
        parameters[MobileCapabilityType.PLATFORM_VERSION] = "15.2"
//        parameters[IOSMobileCapabilityType.XCODE_SIGNING_ID] = "iPhone Developer"
//        parameters[IOSMobileCapabilityType.XCODE_ORG_ID] = "iPhone Developer"
        parameters[MobileCapabilityType.AUTOMATION_NAME] = "XCUITest"
        // BROWSER_FIREFOX, BROWSER_SAFARI, BROWSER_IE, BROWSER_EDGE, BROWSER_OPERA
        parameters[MobileCapabilityType.BROWSER_NAME] = BROWSER_SAFARI
        parameters["server"] = "http://127.0.0.1:4723/wd/hub"
        return parameters
    }
}
