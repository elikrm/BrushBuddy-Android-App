package com.elnaz.brushbuddy.utils
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult

class UserAuth {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    //signUp method
    fun signUp(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        Log.d("UserAuth", "User created: ${user.uid}")
                    } else {
                        Log.e("UserAuth", "Signed up failed:${task.exception?.message}")
                    }
                    callback(task.isSuccessful)
                }
            }
    }
    //sign in method
    fun signIn(email: String?, password: String?, listener: OnCompleteListener<AuthResult>){
        if(email.isNullOrBlank()||password.isNullOrBlank()){
            Log.e("UserAuth", "Invalid email or password")
            return
        }
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(listener)
    }
}