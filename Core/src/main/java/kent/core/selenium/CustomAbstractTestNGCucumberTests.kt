package mlvn.core.selenium

import io.cucumber.testng.FeatureWrapper
import io.cucumber.testng.PickleWrapper
import io.cucumber.testng.TestNGCucumberRunner
import org.testng.ITestContext
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

abstract class CustomAbstractTestNGCucumberTests {
    private var browserCount = 0
    private var testNGCucumberRunner: TestNGCucumberRunner? = null
    private var threadCount = 0

    @BeforeClass(alwaysRun = true)
    @Throws(Exception::class)
    open fun setUpClass(context: ITestContext) {
        threadCount = context.currentXmlTest.suite.tests[0].threadCount
        testNGCucumberRunner = TestNGCucumberRunner(this.javaClass)
    }

    @Test(groups = ["cucumber"], description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
    @Throws(Throwable::class)
    open fun scenario(pickle: PickleWrapper, cucumberFeature: FeatureWrapper?) {
        testNGCucumberRunner?.runScenario(pickle.pickle)
    }

    @DataProvider
    open fun scenarios(): Array<Array<Any?>>? {
        val scenarios = ArrayList<Any>(listOf(*testNGCucumberRunner!!.provideScenarios()))
        val scenarioPerThread = scenarios.size / threadCount
        val runScenarios: List<Any> = if (browserCount == threadCount - 1) scenarios.subList(
            browserCount * scenarioPerThread,
            scenarios.size
        ) else scenarios.subList(browserCount * scenarioPerThread, browserCount * scenarioPerThread + scenarioPerThread)
        println("Thread " + browserCount + " run " + runScenarios.size + " scenarios")
        browserCount++
        return if (testNGCucumberRunner == null) Array(0) { arrayOfNulls(0) } else arrayOf(runScenarios.toTypedArray())
    }

    @AfterClass(alwaysRun = true)
    @Throws(Exception::class)
    open fun tearDownClass() {
        testNGCucumberRunner?.finish()
    }
}
