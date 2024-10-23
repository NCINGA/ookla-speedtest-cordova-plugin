package com.ncinga.speedtest

import android.content.Context
import android.os.Environment
import com.ookla.speedtest.sdk.ConfigHandlerBase
import com.ookla.speedtest.sdk.MainThreadConfigHandler
import com.ookla.speedtest.sdk.SpeedtestResult
import com.ookla.speedtest.sdk.SpeedtestSDK
import com.ookla.speedtest.sdk.TaskManager
import com.ookla.speedtest.sdk.config.Config
import com.ookla.speedtest.sdk.config.ValidatedConfig
import com.ookla.speedtest.sdk.handler.TaskManagerController
import com.ookla.speedtest.sdk.handler.TestHandlerBase
import com.ookla.speedtest.sdk.model.LatencyResult
import com.ookla.speedtest.sdk.model.TransferResult
import com.ookla.speedtest.sdk.result.OoklaError
import android.util.Log
import org.apache.cordova.CallbackContext
import org.json.JSONObject
import java.io.File

class CustomTestHandler(
    private val speedtestSDK: SpeedtestSDK,
    private val configName: String,
    private val callbackContext: CallbackContext,
    private val context: Context
) : TestHandlerBase() {
    private var taskManager: TaskManager? = null
    private val TAG = "SpeedTest"
    private var retryCount = 0
    private val maxRetries = 3
    private val testResult: MutableMap<String, Any> = mutableMapOf()

    private var fileHandler: FileHandler? = null;
    private lateinit var filePath: File;

    fun runHttpGetTest() {
        val config = Config.newConfig(configName)
        val configHandler = object : ConfigHandlerBase() {
            override fun onConfigFetchFinished(validatedConfig: ValidatedConfig?) {
                val handler = object : TestHandlerBase() {
                    override fun onLatencyFinished(
                        taskController: TaskManagerController?,
                        result: LatencyResult
                    ) {
                        super.onLatencyFinished(taskController, result)
                        Log.d(TAG, "Latency Result: ${result}")

                        val latencyResult: MutableMap<String, Any> = mutableMapOf()
                        latencyResult["latencyMillis"] = result.latencyMillis
                        latencyResult["jitterMillis"] = result.jitterMillis
                        testResult["latency"] = latencyResult
                        taskManager?.startNextStage()
                    }

                    override fun onUploadFinished(
                        taskController: TaskManagerController?,
                        result: TransferResult
                    ) {
                        super.onUploadFinished(taskController, result)
                        Log.d(TAG, "Upload Speed: ${result}")
                        val uploadResult: MutableMap<String, Any> = mutableMapOf()
                        uploadResult["bytes"] = result.bytes
                        uploadResult["speedMbps"] = result.speedMbps
                        uploadResult["durationMillis"] = result.durationMillis
                        testResult["uploadResult"] = uploadResult
                        taskManager?.startNextStage()
                    }

                    override fun onDownloadFinished(
                        taskController: TaskManagerController?,
                        result: TransferResult
                    ) {
                        super.onDownloadFinished(taskController, result)

                        Log.d(TAG, "Download Speed: ${result}")
                        val downloadResult: MutableMap<String, Any> = mutableMapOf()
                        downloadResult["bytes"] = result.bytes
                        downloadResult["speedMbps"] = result.speedMbps
                        downloadResult["durationMillis"] = result.durationMillis
                        testResult["downloadSpeed"] = downloadResult
                        taskManager?.startNextStage()
                    }

                    override fun onTestFinished(speedtestResult: SpeedtestResult) {
                        super.onTestFinished(speedtestResult)
                        sendSuccessResultToCallback(testResult)
                    }

                    override fun onTestFailed(
                        error: OoklaError,
                        speedtestResult: SpeedtestResult?
                    ) {
                        super.onTestFailed(error, speedtestResult)
                        Log.e(TAG, error.message)
                        if (retryCount < maxRetries) {
                            retryCount++
                            Log.d(TAG, "Retrying test... attempt $retryCount")
                            taskManager?.startNextStage()
                        } else {
                            Log.e(TAG, "Test failed after $maxRetries attempts: ${error.message}")
                            callbackContext.error("Test failed: ${error.message}")
                        }
                    }
                }

                taskManager = speedtestSDK.newTaskManager(handler, validatedConfig)
                taskManager?.start()
            }

            override fun onConfigFetchFailed(error: OoklaError) {
                Log.e(TAG, "Config fetch failed with: ${error.message}")
                testResult["onConfigFetchFailed"] = error.message
                callbackContext.error("Config fetch failed: ${error.message}")
            }
        }
        ValidatedConfig.validate(config, MainThreadConfigHandler(configHandler))
    }

    fun runHttpGetTestAndSaveJson(count: Int, currentIteration: Int = 1, finalResult: MutableMap<String, Any> = mutableMapOf()) {
        if (currentIteration > count) {
            val callbackResult = JSONObject(finalResult.toString())
            Log.i("Final Result", finalResult.toString())
            callbackContext.success(callbackResult)
            return
        }

        Log.i(TAG, "loop $currentIteration")
        Log.i(TAG, "execute runHttpGetTestAndSaveJson()")
        val config = Config.newConfig(configName)

        val configHandler = object : ConfigHandlerBase() {
            override fun onConfigFetchFinished(validatedConfig: ValidatedConfig?) {
                val handler = object : TestHandlerBase() {
                    override fun onLatencyFinished(
                        taskController: TaskManagerController?,
                        result: LatencyResult
                    ) {
                        super.onLatencyFinished(taskController, result)
                        Log.d(TAG, "Latency Result: ${result}")

                        val latencyResult: MutableMap<String, Any> = mutableMapOf()
                        latencyResult["latencyMillis"] = result.latencyMillis
                        latencyResult["jitterMillis"] = result.jitterMillis
                        testResult["latency"] = latencyResult
                        taskManager?.startNextStage()
                    }

                    override fun onUploadFinished(
                        taskController: TaskManagerController?,
                        result: TransferResult
                    ) {
                        super.onUploadFinished(taskController, result)
                        Log.d(TAG, "Upload Speed: ${result}")
                        val uploadResult: MutableMap<String, Any> = mutableMapOf()
                        uploadResult["bytes"] = result.bytes
                        uploadResult["speedMbps"] = result.speedMbps
                        uploadResult["durationMillis"] = result.durationMillis
                        taskManager?.startNextStage()
                    }

                    override fun onDownloadFinished(
                        taskController: TaskManagerController?,
                        result: TransferResult
                    ) {
                        super.onDownloadFinished(taskController, result)
                        Log.d(TAG, "Download Speed: ${result}")
                        val downloadResult: MutableMap<String, Any> = mutableMapOf()
                        downloadResult["bytes"] = result.bytes
                        downloadResult["speedMbps"] = result.speedMbps
                        downloadResult["durationMillis"] = result.durationMillis
                        testResult["downloadSpeed"] = downloadResult
                        taskManager?.startNextStage()
                    }

                    override fun onTestFinished(speedtestResult: SpeedtestResult) {
                        super.onTestFinished(speedtestResult)
                        fileHandler = FileHandler(context)
                        val result = speedtestResult.resultObj.toJsonString()
                        fileHandler!!.writeFile(result, currentIteration.toString())

                        finalResult["loop${currentIteration.toString()}"] = result
                        runHttpGetTestAndSaveJson(count, currentIteration + 1, finalResult)
                    }

                    override fun onTestFailed(
                        error: OoklaError,
                        speedtestResult: SpeedtestResult?
                    ) {
                        super.onTestFailed(error, speedtestResult)
                        Log.e(TAG, error.message)
                        taskManager?.startNextStage()
                        runHttpGetTestAndSaveJson(count, currentIteration + 1, finalResult)
                    }
                }

                taskManager = speedtestSDK.newTaskManager(handler, validatedConfig)
                taskManager?.start()
            }

            override fun onConfigFetchFailed(error: OoklaError) {
                Log.e(TAG, "Config fetch failed with: ${error.message}")
                testResult["onConfigFetchFailed"] = error.message
                callbackContext.error("Config fetch failed: ${error.message}")

                runHttpGetTestAndSaveJson(count, currentIteration + 1, finalResult)
            }
        }

        ValidatedConfig.validate(config, MainThreadConfigHandler(configHandler))
    }



    fun sendSuccessResultToCallback(result: MutableMap<String, Any>) {
        val sanitizedResult = result.mapValues { it.value ?: JSONObject.NULL }
        val jsonResult = JSONObject(sanitizedResult)
        callbackContext.success(jsonResult)
    }
}
