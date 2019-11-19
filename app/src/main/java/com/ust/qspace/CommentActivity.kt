package com.ust.qspace

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast.*
import androidx.core.graphics.BitmapCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.ust.qspace.models.Post
import com.ust.qspace.models.RecyclerItemClickListenr
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.comment_recycle_adapt.view.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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
    lateinit var now:Date
    lateinit var date:String
    lateinit var time:String
    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        mAdView = findViewById(R.id.adViewComment)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        now = getCurrentDateTime()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")
        commsReference = database.reference.child("Posts")
        levelKey = intent.getStringExtra("levelKey")
        tvNick = intent.getStringExtra("tvName")

        levelAdapt(levelKey)
        fetchComments()

        val window = PopupWindow(this)
        val show = layoutInflater.inflate(R.layout.layout_popup_delete, null)
        window.isOutsideTouchable = true
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        recycleComment.addOnItemTouchListener(RecyclerItemClickListenr(this, recycleComment, object : RecyclerItemClickListenr.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val toast = makeText(baseContext, getString(R.string.touch_longer), LENGTH_SHORT)
                toast.setGravity(Gravity.TOP, 0, 100)
                toast.show()
            }
            //Player detail popup
            override fun onItemDoubleTap(view: View?, position: Int) {
                Log.d(TAG, view?.tvName?.text.toString() + "Comment double tapped")
                val player = view?.tvName?.text.toString()
                val playerRef = database.getReference("/Users")
                playerRef.addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {}
                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(p0: DataSnapshot) {
                        p0.children.forEach {
                            val play = it.child("nickName").value as String
                            if (play == player){
                                val playerLevel = it.child("level").value as String
                                val playerPoints = it.child("points").value as Long
                                val playerUps = if (it.child("upCount").exists()){
                                    it.child("upCount").value as Long
                                }else{
                                    0
                                }
                                val playerQuote = if (it.child("userQuote").exists()){
                                     it.child("userQuote").value as String
                                }else{
                                    "Something but fish yet"
                                }
                                val ushow = layoutInflater.inflate(R.layout.layout_popup_player, null)
                                window.contentView = ushow
                                window.showAtLocation(view,1,0,0)
                                ushow.startAnimation(fadein)
                                val playername = ushow.findViewById<TextView>(R.id.tv_player_name)
                                val playerquote = ushow.findViewById<TextView>(R.id.tv_player_quote)
                                val playerlevel = ushow.findViewById<TextView>(R.id.tv_player_level)
                                val playerpoints = ushow.findViewById<TextView>(R.id.tv_player_points)
                                val playerups = ushow.findViewById<TextView>(R.id.tv_player_ups)
                                playername.text = player
                                playerquote.text = getString(R.string.player_quote, playerQuote)
                                playerlevel.text = getString(R.string.player_level, playerLevel)
                                playerpoints.text = getString(R.string.player_points, playerPoints)
                                playerups.text = getString(R.string.player_ups, playerUps)
                            }else{
                                Log.d(TAG, "$play passed")
                            }
                        }
                    }
                })
            }
            //Comment Upvote/Remove Upvote/Delete Own Comment
            override fun onItemLongClick(view: View?, position: Int) {
                val comm = view?.tvComment?.text.toString()
                Log.d(TAG, "onItemLongClick; Upvote pressed!!!")
                val ref  = database.getReference("/Posts/$levelKey")

                ref.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {}
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
                                val votedRef = database.getReference("Users/$postUid")

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
                                        val toast = makeText(baseContext, getString(R.string.own_comment), LENGTH_SHORT)
                                        toast.setGravity(Gravity.CENTER, 0, 0)
                                        toast.show()
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
                                        val toast = makeText(baseContext, getString(R.string.upvote_back), LENGTH_SHORT)
                                        toast.setGravity(Gravity.CENTER, 0, 0)
                                        toast.show()
                                        //Mirroring remove upvote to the other side
                                        votedRef.addListenerForSingleValueEvent(object:ValueEventListener{
                                            override fun onCancelled(p0: DatabaseError) {}
                                            override fun onDataChange(p0: DataSnapshot) {
                                                if(p0.child("upCount").exists()){
                                                    var uvCount = p0.child("upCount").value as Long
                                                    Log.d(TAG, "$postUid UpVote points= $uvCount")
                                                    votedRef.child("upCount").setValue(uvCount-1)
                                                    val prePoints = p0.child("points").value as Long
                                                    val postPoints = prePoints - 10
                                                    votedRef.child("points").setValue(postPoints)
                                                }
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
                                                Log.d(TAG, "$postUid UpVote points= $uvCount")
                                                votedRef.child("upCount").setValue(uvCount+1)
                                            }else{
                                                votedRef.child("upCount").setValue(1)
                                            }
                                            val prePoints = p0.child("points").value as Long
                                            val postPoints = prePoints + 10
                                            votedRef.child("points").setValue(postPoints)
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
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            if(view != null){
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
            var user = auth.currentUser
            val post = textCom.text.toString()
            var nickName = tvNick
            var lvlReference = commsReference.child(levelKey)
            date = now.turnToString("yyyy-MM-dd")
            time = now.turnToString("HH:mm:ss")
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
            val toast = makeText(baseContext, getString(R.string.minimum_letters), LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
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
                val toast = makeText(baseContext, getString(R.string.listener_cancelled), LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        })
    }

    private fun levelAdapt(level: String) {
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        when (level) {
            "Stage 1" -> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage1.png?alt=media&token=af22cfdc-27c3-46d0-9583-ee0ccabba881").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 2" -> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage2.png?alt=media&token=8ca7402b-48a5-4bdc-b0e6-ddd6112d1caa").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 4" -> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage4.png?alt=media&token=9da5036a-1f2a-4ad8-9e0b-4a31748e9cdd").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 5" -> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage5.png?alt=media&token=fe278d9d-b1b1-4d9c-8b73-3ff7e5e6a465").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 6" -> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage6.png?alt=media&token=085ae9c3-e1b9-4038-b214-32ec224f8b32").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 7" -> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage7.png?alt=media&token=4eba51d5-ffb8-4e67-9c87-3e9af2ee9c61").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 9" -> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage9.png?alt=media&token=8facb0bb-9b2a-44a0-8684-3fa6629b3afd").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 10"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage10.png?alt=media&token=04c3733e-faa5-48a5-bfcc-9906d1a92f1b").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 11"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage11.png?alt=media&token=a165710c-76cc-4bc9-86e8-d49a06cbe193").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 12"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage12.png?alt=media&token=19dc2561-6d78-4568-a093-75f5a736ad00").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 13"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage13.png?alt=media&token=7108ec0d-c027-4f3f-adbf-6b4199590ad5").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 14"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage14.png?alt=media&token=4da2ef06-3fe3-45cb-8635-8bcf5a3a3bd6").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 15"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage15.png?alt=media&token=8adea02c-8bd1-462b-a966-e7f926f084d1").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 16"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage16.png?alt=media&token=9b02b3eb-fd73-4a0c-a5a7-0d06e5d35573").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 17"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage17.png?alt=media&token=0e1120d0-830b-425a-a330-4374001fcbe9").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 18"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage18.png?alt=media&token=11c022ad-1e9b-4b10-8a64-c2a26241ab05").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 19"-> {
                /*var imageUri = Uri.parse(
                    MediaStore.Images.Media.insertImage(this.contentResolver, BitmapFactory.decodeResource(resources, R.drawable.ic_table), null, null)
                )
                Picasso.get().load(imageUri).into(qSummary)
                qSummary.startAnimation(fadein)*/
                qSummary.visibility = View.GONE
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 20"-> {
                qSummary.visibility = View.GONE
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 21"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage21.png?alt=media&token=84edde33-c653-4edb-808e-98024b46d9c8").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 22"-> {
                qSummary.visibility = View.GONE
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 24"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage24.png?alt=media&token=cc2061a4-fb35-416f-bad5-bdf68822d2a0").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 25"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage25.png?alt=media&token=04239edf-80f1-4dae-8dfb-9a3808ba1251").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 26"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage26.png?alt=media&token=75a28271-effb-459a-9b60-c671a0f93db6").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 27"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage27.png?alt=media&token=d7e9792f-8f85-4f99-a599-0888e84da2e3").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 28"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage28.png?alt=media&token=956ec2f1-2dd1-4763-93b1-3285f557ea5c").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 29"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage29.png?alt=media&token=7ee11bc7-ca2e-4d0f-9fa4-a828bb5ba109").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 30"-> {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/stages%2Fstage30.png?alt=media&token=ea102fcb-e600-4c7e-b9c9-8718383cd024").into(qSummary)
                qSummary.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            else -> {
                qSummary.visibility = View.GONE
                Log.d(TAG, "Something's Wrong; levelAdapt is failed!")
            }
        }
    }

    fun Date.turnToString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    override fun onBackPressed(){
        this@CommentActivity.finish()
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

        /*userName.text  = userReference.orderByChild("nickName").turnToString()*/
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
