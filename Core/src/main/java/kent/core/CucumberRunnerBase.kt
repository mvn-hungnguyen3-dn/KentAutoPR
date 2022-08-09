package kent.core

import io.cucumber.testng.AbstractTestNGCucumberTests
import org.testng.ITestContext
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Listeners

@Listeners(ScreenShotListener::class, PropertyListener::class, ReportListener::class)
open class CucumberRunnerBase : AbstractTestNGCucumberTests() {

    @DataProvider(parallel = true)
    override fun scenarios(): Array<Array<(Any)>> {
        return super.scenarios()
    }

    @BeforeClass(alwaysRun = true)
    @Throws(Exception::class)
    fun setUpClass(context: ITestContext?) {
        super.setUpClass()
        context?.run {
            Controller.instance.setContext(context)
        }
    }

    @AfterClass(alwaysRun = true)
    override fun tearDownClass() {
        super.tearDownClass()
        println("tearDownClass")
        Controller.instance.resetContext()
    }
}
