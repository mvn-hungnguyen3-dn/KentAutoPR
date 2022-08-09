package mlvn.core

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import kent.core.Controller

/**
 * @author tien.hoang
 */
class PageFactory<T>(private val clazz: Class<T>) {

    fun create(): T? {
        val platform: String = when (Controller.instance.getDriver()) {
            is IOSDriver<*> -> {
                "IOS"
            }
            is AndroidDriver<*> -> {
                "Android"
            }
            else -> {
                ""
            }
        }
        try {
            var newClazz: Class<*>? = null
            try {
                newClazz = if (platform == "") {
                    Class.forName(clazz.name)
                } else {
                    Class.forName(clazz.name + platform)
                }
            } catch (e: Exception) {
                //no-opt
            }
            if (newClazz == null) {
                newClazz = clazz
            }
            val classHash = Controller.instance.getDriver().hashCode().toString() + Class.forName(clazz.name)
            if (pages.containsKey(classHash)) {
                return pages[classHash] as T?
            }
            val constructor = newClazz.getConstructor()
            val `object` = constructor.newInstance()
            pages[classHash] = `object`
            return `object` as T
        } catch (e: Exception) {
            println("An exception at PageFactory")
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private val pages: MutableMap<String, Any> = HashMap()
    }
}
