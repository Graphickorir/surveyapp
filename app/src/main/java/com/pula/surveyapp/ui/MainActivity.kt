package com.pula.surveyapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.pula.surveyapp.R
import com.pula.surveyapp.databinding.ActivityMainBinding
import com.pula.surveyapp.room.AppDb
import com.pula.surveyapp.room.Que
import com.pula.surveyapp.worker.MyWorker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDb
    private var ques: List<Que>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDb(application)
        GlobalScope.launch {
            ques = db.QueDao().getAllQuestions()
            setupViews()
        }
    }

    private fun startWorker(){
        GlobalScope.launch {
            ques = db.QueDao().getAllQuestions()
        }

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString("q1", ques!![0].answer)
            .putString("q2", ques!![1].answer)
            .putString("q3", ques!![2].answer)
            .build()

        val request = PeriodicWorkRequest.Builder( MyWorker::class.java,15, TimeUnit.MINUTES)
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(request)
    }

    private fun setupViews() {
        binding.tvQue.text = ques!![0].question_text
        binding.tvQue1.text = ques!![1].question_text
        binding.tvQue2.text = ques!![2].question_text

        try {
            val arr = JSONArray(ques!!.get(1).options)

            for (i in 0 until arr.length()) {
                val rdbtn = RadioButton(this)
                rdbtn.id = View.generateViewId()
                rdbtn.text = arr.getJSONObject(i).getString("value")
                binding.rdGender.addView(rdbtn)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun onClick(v: View) {
        if(v.id == R.id.next){
            if(binding.vpHome.displayedChild == 0) {
                if(binding.etFirstName.text.toString().isEmpty() && binding.etLastName.text.toString().isEmpty()){
                    Toast.makeText(this,"fill all fields",Toast.LENGTH_SHORT).show()
                    return
                }
                binding.vpHome.displayedChild = 1
                binding.siIndicator.currentStep = 1
                val updatedque = ques!![0]
                updatedque.answer = "${binding.etFirstName.text} ${binding.etLastName.text}"

                GlobalScope.launch {
                    db.QueDao().update(updatedque)
                }
            }
            else if (binding.vpHome.displayedChild == 1){
                if(binding.rdGender.checkedRadioButtonId == -1){
                    Toast.makeText(this,"Pick one option",Toast.LENGTH_SHORT).show()
                    return
                }
                binding.vpHome.displayedChild = 2
                binding.siIndicator.currentStep = 2
                val updatedque = ques!![1]
                val radioButton = findViewById<RadioButton>(binding.rdGender.checkedRadioButtonId)
                updatedque.answer = radioButton.text.toString()

                GlobalScope.launch {
                    db.QueDao().update(updatedque)
                }
                binding.next.text = "Finish"
            }
            else if (binding.vpHome.displayedChild == 2){
                if(binding.etSize.text.toString().isEmpty()){
                    Toast.makeText(this,"fill all fields",Toast.LENGTH_SHORT).show()
                    return
                }
                val size = binding.etSize.text.toString().toFloat()
                val updatedque = ques!![2]
                updatedque.answer = size.toString()

                GlobalScope.launch {
                    db.QueDao().update(updatedque)
                }

                startWorker()
                binding.next.text = "Next"

                binding.etFirstName.text.clear()
                binding.etLastName.text.clear()
                binding.etSize.text.clear()
                binding.rdGender.clearCheck()

                binding.vpHome.displayedChild = 0
                binding.siIndicator.currentStep = 0
                Toast.makeText(this,"Data saved",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if(binding.vpHome.displayedChild == 0) {
            super.onBackPressed()
        }else if (binding.vpHome.displayedChild == 1){
            binding.vpHome.displayedChild = 0
            binding.siIndicator.currentStep = 0
        }else if (binding.vpHome.displayedChild == 2){
            binding.next.text = "Next"
            binding.vpHome.displayedChild = 1
            binding.siIndicator.currentStep = 1
        }
    }
}
