@file:Suppress("DEPRECATION")

package com.example.taskflow.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.os.Handler
import com.example.taskflow.R
import com.example.taskflow.firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val typeFace: Typeface = Typeface.createFromAsset(assets, "blackfont.TTF")
        tv_app_name.typeface = typeFace

        Handler().postDelayed({
            var currentUserId= FirestoreClass().getCurrentUserId()
            if(currentUserId.isNotEmpty()){
                startActivity(Intent(this,
                    MainActivity::class.java))
            }else{
                startActivity(Intent(this,
                    IntroActivity::class.java))
            }


            finish()
        }, 2500)
    }
}