@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package kent.core

import org.apache.commons.lang3.time.DurationFormatUtils
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import org.testng.ITestContext
import org.testng.TestListenerAdapter
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class ReportListener : TestListenerAdapter() {
    companion object {
        private const val TIME_NANO_SECOND = 1000000
    }

    private lateinit var files: Array<File>
    private var resultFeatures: JSONArray? = null
    private var totalFeatures: Long = 0
    private var featurePassed: Long = 0
    private var featureFailed: Long = 0
    private var totalScenarios: Long = 0
    private var scenarioPassed: Long = 0
    private var scenarioFailed: Long = 0
    private var totalSteps: Long = 0
    private var stepPassed: Long = 0
    private var stepFailed: Long = 0
    private var stepSkipped: Long = 0
    private var stepPending: Long = 0
    private var stepUndefined: Long = 0
    private var totalDuration: Long = 0
    private var passedDuration: Long = 0
    private var failedDuration: Long = 0
    private var skipDuration: Long = 0
    private var pendingDuration: Long = 0
    private var undefineDuration: Long = 0
    private val allFiles: Array<File>
        get() {
            val dir = File("target/cucumber-reports/")
            return dir.listFiles { _: File?, name: String -> name.endsWith(".json") }
        }

    override fun onFinish(testContext: ITestContext) {
        super.onFinish(testContext)
        try {
            mergeJsonFiles()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class, ParseException::class)
    private fun mergeJsonFiles() {
        val size: Int
        files = allFiles
        val resultIds: MutableList<String> = ArrayList()
        if (files.isNotEmpty()) {
            resultFeatures = getFeatures(FileReader(files[0].path))
            size = resultFeatures!!.size
            for (i in 0 until size) {
                resultIds.add((resultFeatures!![i] as JSONObject)["id"] as String)
            }
        } else {
            return
        }
        for (pos in 1 until files.size) {
            val fileFeatures = getFeatures(FileReader(files[pos].path))
            if (resultFeatures!!.size == 0) {
                resultFeatures = fileFeatures
            }
            for (i in 0 until size) {
                for (fileFeature in fileFeatures) {
                    val mergeObject = resultFeatures!![i] as JSONObject
                    val `object` = fileFeature as JSONObject
                    val idMergeFeature = mergeObject["id"] as String
                    val idFeature = `object`["id"] as String
                    if (idMergeFeature == idFeature) {
                        val mainElements = mergeObject["elements"] as JSONArray
                        val elements = `object`["elements"] as JSONArray
                        mainElements.addAll(elements)
                    } else if (!resultIds.contains(idFeature)) {
                        resultIds.add(idFeature)
                        resultFeatures!!.add(fileFeature)
                    }
                }
            }
        }
        Files.write(Paths.get(files[0].path), resultFeatures!!.toJSONString().toByteArray())
        println("Merge json reports success.")
        deleteFiles()
        dataForReports
    }

    @get:Throws(IOException::class)
    private val dataForReports: Unit
        get() {
            totalFeatures = resultFeatures!!.size.toLong()
            for (resultFeature in resultFeatures!!) {
                var isFeaturePassed = true
                val feature = resultFeature as JSONObject
                val elements = feature["elements"] as JSONArray
                for (element in elements) {
                    val elementObject = element as JSONObject
                    val allSteps = elementObject["steps"] as JSONArray
                    totalSteps += allSteps.size.toLong()
                    for (step in allSteps) {
                        val stepObject = step as JSONObject
                        val resultObject = stepObject["result"] as JSONObject
                        val status = resultObject["status"] as String
                        var duration: Long = 0
                        if (resultObject.containsKey("duration")) {
                            duration = resultObject["duration"] as Long
                        }
                        when (status) {
                            "passed" -> {
                                stepPassed++
                                passedDuration += duration
                            }
                            "failed" -> {
                                stepFailed++
                                failedDuration += duration
                            }
                            "skipped" -> {
                                stepSkipped++
                                skipDuration += duration
                            }
                            "pending" -> {
                                stepPending++
                                pendingDuration += duration
                            }
                            else -> {
                                stepUndefined++
                                undefineDuration += duration
                            }
                        }
                        totalDuration += duration
                    }
                    if (elementObject["type"] == "scenario") {
                        val steps = elementObject["steps"] as JSONArray
                        var isScenarioPassed = true
                        for (step in steps) {
                            val stepObject = step as JSONObject
                            val resultObject = stepObject["result"] as JSONObject
                            val status = resultObject["status"] as String
                            if (status != "passed" && isScenarioPassed) {
                                isScenarioPassed = false
                            }
                        }
                        if (isScenarioPassed) {
                            scenarioPassed++
                        } else {
                            scenarioFailed++
                            if (isFeaturePassed) {
                                isFeaturePassed = false
                            }
                        }
                    }
                    if (elementObject["type"] == "scenario") {
                        totalScenarios++
                    }
                }
                if (isFeaturePassed) {
                    featurePassed++
                } else {
                    featureFailed++
                }
            }
            generateJsonReport()
        }

    @Throws(IOException::class)
    private fun generateJsonReport() {
        val step = JSONObject()
        step["totalSteps"] = totalSteps
        step["passedStep"] = stepPassed
        step["failedStep"] = stepFailed
        step["skippedStep"] = stepSkipped
        step["pendingStep"] = stepPending
        step["undefinedStep"] = stepUndefined
        val scenario = JSONObject()
        scenario["totalScenarios"] = totalScenarios
        scenario["passedScenario"] = scenarioPassed
        scenario["failedScenario"] = scenarioFailed
        val feature = JSONObject()
        feature["totalFeatures"] = totalFeatures
        feature["passedFeature"] = featurePassed
        feature["failedFeature"] = featureFailed
        val duration = JSONObject()
        duration["totalDuration"] = getFormatDuration(totalDuration)
        duration["passedDuration"] = getFormatDuration(passedDuration)
        duration["failedDuration"] = getFormatDuration(failedDuration)
        duration["skippedDuration"] = getFormatDuration(skipDuration)
        duration["pendingDuration"] = getFormatDuration(pendingDuration)
        duration["undefinedDuration"] = getFormatDuration(undefineDuration)
        val report = JSONObject()
        report["features"] = feature
        report["scenarios"] = scenario
        report["steps"] = step
        report["durations"] = duration
        Files.write(Paths.get("target/GitHubReport.json"), report.toJSONString().toByteArray())
        println("Generate cucumber report for github success.")
    }

    private fun getFormatDuration(nanoSecond: Long): String {
        val milliSecond = nanoSecond / TIME_NANO_SECOND
        return DurationFormatUtils.formatDuration(milliSecond, "HH:mm:ss.SSS")
    }

    @Throws(IOException::class, ParseException::class)
    private fun getFeatures(file: FileReader): JSONArray {
        val parser = JSONParser()
        val `object` = parser.parse(file)
        return `object` as JSONArray
    }

    private fun deleteFiles() {
        for (i in 1 until files.size) {
            files[i].delete()
        }
    }
}
