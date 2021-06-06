package com.broto.projtracker.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import com.broto.projtracker.R
import com.broto.projtracker.firebase.FireStoreClass
import com.broto.projtracker.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signin.*

class SigninActivity : BaseActivity() {

    private val TAG = "SigninActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setUpActionBar()
        auth = FirebaseAuth.getInstance()

        btn_sign_in.setOnClickListener {
            signInUser()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_sign_in_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_24dp)

        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun validateForm(email: String, password: String): Boolean {

        if (TextUtils.isEmpty(email)) {
            showErrorSnackBar("Please Enter email")
            return false
        }
        if (TextUtils.isEmpty(password)) {
            showErrorSnackBar("Please Enter password")
            return false
        }

        return true
    }

    private fun signInUser() {
        val email = et_sign_in_email.text.toString().trim()
        val password = et_sign_in_password.text.toString()

        hideKeyboard()

        if (!validateForm(email, password)) {
            return
        }

        showProgressDialog(getString(R.string.please_wait))

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d(TAG, "Sign In success ${user?.email}")
                FireStoreClass.getInstance().getSignInUserData(this)

            } else {
                et_sign_in_email.setText("")
                et_sign_in_password.setText("")
                showErrorSnackBar("Sign In failed")
                Log.d(TAG, "Sign In Failed. Error: ${task.exception?.message?:""}")
                hideProgressDialog()
            }
        }

    }

    fun userLoginSuccess(user: User?) {
        hideProgressDialog()

        if (user == null) {
            et_sign_in_email.setText("")
            et_sign_in_password.setText("")
            Log.e(TAG, "User instance is null")
            finish()
            return
        }

        startActivity(Intent(this@SigninActivity, MainActivity::class.java))
        finish()

    }

    fun userLoginFailed() {
        et_sign_in_email.setText("")
        et_sign_in_password.setText("")
        hideProgressDialog()
        showErrorSnackBar("SignIn Failed!!")
    }
}