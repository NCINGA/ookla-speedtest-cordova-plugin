package com.ncinga.speedtest

import org.apache.cordova.*
import org.json.JSONArray
import com.ookla.speedtest.sdk.SpeedtestSDK

class SpeedTest : CordovaPlugin() {

    override fun execute(action: String, args: JSONArray, callbackContext: CallbackContext): Boolean {
        return when (action) {
            "coolMethod" -> {
                val message = args.getString(0)
                val application = cordova.activity.application

                cordova.activity.runOnUiThread {
                    try {
                        // Initialize the SpeedtestSDK on the main thread
                        SpeedtestSDK.initSDK(application, "wrqgslqcvhmjzmpt")
                        callbackContext.success("SpeedtestSDK initialized successfully")
                    } catch (e: Exception) {
                        callbackContext.error("Error initializing SpeedtestSDK: ${e.message}")
                    }
                }
                true
            }
            else -> false
        }
    }
}
