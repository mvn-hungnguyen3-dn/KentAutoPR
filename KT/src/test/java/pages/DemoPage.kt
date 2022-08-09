package pages

import kent.base.BasePage
import kent.base.Constant
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

class DemoPage : BasePage<DemoPage>() {

    @FindBy(css = ".YacQv")
    lateinit var searchBar: WebElement

    override fun open(): DemoPage {
        if (!isPageDisplayed()) {
            driver?.get(Constant.BASE_URL)
            waitForPageDisplayed()
        }
        return this
    }

    override fun isPageDisplayed(): Boolean {
        return isForElementPresent(searchBar)
    }

    override fun waitForPageDisplayed(): DemoPage {
        waitForElementDisplayed(searchBar)
        return this
    }

}