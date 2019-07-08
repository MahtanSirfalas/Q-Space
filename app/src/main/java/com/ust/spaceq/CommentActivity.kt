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
import com.squareup.picasso.Picasso
import com.ust.spaceq.models.Post
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.comment_recycle_adapt.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var commsReference: DatabaseReference
private lateinit var levelKey: String
private lateinit var tvNick: String
private lateinit var avatareach: String

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
        levelKey = intent.getStringExtra("levelKey")
        tvNick = intent.getStringExtra("tvName")

//        val adapter = GroupAdapter<ViewHolder>()
//
//        adapter.add(PostItem())
//        adapter.add(PostItem())
//        adapter.add(PostItem())
//
//        recycleComment.adapter = adapter

        fetchComments()
    }

    fun buttComms(view: View?){
        var user = auth.currentUser
        val post = textCom.text.toString()
        var nickName = tvNick
        var lvlReference = commsReference.child(levelKey)
        var date = date.format(now)
        var time = time.format(now)
        var giverReference = lvlReference.child(user!!.uid+"D:"+date+"T:"+time)
        giverReference.child("post").setValue(post)
        giverReference.child("nickName").setValue(nickName)
        giverReference.child("date").setValue(date)
        giverReference.child("time").setValue(time)
        giverReference.child("uid").setValue(uid)
        Log.d(TAG, "comment sent")
        textCom.text.clear()
    }

    private fun fetchComments(){
        val ref  = database.getReference("/Posts/$levelKey")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    Log.d(TAG, it.toString())
                    val post = it.getValue(Post::class.java)
                    if (post != null){
                        adapter.add(PostItem(post))
                    }
                }

                recycleComment.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class PostItem(val post: Post): Item<ViewHolder>(){
    val TAG = "CommentActivity"
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tvComment.text = post.post
        viewHolder.itemView.tvName.text = post.nickName
        viewHolder.itemView.tvDate.text = post.date +" | "+ post.time
        //will be called in our list for each user comment later on..
        val userId = post.uid
        val userReference = databaseReference.child(userId)
        Log.d(TAG, "class PostItem; userId = $userId assigned")

        /*userName.text  = userReference.orderByChild("nickName").toString()*/
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    Log.d(TAG, "onDataChange")
                    avatareach = p0.child("avatar").value as String
                    Log.d(TAG, "onDataChange; avatareach = $avatareach")
                    Picasso.get().load(avatareach).into(viewHolder.itemView.ivAvatar_circle)
                    Log.d(TAG, "Picasso is successful!")
                }

                override fun onCancelled(p0: DatabaseError) {}
        })

    }
    override fun getLayout(): Int {
        return R.layout.comment_recycle_adapt
    }
}
