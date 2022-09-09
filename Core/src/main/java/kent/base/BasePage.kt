package kent.base

import io.appium.java_client.*
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.touch.WaitOptions
import io.appium.java_client.touch.offset.PointOption
import kent.api.GmailApi
import kent.core.Controller
import org.openqa.selenium.*
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

abstract class BasePage<T> {

    abstract fun open(): T

    abstract fun isPageDisplayed(): Boolean

    abstract fun waitForPageDisplayed(): T

    var driver: WebDriver? = null

    init {
        if (Controller.instance.getDriver() is AppiumDriver<*>) {
            if (Controller.instance.isMobileDriver()) {
                driver = Controller.instance.getDriver() as? MobileDriver<*>
            } else {
                driver = Controller.instance.getDriver() as? AppiumDriver<*>
                PageFactory.initElements(DefaultElementLocatorFactory(driver), this)
            }
        } else {
            driver = Controller.instance.getDriver()
            PageFactory.initElements(DefaultElementLocatorFactory(driver), this)
        }
    }

    open fun isElementDisplayed(element: WebElement?): Boolean {
        return try {
            element?.isDisplayed ?: false
        } catch (e: NoSuchElementException) {
            false
        }
    }

    open fun isElementDisplayedWithCountChanged(element: WebElement, count: Int): Boolean {
        return try {
            element.isDisplayed && element.text.toInt() == count
        } catch (e: NoSuchElementException) {
            return false
        }
    }

    /** This function is used for waiting a webElement displayed */
    protected open fun waitForElementDisplayed(element: WebElement?) {
        try {
            WebDriverWait(driver, Constant.DEFAULT_TIME_OUT).until { isElementDisplayed(element) }
        } catch (e: Exception) {
            //No-otp
        }
    }

    /** This function is used for waiting a webElement displayed */
    protected open fun waitForElementDisplayedWithCountChanged(element: WebElement, count: Int) {
        WebDriverWait(driver, Constant.DEFAULT_TIME_OUT).until { isElementDisplayedWithCountChanged(element, count) }
    }

    /** This function is used for waiting a list of webElements displayed */
    protected open fun waitForListElement(elements: List<WebElement?>) {
        WebDriverWait(driver, Constant.DEFAULT_TIME_OUT).until { elements.isNotEmpty() }
    }

    /** Wait for element displayed with timeout */
    fun waitForElementDisplay(by: By?, timeOutInSecond: Int) {
        try {
            WebDriverWait(
                driver, timeOutInSecond.toLong()
            ).until { driver: WebDriver? -> isElementDisplayed(driver!!.findElement(by) as MobileElement) }
        } catch (e: Exception) {
            // no-opt
        }
    }

    /**  Wait for element displayed with timeout */
    fun waitForElementDisplay(element: MobileElement?, timeOutInSecond: Int) {
        try {
            WebDriverWait(driver, timeOutInSecond.toLong()).until { isElementDisplayed(element) }
        } catch (e: Exception) {
            // no-opt
        }
    }

    fun clickNotification(element: MobileElement?): BasePage<*> {
        try {
            WebDriverWait(driver, Constant.TIME_OUT_NORMAL_ELEMENT.toLong()).until { isElementDisplayed(element) }
            element?.click()
        } catch (e: Exception) {
        }
        return this
    }

    /** Wait for element displayed without timeout */
    fun waitForElementDisplay(element: MobileElement?) {
        try {
            WebDriverWait(driver, Constant.TIME_OUT_NORMAL_ELEMENT.toLong()).until { isElementDisplayed(element) }
        } catch (e: Exception) {
            // no-opt
        }
    }

    /** Wait for element displayed without timeout */
    fun waitForElementEnable(element: MobileElement) {
        try {
            WebDriverWait(driver, Constant.TIME_OUT_NORMAL_ELEMENT.toLong()).until { element.isEnabled }
        } catch (e: Exception) {
            // no-opt
        }
    }

