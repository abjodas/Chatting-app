package com.example.kotlinmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.user_row_newmessage.view.*

class ChatLogActivity : AppCompatActivity() {
    companion object{
        val TAG = "SEND";
    }
    val adapter = GroupAdapter<ViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chatlog.adapter = adapter


        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = user?.username

       // setupDummyData()
        listenForMessages()
        sendbutton_chatlog.setOnClickListener{

            if(sendmessage_chatlog.text.toString() != "") {
                Log.d(TAG, "Send button clicked")
                performSendMessage()
            }
            sendmessage_chatlog.setText("")
        }
    }
    private fun listenForMessages(){
        val fromid = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toid = user?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromid/$toid")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
               val chatmessage =  snapshot.getValue(ChatMessage::class.java)
                Log.d("supbitch", chatmessage?.text!!)

                if(chatmessage?.text != "") {
                    Log.d(TAG, chatmessage.text!!)
                    if(chatmessage.fromid ==  FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatmessage.text))
                        recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)

                    }
                    else{

                        adapter.add(ChatToItem(chatmessage.text, user!!))
                        recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
        })
    }
    private fun performSendMessage(){
        //Send a message and store it to firebase
        val text = sendmessage_chatlog.text.toString()

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        val fromid = FirebaseAuth.getInstance().uid
        val toid = user?.uid

        // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/${fromid}/${toid}").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/${toid}/${fromid}").push()


        val chatmessage = ChatMessage(reference.key!!, text, fromid!!, toid!!, System.currentTimeMillis() / 1000)
        reference.setValue(chatmessage)
            .addOnSuccessListener {
                Log.d(TAG, "SAVED OUR CHAT MESSAGE id: ${reference.key}")
                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }
        toReference.setValue(chatmessage)

        val latestMessageReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromid/$toid")
        val latestMessageToReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$toid/$fromid")
        latestMessageReference.setValue(chatmessage)
        latestMessageToReference.setValue(chatmessage)
    }

}
class ChatMessage(val id: String, val text: String, val fromid: String, val toid: String, val timestamp: Long){
    constructor() : this("", "", "", "", -1)
}
class ChatFromItem(val text: String): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val userid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    val myuser = it.getValue(User::class.java)
                    if(FirebaseAuth.getInstance().uid == myuser?.uid)
                    {
                        val profImg = myuser?.profImg
                        Picasso.get().load(profImg).into(viewHolder.itemView.imageview_from)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        viewHolder.itemView.sendmessage_from.text = text

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.sendmessage_to.text = text
        Picasso.get().load(user.profImg).into(viewHolder.itemView.imageview_to)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}