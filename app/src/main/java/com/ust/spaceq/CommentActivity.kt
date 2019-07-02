package com.ust.spaceq

import android.annotation.TargetApi
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.comment_recycle_adapt.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var commsReference: DatabaseReference
private lateinit var seviyelvl:String

@TargetApi(Build.VERSION_CODES.O)
class CommentActivity : AppCompatActivity() {
    val TAG = "CommentActivity"
    val now = LocalDateTime.now()
    var date = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var time = DateTimeFormatter.ofPattern("HH:mm:ss")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")
        commsReference = database.reference.child("Posts")
        seviyelvl = intent.getStringExtra("seviye")

//        val adapter = GroupAdapter<ViewHolder>()
//
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//
//        recycleComment.adapter = adapter

        fetchComments()
    }

    private fun fetchComments(){
        val ref  = database.getReference("/Posts/$seviyelvl")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    Log.d(TAG, it.toString())
                    val post = it.getValue(Posts::class.java)
                    if (post != null){
                        adapter.add(UserItem(post))
                    }
                }

                recycleComment.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    fun buttComms(view: View?){
        Log.d(TAG, "comment sent")
        val user = auth.currentUser
        val post = textCom.text.toString()
        val nickName = tvName.text.toString()
        val lvlReference = commsReference.child(seviyelvl)
        var date = date.format(now)
        var time = time.format(now)
        val giverReference = lvlReference.child(user!!.uid+"D:"+date+"T:"+time)
        giverReference.child("post").setValue(post)
        giverReference.child("nickName").setValue(nickName)
        giverReference.child("date").setValue(date)
        giverReference.child("time").setValue(time)
    }
}

class UserItem(val Post: Posts): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tvComment.text = Post.post
        viewHolder.itemView.tvName.text = Post.nickName
        viewHolder.itemView.tvDate.text = Post.date +" | "+ Post.time
        //will be called in our list for each user comment later on..
    }
    override fun getLayout(): Int {
        return R.layout.comment_recycle_adapt
    }
}

class Posts(val nickName: String, val date: String, val time: String,val post: String){
    constructor():this("","", "", "")
}
