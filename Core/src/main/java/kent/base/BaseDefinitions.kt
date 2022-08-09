package kent.base

import io.cucumber.java8.En
import io.cucumber.java8.Scenario
import kent.core.Controller
import org.openqa.selenium.*
import org.openqa.selenium.support.pagefactory.DefaultFieldDecorator
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory
import org.openqa.selenium.support.pagefactory.FieldDecorator
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.annotations.AfterMethod

open class BaseDefinitions : En {

    @AfterMethod(alwaysRun = true)
    fun clearCookies() {
        try {
            println("clearCookies")
            Controller.instance.getDriver()?.manage()?.deleteAllCookies()
        } catch (ignored: Exception) {
            println("Unable to clear cookies, driver objects is not viable...")
        }
    }

    private fun initElements(factory: ElementLocatorFactory, page: Any?) {
        initElements(DefaultFieldDecorator(factory), page)
    }

    private fun initElements(decorator: FieldDecorator, page: Any?) {
        page?.run {
            var proxyIn: Class<*> = page.javaClass
            while (proxyIn != Any::class.java) {
                proxyFields(decorator, page, proxyIn)
                proxyIn = proxyIn.superclass
            }
        }
    }

    private fun proxyFields(decorator: FieldDecorator, page: Any, proxyIn: Class<*>) {
        val fields = proxyIn.declaredFields
        for (field in fields) {
            val value = decorator.decorate(page.javaClass.classLoader, field)
            if (value != null) {
                try {
                    field.isAccessible = true
                    field[page] = value
                } catch (var10: IllegalAccessException) {
                    throw RuntimeException(var10)
                }
            }
        }
    }

    protected fun getDriver(): WebDriver? {
        println("getDriver in baseDefinition")
        return Controller.instance.getDriver()
    }

    /**
     * This function is used for waiting a page displayed
     *
     * @param url     - The url of page is checking
     * @param element - The webElement is dispayed on this page
     */
    protected fun waitForPageDisplayed(url: String?, element: WebElement?) {
        WebDriverWait(
            getDriver(), Constant.DEFAULT_TIME_OUT
        ).until { webDriver: WebDriver -> (webDriver as JavascriptExecutor).executeScript("return document.readyState") == "complete" }
        waitForElementDisplayed(element)
        WebDriverWait(getDriver(), Constant.DEFAULT_TIME_OUT).until(ExpectedConditions.urlToBe(url))
    }

    /**
     * This function is used for waiting a webElement displayed
     *
     * @param element - The webElement is checking display
     */
    protected fun waitForElementDisplayed(element: WebElement?) {
        WebDriverWait(getDriver(), Constant.DEFAULT_TIME_OUT).until(
            ExpectedConditions.visibilityOf(
                element
            )
        )
    }

    protected fun embedScreenshot(scenario: Scenario) {
        println("embedScreenshot")
        val ts = Controller.instance.getDriver() as TakesScreenshot
        val src = ts.getScreenshotAs(OutputType.BYTES)
        scenario.attach(src, "image/png", "Screenshot: " + scenario.name)
    }

    protected fun quitDriver() {
        Controller.instance.quitDriver()
    }
}
