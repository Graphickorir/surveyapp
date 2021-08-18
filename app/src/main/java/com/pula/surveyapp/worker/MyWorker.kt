package com.pula.surveyapp.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.saspacity.android.networking.MySingleton

class MyWorker (private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        uploadUserData()
        return Result.success()
    }

    private fun uploadUserData() {
        val sRequest: StringRequest = object : StringRequest(
            Method.POST,
            "https://webhook.site/29975d68-6a80-4725-a35a-46fa04c98640",
            Response.Listener {
                Log.e("Sent Data", "done")
            },
            Response.ErrorListener { error: VolleyError ->
                Log.e("load_questions ", "onError: $error")
            })
        {
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["q1"] = inputData.getString("q1").toString()
                params["q2"] = inputData.getString("q2").toString()
                params["q3"] = inputData.getString("q3").toString()
                return params
            }
        }

        MySingleton.getInstance(context).addToRequestQueue(sRequest)
    }
}