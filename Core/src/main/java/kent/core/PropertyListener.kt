package kent.core

import org.openqa.selenium.remote.RemoteWebDriver
import org.testng.ITestContext
import org.testng.TestListenerAdapter
import java.io.*
import java.util.*

class PropertyListener : TestListenerAdapter() {

    override fun onStart(iTestContext: ITestContext) {
        val driver = iTestContext.getAttribute("webDriver")
        if (driver !is RemoteWebDriver) {
            val fileName = iTestContext.name.replace(" ", "_") + ".properties"
            val reportDirectory = File(System.getProperty("user.dir")).absolutePath + "/target/classifications"
            val directory = File(reportDirectory)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val destFile = File("$reportDirectory/$fileName")
            try {
                val fos = FileOutputStream(destFile)
                val bw = BufferedWriter(OutputStreamWriter(fos))
                for ((key, value) in iTestContext.currentXmlTest.allParameters) {
                    bw.write("$key:$value")
                    bw.newLine()
                }
                bw.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onStart(iTestContext)
    }

    override fun onFinish(context: ITestContext) {
        super.onFinish(context)
        val driver = context.getAttribute("webDriver")
        if (driver is RemoteWebDriver) {
            val prop = Properties()
            val capabilities = driver.capabilities
            try {
                val output: OutputStream = FileOutputStream("target/cucumber-reports/browser.properties")
                prop.setProperty("BrowserName", capabilities.browserName)
                prop.setProperty("BrowserVersion", capabilities.version)
                prop.setProperty("Platform", capabilities.platform.name)
                val server = context.currentXmlTest.suite.getParameter("server")
                if (server != "") {
                    prop.setProperty("Server", context.currentXmlTest.suite.getParameter("server"))
                }
                prop.store(output, null)
                output.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
