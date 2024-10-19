package com.ncinga.speedtest

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

class CustomTestHandler(
    private val speedtestSDK: SpeedtestSDK,
    private val configName: String,
    private val callbackContext: CallbackContext
) : TestHandlerBase() {
    private var taskManager: TaskManager? = null
    private val TAG = "SpeedTest"
    private var retryCount = 0
    private val maxRetries = 3
    val testResult: MutableMap<String, Any> = mutableMapOf()

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
                        testResult["uploadSpeed"] = uploadResult
                        sendSuccessResultToCallback(testResult)
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

                    override fun onTestFailed(
                        error: OoklaError,
                        speedtestResult: SpeedtestResult?
                    ) {
                        super.onTestFailed(error, speedtestResult)
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

    fun sendSuccessResultToCallback(result: MutableMap<String, Any>) {
        val sanitizedResult = result.mapValues { it.value ?: JSONObject.NULL }
        val jsonResult = JSONObject(sanitizedResult)
        callbackContext.success(jsonResult)
    }
}