    fun waitForElementEnable(element: WebElement) {
        try {
            WebDriverWait(driver, Constant.TIME_OUT_NORMAL_ELEMENT.toLong()).until { element.isEnabled }
        } catch (e: Exception) {
            // no-opt
        }
    }

    @Synchronized
    fun waitForNextStep(time: Int) {
        val start = System.currentTimeMillis()
        val end = start + time * 1000
        while (System.currentTimeMillis() < end) {
        }
    }

    fun waitForElementHide(element: MobileElement?) {
        try {
            WebDriverWait(driver, Constant.TIME_OUT_NORMAL_ELEMENT.toLong()).until { !isElementDisplayed(element)!! }
        } catch (e: Exception) {
            // no-opt
        }
    }

    fun clickNotificationTimeline(element: MobileElement?, time: Int) {
        try {
            waitForNextStep(time)
            element?.click()
        } catch (e: Exception) {
        }
    }

    fun waitForPageDisplayed(element: MobileElement?): BasePage<*> {
        try {
            WebDriverWait(driver, Constant.TIME_OUT_NORMAL_ELEMENT.toLong()).until { isElementDisplayed(element) }
        } catch (e: Exception) {
            // no-opt
        }
        return this
    }

    private fun isForElementPresent(element: MobileElement?): Boolean {
        return isElementDisplayed(element, Constant.TIME_OUT_MIN_ELEMENT)
    }

    fun isForElementPresent(element: WebElement?): Boolean {
        return isElementDisplayed(element, Constant.TIME_OUT_MIN_ELEMENT)
    }

    fun scrollToEndPage() {
        val js = driver as JavascriptExecutor
        js.executeScript("window.scrollBy(0,document.body.scrollHeight)")
    }

    fun waitForListElementDisplay(elements: List<MobileElement>?) {
        try {
            WebDriverWait(driver, Constant.TIME_OUT_NORMAL_ELEMENT.toLong()).until { isListElementDisplayed(elements) }
        } catch (e: Exception) {
        }
    }

    private fun isListElementDisplayed(elements: List<MobileElement>?): Boolean? {
        return try {
            elements?.isNotEmpty()
        } catch (exception: Exception) {
            exception.printStackTrace()
            false
        }
    }

    fun scrollToElementContainText(elementTextDisplay: String, element: MobileElement) {
        try {
            WebDriverWait(
                driver, Constant.TIME_OUT_MIN_ELEMENT.toLong()
            ).until(ExpectedConditions.visibilityOf(element))
        } catch (e: Exception) {
            if (driver is AndroidDriver<*>) {
                (driver as AndroidDriver<*>).findElementByAndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains(\"$elementTextDisplay\"))")
            } else if (driver is IOSDriver<*>) {
                val js: JavascriptExecutor = driver as IOSDriver<*>
                val scrollDownObject = HashMap<String, String>()
                scrollDownObject["predicateString"] = "value == '$elementTextDisplay'"
                scrollDownObject["direction"] = "down"
                js.executeScript("mobile: scroll", scrollDownObject)
                if (!element.isDisplayed) {
                    val scrollUpObject = HashMap<String, String>()
                    scrollUpObject["predicateString"] = "value == '$elementTextDisplay'"
                    scrollUpObject["direction"] = "up"
                    js.executeScript("mobile: scroll", scrollUpObject)
                }
            }
        }
    }

    fun scrollDownSmall(): BasePage<*> {
        try {
            driver?.let {
                val dimensions = it.manage().window().size
                val startPosition = (dimensions.getHeight() * 0.8).toInt()
                val endPosition = (dimensions.getHeight() * 0.7).toInt()
                PlatformTouchAction(it as? MobileDriver<*>).press(PointOption.point(OFFSET_X, startPosition))
                    .waitAction(WaitOptions.waitOptions(Duration.ofMillis(DURATION.toLong())))
                    .moveTo(PointOption.point(OFFSET_X, endPosition)).release().perform()
            }
            Thread.sleep(2000)
        } catch (e: Exception) {
        }
        return this
    }

