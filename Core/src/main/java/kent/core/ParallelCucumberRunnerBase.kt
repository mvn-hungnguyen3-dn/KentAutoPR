package kent.core

import kent.core.selenium.CustomAbstractTestNGCucumberTests
import org.testng.ITestContext
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Listeners

@Listeners(ScreenShotListener::class, PropertyListener::class, ReportListener::class)
open class ParallelCucumberRunnerBase : CustomAbstractTestNGCucumberTests() {
    @DataProvider
    fun features(): Array<Array<Any?>>? {
        return super.scenarios()
    }

    @BeforeClass(alwaysRun = true)
    @Throws(Exception::class)
    override fun setUpClass(context: ITestContext) {
        super.setUpClass(context)
        println("setUpClass")
        Controller.instance.start(context.currentXmlTest)
    }

    @AfterClass(alwaysRun = true)
    @Throws(Exception::class)
    override fun tearDownClass() {
        super.tearDownClass()
        println("tearDownClass")
        Controller.instance.quitDriver()
    }
}
