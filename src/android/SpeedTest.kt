package com.ncinga.speedtest

import android.os.Build
import android.os.Environment
import org.apache.cordova.*
import org.json.JSONArray
import com.ookla.speedtest.sdk.SpeedtestSDK
import android.util.Log
import org.json.JSONObject
import java.io.File

class SpeedTest : CordovaPlugin() {
    private val TAG = "SpeedTest"
    private lateinit var customTestHandler: CustomTestHandler
    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        return when (action) {
            "startTesting" -> {
                val application = cordova.activity.application
                val jsonString = args.getString(0)
                val jsonObject = JSONObject(jsonString)
                var apiKey: String = ""
                var config: String = ""
                Log.d(TAG, "execute() called with action: $action")

                if (jsonObject.has("apiKey") && jsonObject.getString("apiKey").isNotEmpty()) {
                    apiKey = jsonObject.getString("apiKey")
                } else {
                    Log.e(TAG, "API Key is empty")
                    callbackContext.error("API Key is required")
                    return false
                }

                if (jsonObject.has("config") && jsonObject.getString("config").isNotEmpty()) {
                    config = jsonObject.getString("config")
                } else {
                    Log.e(TAG, "Config is not found")
                    callbackContext.error("Config is required")
                    return false
                }

                cordova.activity.runOnUiThread {
                    try {
                        val speedtestSDK = SpeedtestSDK.initSDK(application, apiKey)
                        customTestHandler = CustomTestHandler(
                            speedtestSDK,
                            config,
                            callbackContext,
                            cordova.context

                        )
                        customTestHandler.runHttpGetTest()

                    } catch (e: Exception) {
                        Log.e(TAG, "Error initializing SpeedtestSDK: ${e.message}")
                        callbackContext.error("Error initializing SpeedtestSDK: ${e.message}")
                    }
                }
                true
            }

            "extractJson" -> {
                val application = cordova.activity.application
                val jsonString = args.getString(0)
                val jsonObject = JSONObject(jsonString)
                var apiKey: String = ""
                var config: String = ""
                var count: Int = 1
                Log.d(TAG, "execute() called with action: $action")

                if (jsonObject.has("apiKey") && jsonObject.getString("apiKey").isNotEmpty()) {
                    apiKey = jsonObject.getString("apiKey")
                } else {
                    Log.e(TAG, "API Key is empty")
                    callbackContext.error("API Key is required")
                    return false
                }

                if (jsonObject.has("config") && jsonObject.getString("config").isNotEmpty()) {
                    config = jsonObject.getString("config")
                } else {
                    Log.e(TAG, "Config is not found")
                    callbackContext.error("Config is required")
                    return false
                }
                if (jsonObject.has("count") && jsonObject.getString("count").isNotEmpty()) {
                    count = jsonObject.getInt("count")
                }

                cordova.activity.runOnUiThread {
                    try {
                        val speedtestSDK = SpeedtestSDK.initSDK(application, apiKey)
                        customTestHandler = CustomTestHandler(
                            speedtestSDK,
                            config,
                            callbackContext,
                            cordova.context
                        )
                        Log.i(TAG, "config : $config, count: $count")
                        customTestHandler.runHttpGetTestAndSaveJson(count)

                    } catch (e: Exception) {
                        Log.e(TAG, "Error initializing SpeedtestSDK: ${e.message}")
                        callbackContext.error("Error initializing SpeedtestSDK: ${e.message}")
                    }
                }
                true
            }


            else -> false
        }


    }
}