    fun scrollUp(): BasePage<*> {
        try {
            driver?.let {
                val dimensions = it.manage().window().size
                val startPosition = (dimensions.getHeight() * OFFSET2).toInt()
                val endPosition = (dimensions.getHeight() * OFFSET1).toInt()
                PlatformTouchAction(it as? MobileDriver<*>).press(PointOption.point(OFFSET_X, startPosition))
                    .waitAction(WaitOptions.waitOptions(Duration.ofMillis(DURATION.toLong())))
                    .moveTo(PointOption.point(OFFSET_X, endPosition)).release().perform()
            }
        } catch (e: Exception) {
        }
        return this
    }

    fun scrollUpSmall(): BasePage<*> {
        try {
            driver?.let {
                val dimensions = it.manage().window().size
                val startPosition = (dimensions.getHeight() * 0.5).toInt()
                val endPosition = (dimensions.getHeight() * 0.8).toInt()
                PlatformTouchAction(it as? MobileDriver<*>).press(PointOption.point(OFFSET_X, startPosition))
                    .waitAction(WaitOptions.waitOptions(Duration.ofMillis(DURATION.toLong())))
                    .moveTo(PointOption.point(OFFSET_X, endPosition)).release().perform()
                Thread.sleep(2000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    fun scrollWithSpecialOffset(startOffset: Double, endOffset: Double): BasePage<*> {
        try {
            driver?.let {
                val dimensions = it.manage().window().size
                val startPosition = (dimensions.getHeight() * startOffset).toInt()
                val endPosition = (dimensions.getHeight() * endOffset).toInt()
                PlatformTouchAction(it as? MobileDriver<*>).press(PointOption.point(OFFSET_X, startPosition))
                    .waitAction(WaitOptions.waitOptions(Duration.ofMillis(DURATION.toLong())))
                    .moveTo(PointOption.point(OFFSET_X, endPosition)).release().perform()
                Thread.sleep(2000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    open fun clickBackButton(): BasePage<*> {
        return this
    }


    fun scrollToElement(element: MobileElement) {
        var count = 0
        while (!isForElementPresent(element) && count < 5) {
            scrollToView()
            count++
        }
    }

    private fun scrollToView(): BasePage<*> {
        try {
            driver?.let {
                val dimensions = it.manage().window().size
                val startPosition = (dimensions.getHeight() * OFFSET1).toInt()
                val endPosition = (dimensions.getHeight() * OFFSET2).toInt()
                PlatformTouchAction(it as? MobileDriver<*>).press(PointOption.point(OFFSET_X, startPosition))
                    .waitAction(WaitOptions.waitOptions(Duration.ofMillis(DURATION.toLong())))
                    .moveTo(PointOption.point(OFFSET_X, endPosition)).release().perform()
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
        }
        return this
    }

    fun clickNotification(by: By?): BasePage<*> {
        try {
            WebDriverWait(
                driver, Constant.TIME_OUT_MEDIUM_ELEMENT.toLong()
            ).until { driver: WebDriver? -> isElementDisplayed(driver!!.findElement(by) as MobileElement) }
            driver?.findElement<MobileElement>(by)?.click()
        } catch (e: Exception) {
            // no-opt
        }
        return this
    }

    /** ResetApp to logout app for android devices */
    fun resetApplication(): BasePage<*> {
        (driver as? MobileDriver<*>)?.resetApp()
        return this
    }

    fun swipeRight(): BasePage<*> {
        driver?.let {
            val size = it.manage().window().size
            val startY = size.height / 2
            val startX = (size.width * OFFSET_RIGHT1).toInt()
            val endX = (size.width * OFFSET_RIGHT2).toInt()
            PlatformTouchAction(it as? MobileDriver<*>).press(PointOption.point(startX, startY))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(DURATION.toLong())))
                .moveTo(PointOption.point(endX, startY)).release().perform()
        }
        return this
    }

    fun swipeLeft(): BasePage<*> {
        driver?.let {
            val size = it.manage().window().size
            val startY = size.height / 2
            val endX = (size.width * OFFSET_LEFT1).toInt()
            val startX = (size.width * OFFSET_LEFT2).toInt()
            PlatformTouchAction(it as? MobileDriver<*>).press(PointOption.point(endX, startY))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(DURATION.toLong())))
                .moveTo(PointOption.point(startX, startY)).release().perform()
        }
        return this
    }

    fun waitToCallApiSuccess(apiStatus: Boolean) {
        val wait = WebDriverWait(driver, Constant.TIME_OUT_NORMAL_ELEMENT.toLong())
        try {
            wait.until { apiStatus }
        } catch (e: Exception) {
            // No-opt
        }
    }

    fun waitToCallGmailApiSuccess(apiStatus: Boolean) {
        val wait = WebDriverWait(driver, Constant.TIME_OUT_NORMAL_ELEMENT.toLong())
        try {
            wait.until { apiStatus }
        } catch (e: Exception) {
            // No-opt
        }
    }

    private fun isElementDisplayed(element: MobileElement?, timeOutInSecond: Int): Boolean {
        var isVisible = false
        val wait = WebDriverWait(driver, timeOutInSecond.toLong())
        try {
            if (wait.until(ExpectedConditions.visibilityOf(element)) != null) {
                isVisible = true
            }
        } catch (e: Exception) {
            // No-opt
        }
        return isVisible
    }

    private fun isElementDisplayed(element: WebElement?, timeOutInSecond: Int): Boolean {
        var isVisible = false
        val wait = WebDriverWait(driver, timeOutInSecond.toLong())
        try {
            if (wait.until(ExpectedConditions.visibilityOf(element)) != null) {
                isVisible = true
            }
        } catch (e: Exception) {
            // No-opt
        }
        return isVisible
    }

    private fun isElementDisplayed(element: MobileElement?): Boolean? {
        return try {
            element?.isDisplayed
        } catch (e: NoSuchElementException) {
            false
        }
    }

    val passCode: String?
        get() = Companion.passCode
    val takeOverID: String?
        get() = Companion.takeOverID

    companion object {
        private const val OFFSET1 = 0.8
        private const val OFFSET2 = 0.2
        private const val DURATION = 500
        private const val OFFSET_X = 10
        private const val OFFSET_RIGHT1 = 0.90
        private const val OFFSET_RIGHT2 = 0.05
        private const val OFFSET_LEFT1 = 0.30
        private const val OFFSET_LEFT2 = 0.70
        private var passCode: String? = null
        private var takeOverID: String? = null

        private fun getScrollStartAndEndElements(driver: AppiumDriver<*>): HashMap<String, Int> {
            val dimensions: Dimension = driver.manage().window().size
            val screenHeightStart = dimensions.getHeight() * 0.5
            val scrollHash = HashMap<String, Int>()
            val scrollStart = screenHeightStart.toInt()
            scrollHash["start"] = scrollStart
            val screenHeightEnd = dimensions.getHeight() * 0.2
            val scrollEnd = screenHeightEnd.toInt()
            scrollHash["end"] = scrollEnd
            return scrollHash
        }
    }

    fun scrollToViewElement(element: WebElement) {
        val js = driver as JavascriptExecutor
        js.executeScript("arguments[0].scrollIntoView(true);", element)
    }

    open fun getFirstEmailOnInbox(): String? {
        val apiGmail = GmailApi()
        return apiGmail.getFirstEmailOnInbox
    }

    open fun getCognitoCodeOnInboxMail(): String {
        val apiGmail = GmailApi()
        return apiGmail.cognitoCode
    }

    open fun getError(element:WebElement): String {
        return if (isElementDisplayed(element)) {
            element.text
        } else {
            ""
        }
    }
}

class PlatformTouchAction(performsTouchActions: PerformsTouchActions?) :
    TouchAction<PlatformTouchAction>(performsTouchActions)
