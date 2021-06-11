package com.broto.projtracker.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.broto.projtracker.R
import com.broto.projtracker.firebase.FireStoreClass
import com.broto.projtracker.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setUpActionBar()
        btn_sign_up.setOnClickListener {
            registerUser()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_sign_up_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_black_color_24dp)

        toolbar_sign_up_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {

        if (TextUtils.isEmpty(name)) {
            showErrorSnackBar("Please Enter name")
            return false
        }
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

    private fun registerUser() {
        val name = et_name.text.toString().trim()
        val email = et_email.text.toString().trim()
        val password = et_password.text.toString()

        hideKeyboard()

        if (!validateForm(name, email, password)) {
            return
        }

        showProgressDialog(getString(R.string.please_wait))

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user!!

                    val user = User(
                        firebaseUser.uid,
                        name,
                        email
                    )

                    FireStoreClass.getInstance().registerUser(this, user)

                } else {
                    val message = task.exception?.message?:""
                    showErrorSnackBar(message)
                    et_name.setText("")
                    et_email.setText("")
                    et_password.setText("")
                    hideProgressDialog()
                }
            }
    }

    fun userRegisteredSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this@SignupActivity,
            "You have successfully registered",
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    fun userRegisteredFailed() {
        et_name.setText("")
        et_email.setText("")
        et_password.setText("")
        hideProgressDialog()
        showErrorSnackBar("Signup Failed!!")
    }
}