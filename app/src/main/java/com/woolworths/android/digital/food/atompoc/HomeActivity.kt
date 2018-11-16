package com.woolworths.android.digital.food.atompoc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.woolworths.android.digital.food.atompoc.R.id.fab
import com.woolworths.android.digital.food.atompoc.R.id.textView

import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)
        val response = intent.extras.getString("DATA")
        Log.d("HomeActivity", "Response " + response)

        textView.text = response
        fab.isEnabled = false
        fab.setOnClickListener { view ->
            startNextActivity()
        }

        Handler().postDelayed({
            fab.isEnabled = true
        }, 3000);
    }

    fun startNextActivity(){
        startActivity(Intent(this, TapOffActivity::class.java))
        finish()
    }
}
