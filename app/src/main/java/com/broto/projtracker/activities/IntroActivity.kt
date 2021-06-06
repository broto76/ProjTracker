package com.broto.projtracker.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.broto.projtracker.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        btn_sign_up_intro.setOnClickListener {
            val intent = Intent(this@IntroActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        btn_sign_in_intro.setOnClickListener {
            val intent = Intent(this@IntroActivity, SigninActivity::class.java)
            startActivity(intent)
        }

    }
}