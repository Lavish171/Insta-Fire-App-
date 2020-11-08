package com.example.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : PostsActivity() {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menu_logout)
        {
            Toast.makeText(this,"User wants to logout",Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
                //  val intent=
            startActivity(Intent(this,LoginActivity::class.java))

        }
        return super.onOptionsItemSelected(item)
    }
}