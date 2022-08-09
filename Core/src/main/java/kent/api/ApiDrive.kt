package kent.api

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kent.base.Constant
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.GeneralSecurityException

class ApiDrive {
    /** Get value of the first row from the special data sheet */
    private fun getFirstRowFromDataSheet(spreadSheetRange: String): String {
        var firstRowData = ""
        try {
            val response: ValueRange? =
                Companion.serviceSheet?.spreadsheets()?.values()?.get(Constant.SPREADSHEET_ID, spreadSheetRange)
                    ?.execute()
            val values = response?.getValues()
            if (values == null || values.isEmpty()) {
                println("No data found.")
            } else {
                firstRowData = values[0][0].toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return firstRowData
    }

    /** Get value of the first row from the special data sheet */
    fun getTakeOverIdSpecialCompany(name: String?): String {
        var spreadSheetRange = "[SpecialCompany]TakeoverID!C"
        var pos = 2
        when (name) {
            "Ajinomoto" -> {
                pos++
            }
            "Lawson" -> {
                pos += 2
            }
            "Kobe" -> {
                pos += 3
            }
            "Relo" -> {
                pos += 4
            }
            "Sportsclub" -> {
                pos += 5
            }
            "Insurance" -> {
                pos += 6
            }
            "D2D" -> {
                pos += 7
            }
        }
        spreadSheetRange += pos
        var firstRowData = ""
        try {
            val response: ValueRange? =
                Companion.serviceSheet?.spreadsheets()?.values()?.get(Constant.SPREADSHEET_ID, spreadSheetRange)
                    ?.execute()
            val values = response?.getValues()
            if (values == null || values.isEmpty()) {
                println("No data found.")
            } else {
                firstRowData = values[0][0].toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return firstRowData
    }

    fun deleteSecondRowFromDataSupporterSheet(sheetId: Int?) {
        val content = BatchUpdateSpreadsheetRequest()
        val request = Request().setDeleteDimension(
            DeleteDimensionRequest().setRange(
                DimensionRange().setSheetId(sheetId).setDimension("ROWS").setStartIndex(1).setEndIndex(2)
            )
        )
        val requests: MutableList<Request> = ArrayList()
        requests.add(request)
        content.requests = requests
        try {
            Companion.serviceSheet?.spreadsheets()?.batchUpdate(Constant.SPREADSHEET_ID, content)?.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteFirstRowFromDataSheet(sheetId: Int?) {
        val content = BatchUpdateSpreadsheetRequest()
        val request = Request().setDeleteDimension(
            DeleteDimensionRequest().setRange(
                DimensionRange().setSheetId(sheetId).setDimension("ROWS").setStartIndex(0).setEndIndex(1)
            )
        )
        val requests: MutableList<Request> = ArrayList()
        requests.add(request)
        content.requests = requests
        try {
            Companion.serviceSheet?.spreadsheets()?.batchUpdate(Constant.SPREADSHEET_ID, content)?.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Print the names and IDs for up to 10 files.
//    private val file: Unit
//        get() {
//            // Print the names and IDs for up to 10 files.
//            val result: FileList?
//            try {
//                result = serviceDriver?.files()?.list()?.setPageSize(10)?.setFields("nextPageToken, files(id, name)")
//                    ?.execute()
//                val files = result?.files
//                if (files == null || files.isEmpty()) {
//                    println("No files found.")
//                } else {
//                    println("Files:")
//                    for (file in files) {
//                        System.out.printf("%s (%s)\n", file.name, file.id)
//                    }
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }

    /** Read credentials.json */
    @get:Throws(IOException::class, GeneralSecurityException::class)
    private val driverService: Drive

    /** Credential builder */
    /** Create Drive service */
    ?
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
            val filePath = File(path.replace("/App", ""))
            val `in`: InputStream = FileInputStream(filePath) // Read credentials.json
            val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

            // Credential builder
            val authorize: Credential =
                GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(JSON_FACTORY).setClientSecrets(
                        clientSecrets.details.clientId, clientSecrets.details.clientSecret
                    ).build().setAccessToken(accessToken).setRefreshToken(
                        "1//0eP7QkKpBz7KCCgYIARAAGA4SNwF-L9IrHk7A8uAHihQIlWv3fIq0KfGgIOBLGhvhqapTcw7qEQ_4Ht5nw1J1LguP8_mzek3jEbk"
                    )
            // Create Drive service
            val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            serviceDriver =
                Drive.Builder(httpTransport, JSON_FACTORY, authorize).setApplicationName(APPLICATION_NAME).build()
            return serviceDriver
        }

    /** Read credentials.json */
    val serviceSheet: Sheets

    /** Credential builder */
    /** Create Drive service */
    ?
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
            val filePath = File(path.replace("/App", ""))
            try {
                val `in`: InputStream = FileInputStream(filePath) // Read credentials.json
                val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

                // Credential builder
                val authorize: Credential =
                    GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                        .setJsonFactory(JSON_FACTORY).setClientSecrets(
                            clientSecrets.details.clientId, clientSecrets.details.clientSecret
                        ).build().setAccessToken(accessToken).setRefreshToken(refreshToken)
                // Create Drive service
                val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
                Companion.serviceSheet =
                    Sheets.Builder(httpTransport, JSON_FACTORY, authorize).setApplicationName(APPLICATION_NAME).build()
            } catch (exception: IOException) {
                exception.printStackTrace()
            } catch (exception: GeneralSecurityException) {
                exception.printStackTrace()
            }
            return Companion.serviceSheet
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
        private const val clientID = "441323701987-7p1p59aqu670il9icgkf31maisrggqmr.apps.googleusercontent.com"
        private const val clientSecret = "lyjI_1YSzUltQOeRorE__ziD"
        private const val refreshToken =
            "1//0eWhE1j0d1lz5CgYIARAAGA4SNwF-L9IrbAkUhqYaYjcH12RJAvw95RZ75JNDPKy-iLEbIkwkWJNZDsJycxM44Br5DsQdbJ1OFtY"
        private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
        var serviceDriver: Drive? = null
        var serviceSheet: Sheets? = null
    }
}
