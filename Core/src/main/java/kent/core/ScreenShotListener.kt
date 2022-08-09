package mlvn.core

import kent.core.Controller
import org.apache.commons.io.FileUtils
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver
import org.openqa.selenium.remote.Augmenter
import org.openqa.selenium.remote.RemoteWebDriver
import org.testng.ITestResult
import org.testng.TestListenerAdapter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author tien.hoang
 */
class ScreenShotListener : TestListenerAdapter() {

    override fun onTestFailure(result: ITestResult) {
        println("onTestFailure")
        val driver = Controller.instance.getDriver()
        if (driver is RemoteWebDriver) {
            try {
                val screenShotDirectory = System.getProperty("screenShotDirectory", "target/screenshots")
                val screenShotAbsolutePath =
                    screenShotDirectory + File.separator + System.currentTimeMillis() + "_" + result.name + ".png"
                val screenShot = File(screenShotAbsolutePath)
                if (createFile(screenShot)) {
                    try {
                        writeScreenShotToFile(driver, screenShot)
                    } catch (weNeedToAugmentOurDriverObject: ClassCastException) {
                        writeScreenShotToFile(Augmenter().augment(driver), screenShot)
                    }
                    println("Written screenShot to $screenShotAbsolutePath")
                } else {
                    System.err.println("Unable to create $screenShotAbsolutePath")
                }
            } catch (ex: Exception) {
                System.err.println("Unable to capture screenShot...")
                ex.printStackTrace()
            }
        } else {
            val calendar = Calendar.getInstance()
            val format = SimpleDateFormat("dd_MM_yyyy_hh_mm_ss")
            val methodName = result.name
            if (!result.isSuccess) {
                println("scrFile")
                val scrFile = (Controller.instance.getDriver() as TakesScreenshot).getScreenshotAs(OutputType.FILE)
                try {
                    val reportDirectory = File(System.getProperty("user.dir")).absolutePath + "/target"
                    val destFile =
                        File(reportDirectory + "/screenshots/" + methodName + "_" + format.format(calendar.time) + ".png")
                    FileUtils.copyFile(scrFile, destFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createFile(screenShot: File): Boolean {
        var fileCreated = false
        if (screenShot.exists()) {
            fileCreated = true
        } else {
            val parentDirectory = File(screenShot.parent)
            if (parentDirectory.exists() || parentDirectory.mkdirs()) {
                try {
                    fileCreated = screenShot.createNewFile()
                } catch (errorCreatingScreenShot: IOException) {
                    errorCreatingScreenShot.printStackTrace()
                }
            }
        }
        return fileCreated
    }

    private fun writeScreenShotToFile(driver: WebDriver?, screenshot: File) {
        try {
            val screenShotStream = FileOutputStream(screenshot)
            screenShotStream.write((driver as TakesScreenshot?)!!.getScreenshotAs(OutputType.BYTES))
            screenShotStream.close()
        } catch (unableToWriteScreenShot: IOException) {
            System.err.println("Unable to write " + screenshot.absolutePath)
            unableToWriteScreenShot.printStackTrace()
        }
    }
}
