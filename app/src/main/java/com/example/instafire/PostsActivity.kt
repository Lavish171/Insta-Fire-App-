package com.example.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instafire.models.Post
import com.example.instafire.models.User
import com.google.firebase.auth.FirebaseAuth
//import com.example.instafire.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_posts.*
import java.lang.Exception

private  const val TAG="PostsActivity"
  const val EXTRA_USERNAME="EXTRA_USERNAME"
open class PostsActivity : AppCompatActivity() {
    private lateinit var  firestoreDb:FirebaseFirestore
    private lateinit var posts:MutableList<Post>
    private lateinit var adapter: PostsAdapter
    private var signedUser: User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        //create the layout file which represents one post-Done
        //create the data source-Done
        posts= mutableListOf()

        //craete the adapter
        adapter= PostsAdapter(this,posts)
        //Bind the adapter and layout manager to the RV
        rvPosts.adapter=adapter
        rvPosts.layoutManager=LinearLayoutManager(this)

        firestoreDb= FirebaseFirestore.getInstance()
        //edit

       firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener {documentSnapshot ->
                signedUser=documentSnapshot.toObject(User::class.java)
                Log.i(TAG,"Signed In user $signedUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG,"Error In Fetching Signed In User ",exception)
            }

        var postsReference=firestoreDb.collection("posts").
                                    limit(20).
                                    orderBy("creation_time_ms",Query.Direction.DESCENDING)

        val username=intent.getStringExtra(EXTRA_USERNAME)
        if(username!=null)
        {
            supportActionBar?.title=username
           postsReference= postsReference.whereEqualTo("user.username",username)
        }


        postsReference.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            //if the query is not null,then querySnapshot will contain the data
            //else if query is null,exception would not be null
            if(querySnapshot==null || firebaseFirestoreException!=null)
            {
                Log.i(TAG,"Exception when querying posts",firebaseFirestoreException)
                return@addSnapshotListener
            }
                val postList=querySnapshot.toObjects(Post::class.java)
                //as we got some updated data from firestore
                //we will tell the adapter that we have
                //received some updated data
                posts.clear()
                posts.addAll(postList)
                adapter.notifyDataSetChanged()
                //we got some data to show
                for(post in postList) {
                    Log.i(TAG, "Post ${post}")
                }
            /*for(document in querySnapshot.documents)
            {
                Log.i(TAG,"Document ${document.id} : ${document.data}")
            }*/
        }

        fabCreate.setOnClickListener {
            val intent=Intent(this,CreateActivity::class.java)
            startActivity(intent)
        }

         }
        //now reference the menu_post inside of the post activity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //item is the menuItem which will tell us which
        // item in the menu does the user selected
        if(item.itemId==R.id.menu_profile) {
            val intent= Intent(this,ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME,signedUser?.username)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
    }