package com.ust.spaceq

import android.annotation.TargetApi
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.PopupWindow
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.ust.spaceq.models.Post
import com.ust.spaceq.models.RecyclerItemClickListenr
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

        fetchComments()

        val window = PopupWindow(this)
        val show = layoutInflater.inflate(R.layout.layout_popup_delete, null)
        window.isOutsideTouchable = true
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        recycleComment.addOnItemTouchListener(RecyclerItemClickListenr(this, recycleComment, object : RecyclerItemClickListenr.OnItemClickListener {

            override fun onItemClick(view: View, position: Int) {

                Toast.makeText(baseContext,getString(R.string.touch_longer),Toast.LENGTH_SHORT).show()
            }

            override fun onItemLongClick(view: View?, position: Int) {
                val comm = view?.tvComment?.text.toString()
                Log.d(TAG, "onItemLongClick; Upvote pressed!!!")
                val ref  = database.getReference("/Posts/$levelKey")

                ref.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        p0.children.forEach {
                            val comms = it.child("post").value as String
                            if (comm == comms){

                                val key = it.key.toString()
                                val itemRef = database.getReference("/Posts/$levelKey/$key")
                                val upvotes = it.child("upvotes").value as MutableList<String>
                                val found = upvotes.contains(uid)
                                var upvoteCount = it.child("upvoteCount").value as Long
                                var ordernum = it.child("order").value as Long
                                val postUid = it.child("uid").value as String
                                val upvotedId = it.child("uid").value as String
                                val votedRef = database.getReference("Users/$upvotedId")

                                if (found){
                                    //Removing Comment
                                    if (postUid == uid){
                                        window.contentView = show
                                        window.showAtLocation(view, 1,0, 100)
                                        show.startAnimation(fadein)
                                        val yes = show.findViewById<Button>(R.id.buttYes)
                                        val no = show.findViewById<Button>(R.id.buttNo)
                                        val thepost = it
                                        yes.setOnClickListener {
                                            thepost.ref.removeValue()
                                            window.dismiss()
                                            fetchComments()
                                        }
                                        no.setOnClickListener {
                                            window.dismiss()
                                        }
                                        Log.d(TAG, "Users' post")
                                        Toast.makeText(baseContext, getString(R.string.own_comment), Toast.LENGTH_SHORT).show()
                                    }else{
                                        //Removing upvote from the specific comment
                                        var count = upvoteCount-1
                                        itemRef.child("upvoteCount").setValue(count)
                                        itemRef.child("order").setValue(ordernum+1)
                                        upvotes.remove(uid)
                                        itemRef.child("upvotes").setValue(upvotes)
                                        view?.tvUpvotes?.setBackgroundColor(Color.TRANSPARENT)
                                        fetchComments()
                                        Log.d(TAG, "$uid found in upvoters, taken back!")
                                        Toast.makeText(baseContext, getString(R.string.upvote_back),Toast.LENGTH_SHORT).show()
                                        //Mirroring upvote to the other side
                                        votedRef.addListenerForSingleValueEvent(object:ValueEventListener{
                                            override fun onCancelled(p0: DatabaseError) {}
                                            override fun onDataChange(p0: DataSnapshot) {
                                                if(p0.child("upCount").exists()){
                                                    var uvCount = p0.child("upCount").value as Long
                                                    Log.d(TAG, "$upvotedId UpVote points= $uvCount")
                                                    votedRef.child("upCount").setValue(uvCount-1)
                                                }else{}
                                            }
                                        })
                                    }

                                }else{
                                    //Upvoting the specific comment
                                    var count = upvoteCount+1
                                    itemRef.child("upvoteCount").setValue(count)
                                    itemRef.child("order").setValue(ordernum-1)
                                    Log.d(TAG, "upvoteCount raised!; key= $key count = $count")
                                    upvotes.add(uid)
                                    if (view != null) {
                                        val hologreen = resources.getColor(R.color.hologreen)
                                        view.tvUpvotes.setBackgroundColor(hologreen)
                                    }
                                    itemRef.child("upvotes").setValue(upvotes)
                                    fetchComments()
                                    Log.d(TAG, "upvotes UPDATED, ${it.child("upvotes").value}")
                                    //Mirroring upvote to the other side
                                    votedRef.addListenerForSingleValueEvent(object:ValueEventListener{
                                        override fun onCancelled(p0: DatabaseError) {}
                                        override fun onDataChange(p0: DataSnapshot) {
                                            if(p0.child("upCount").exists()){
                                                var uvCount = p0.child("upCount").value as Long
                                                Log.d(TAG, "$upvotedId UpVote points= $uvCount")
                                                votedRef.child("upCount").setValue(uvCount+1)
                                            }else{
                                                votedRef.child("upCount").setValue(1)
                                            }
                                        }
                                    })
                                }

                            }else{
                                Log.d(TAG, "Passing Next Comment")
                            }
                        }
                    }

                })
            }
        }))
    }

    fun buttComms(view: View?){
        if (textCom.text.length > 4){
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
            giverReference.child("upvoteCount").setValue(0)
            giverReference.child("upvotes").setValue(listOf(uid))
            giverReference.child("order").setValue(999999)
            Log.d(TAG, "comment sent")
            textCom.text.clear()
            fetchComments()
        }else{
            Toast.makeText(baseContext,getString(R.string.minimum_letters),Toast.LENGTH_SHORT).show()
            Log.d(TAG, "textCom = null")
        }
    }

    private fun fetchComments(){
        val ref  = database.getReference("/Posts/$levelKey")
            .orderByChild("order")
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
                Log.d(TAG, "WARNING: Comment Fetching FAILED!")
                Toast.makeText(baseContext, getString(R.string.listener_cancelled), Toast.LENGTH_LONG).show()
            }
        })
    }
}

class PostItem(val post: Post): Item<ViewHolder>(){
    val TAG = "ComentActivity:PostItem"
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tvComment.text = post.post
        viewHolder.itemView.tvName.text = post.nickName
        viewHolder.itemView.tvDate.text = post.date +" | "+ post.time
        viewHolder.itemView.tvUpvotes.text = "+"+post.upvoteCount
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
                    viewHolder.itemView.tvTag.text = "'${p0.child("level").value as String}'"
                    viewHolder.itemView.tvTag.setTextColor(Color.parseColor("#F29B14"))
                }
                override fun onCancelled(p0: DatabaseError) {}
        })
        val found = post.upvotes.contains(uid)
        val spaceWhite = Color.parseColor("#FBFAF6")
        if (found){
            if (post.uid == uid){
                viewHolder.itemView.tvName.setBackgroundColor(Color.parseColor("#F29B14"))
                viewHolder.itemView.tvName.setTextColor(spaceWhite)
                Log.d(TAG, "User's own comment detected!")
            }else{
                viewHolder.itemView.tvUpvotes.setBackgroundColor(Color.parseColor("#669900"))
                viewHolder.itemView.tvUpvotes.setTextColor(spaceWhite)
            }
        }else{
            Log.d(TAG, "No Upvote")
        }

    }
    override fun getLayout(): Int {
        return R.layout.comment_recycle_adapt
    }
}
