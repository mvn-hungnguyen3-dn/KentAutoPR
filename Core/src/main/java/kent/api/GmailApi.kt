package kent.api

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.Base64
import com.google.api.client.util.StringUtils
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.GeneralSecurityException
import java.util.regex.Pattern

class GmailApi {

    val getFirstEmailOnInbox: String
        get() {
            gmailService
            return mailBody
        }

    val cognitoCode: String
        get() {
            try {
                gmailService
                val regex = "\\d{6}"
                val r = Pattern.compile(regex)
                val m = r.matcher(mailBody)
                if (m.find()) {
                    return m.group(0)
                } else {
                    println("NO MATCH")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

    // Access Gmail inbox
    private val mailBody: String
        get() {
            var data = ""
            // Access Gmail inbox
            try {
                Thread.sleep(10000)
                val messagesResponse: ListMessagesResponse? = service?.users()?.messages()?.list(user)?.execute()
                val messages: List<Message>? = messagesResponse?.messages
                val message: Message? = service?.users()?.messages()?.get(user, messages?.get(0)?.id ?: "")?.execute()
                data = if (message?.payload?.parts == null) {
                    message?.payload?.body?.data.toString()
                } else {
                    message.payload.parts[0].body.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return StringUtils.newStringUtf8(Base64.decodeBase64(data))
        }

    // Read credentials.json
    @get:Throws(IOException::class, GeneralSecurityException::class)
    private val gmailService: Gmail?
        get() {
            var property: String
            if (System.getProperty("workSpace") != null) {
                property = System.getProperty("workSpace")
            } else {
                property = System.getProperty("user.dir")
                if (property.contains("/App")) {
                    property = property.replace("/App", "")
                }
            }
            val path = "$property/asset/credentials.json"
            val filePath = File(path)
            val `in`: InputStream = FileInputStream(filePath) // Read credentials.json
            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

            // Credential builder
            val authorize: Credential =
                GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(JSON_FACTORY).setClientSecrets(
                        clientSecrets.details.clientId, clientSecrets.details.clientSecret
                    ).build().setAccessToken(accessToken).setRefreshToken(
                        refreshToken
                    )
            // Create Gmail service
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            service = Gmail.Builder(httpTransport, JSON_FACTORY, authorize).setApplicationName(APPLICATION_NAME).build()
            return service
        }
    private val accessToken: String?
        get() {
            try {
                val params: MutableMap<String, Any> = LinkedHashMap()
                params["grant_type"] = "refresh_token"
                params["client_id"] = clientID
                params["client_secret"] = clientSecret
                params["refresh_token"] = refreshToken
                val postData = StringBuilder()
                for ((key, value) in params) {
                    if (postData.isNotEmpty()) {
                        postData.append('&')
                    }
                    postData.append(URLEncoder.encode(key, "UTF-8"))
                    postData.append('=')
                    postData.append(URLEncoder.encode(value.toString(), "UTF-8"))
                }
                val postDataBytes = postData.toString().toByteArray(charset("UTF-8"))
                val url = URL("https://accounts.google.com/o/oauth2/token")
                val con = url.openConnection() as HttpURLConnection
                con.doOutput = true
                con.useCaches = false
                con.requestMethod = "POST"
                con.outputStream.write(postDataBytes)
                val reader = BufferedReader(InputStreamReader(con.inputStream))
                val buffer = StringBuffer()
                var line = reader.readLine()
                while (line != null) {
                    buffer.append(line)
                    line = reader.readLine()
                }
                val json = JSONObject(buffer.toString())
                return json.getString("access_token")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return null
        }

    companion object {
        private const val APPLICATION_NAME = "Kent PR"
        private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
        private const val user = "me"
        private const val clientID = "441323701987-7p1p59aqu670il9icgkf31maisrggqmr.apps.googleusercontent.com"
        private const val clientSecret = "lyjI_1YSzUltQOeRorE__ziD"
        private const val refreshToken =
            "1//0eJ7AK-k4U0QSCgYIARAAGA4SNwF-L9IrT77-k-11RzI5zZUnJmvAvERxlAyDUTFdA979FZWX_JHTRRvqRMb5SivvDNMx5ROENfo"
        var service: Gmail? = null
    }
}
