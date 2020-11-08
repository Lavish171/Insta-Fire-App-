package com.example.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

private const val TAG="LoginActivity"
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val auth=FirebaseAuth.getInstance()
        /*if(auth!=null)
        {
            goPostsActivity()
        }*/
        //checking if the user already exists or not,if the user exist ie registered,
        //no need to login agains

        btnLogin.setOnClickListener {
            btnLogin.isEnabled=false
            val email=etEmail.text.toString()
            val password=etPassword.text.toString()
            if(email.isBlank() || password.isBlank())
            {
                Toast.makeText(this,"Email/Password cannot be empty",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else
            {
                //connect to the firebase
                auth.signInWithEmailAndPassword(email,password).
                        addOnCompleteListener {task ->
                            btnLogin.isEnabled=false
                           if(task.isSuccessful)
                           {
                               Toast.makeText(this,"Sucess!",Toast.LENGTH_SHORT).show()
                               goPostsActivity()
                           }
                           else
                           {
                               Log.i(TAG,"SignWithEmail-Password Failed",task.exception)
                               Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show()
                           }
                        }
            }
        }
    }

    private fun goPostsActivity() {
        Log.i(TAG,"goPostsActivity")
        val intent=Intent(this,PostsActivity::class.java)
        startActivity(intent)
        finish()//when the user will press the back button,then user will not reach back to the login screen
        //it will directly exit
    }
}

// .limit(20)
//.orderBy("creation_time_ms",Query.Direction.DESCENDING)

//there are basically two methods to fetch the data from
//cloud firestore,one is snapshot listener and other one is
//get method which is used to get the collection