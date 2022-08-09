package stepdefs

import kent.base.BaseDefinitions
import kent.core.PageFactory
import org.testng.Assert
import pages.DemoPage

class DemoDefinitions : BaseDefinitions() {

    private val demoPage: DemoPage? = PageFactory(DemoPage::class.java).create()

    init {
        When("Open Demo Page screen") {
            demoPage?.open()
        }
        Then("Move to Demo page screen successfully") {
            demoPage?.waitForPageDisplayed()
            Assert.assertTrue(demoPage?.isPageDisplayed() ?: false, "Demo Page is not displayed")
        }
    }
}
