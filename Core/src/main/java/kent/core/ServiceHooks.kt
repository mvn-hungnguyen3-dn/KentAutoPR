package kent.core

import org.junit.Before

class ServiceHooks {
    @Before
    fun initializeTest() {
        if (isFirstStep) {
            Runtime.getRuntime().addShutdownHook(Thread { afterAll() })
            isFirstStep = false
            //            AppiumController.instance.getDriver().launchApp();
        }
    }

    private fun afterAll() {
        AppiumController.instance.stop()
    }

    companion object {
        private var isFirstStep = true
    }
}
