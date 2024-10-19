package com.ncinga.speedtest

import org.apache.cordova.*
import org.json.JSONArray
import com.ookla.speedtest.sdk.SpeedtestSDK
import android.util.Log
import org.json.JSONObject

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
                        customTestHandler = CustomTestHandler(speedtestSDK, config, callbackContext)
                        customTestHandler.runHttpGetTest()

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
