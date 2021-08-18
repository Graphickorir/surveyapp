package com.pula.surveyapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.pula.surveyapp.databinding.ActivitySplashBinding
import com.pula.surveyapp.room.AppDb
import com.pula.surveyapp.room.Que
import com.pula.surveyapp.room.QueDao
import com.saspacity.android.networking.MySingleton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    //private lateinit var _quedao: QueDao
    private lateinit var db: AppDb

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)

        db = AppDb(application)

        GlobalScope.launch {
            val data = db.QueDao().getAllQuestions()

            if(data.isNotEmpty()){
                go()
            }else{
                loadQuestions()
            }
        }
    }

    private fun go(){
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun loadQuestions(){
        binding.pb.visibility = View.VISIBLE
        val sRequest: StringRequest = object : StringRequest(Method.GET,
            "https://run.mocky.io/v3/d628facc-ec18-431d-a8fc-9c096e00709a",
            Response.Listener { response: String ->
                binding.pb.visibility = View.INVISIBLE
                Log.e("load_questions ", response)
                try {
                    val jobject = JSONObject(response)
                    val arr = jobject.getJSONArray("questions")
                    val obj = jobject.getJSONObject("strings").getJSONObject("en")

                    for (i in 0 until arr.length()) {
                        val que = arr.getJSONObject(i)
                        var options = que.getJSONArray("options").toString()
                        if(que.getJSONArray("options").length() == 0)
                            options = "null"

                        GlobalScope.launch {
                            db.QueDao().insert(
                                Que(
                                    que.getString("id"),
                                    que.getString("question_text"),
                                    obj.getString(que.getString("question_text")),
                                    options,
                                    "null"
                                )
                            )
                        }
                    }

                    go()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError ->
                binding.pb.visibility = View.INVISIBLE
                Log.i("load_questions ", "onError: $error")
            })
        {
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["App"] = "survey app"
                return params
            }
        }

        MySingleton.getInstance(this).addToRequestQueue(sRequest)
    }

}