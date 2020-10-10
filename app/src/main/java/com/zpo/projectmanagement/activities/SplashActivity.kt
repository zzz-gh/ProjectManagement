package com.zpo.projectmanagement.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.zpo.projectmanagement.Firebase.FireStoreClass
import com.zpo.projectmanagement.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
//        val typeFace = Typeface.createFromAsset(assets,"DMSerifText-Italic.ttf")
//        tv_app_name.typeface = typeFace

        Handler().postDelayed({
            val currentUserId = FireStoreClass().getCurrentUserId()
            if(currentUserId != null){

                startActivity(
                    Intent(this,
                    MainActivity::class.java)
                )
            }else{
                startActivity(
                    Intent(this,
                    IntroActivity::class.java)
                )
            }

            finish()
        },2500)

    }
}