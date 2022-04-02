package com.example.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.kotlinmessenger.NewMessageActivity.Companion.USER_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_latestmessages.view.*
import kotlinx.android.synthetic.main.user_row_newmessage.view.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class LatestMessagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recycleview_latestmessages.adapter = adapter
        recycleview_latestmessages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        //Set Item click listener
        adapter.setOnItemClickListener{item, view ->
            Log.d("1234", "123")
            intent = Intent(this, ChatLogActivity::class.java)

            val row = item as LatestMessageRow


            //Log.d("1234",row.chatPartnerUser?.username!!)
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }
        //setUpDummyRows()
        listenForLatestMessages()
        verifyUserLoggedIn()

    }
    val latestmessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerviewMessages(){
        adapter.clear()
        latestmessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }
    private fun listenForLatestMessages(){
        val fromid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromid")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatmessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestmessagesMap[snapshot.key!!] = chatmessage
                refreshRecyclerviewMessages()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatmessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestmessagesMap[snapshot.key!!] = chatmessage
                refreshRecyclerviewMessages()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {   }
            override fun onCancelled(error: DatabaseError) {   }
            override fun onChildRemoved(snapshot: DataSnapshot) {   }
        })
    }
    class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
        var chatPartnerUser: User? = null
        override fun getLayout(): Int {
            return R.layout.user_row_latestmessages
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.latestmessages_latestmessages.text = chatMessage.text

            val chatpartnerid: String
            if(chatMessage.fromid == FirebaseAuth.getInstance().uid)
            {
                chatpartnerid = chatMessage.toid
            }
            else{
                chatpartnerid = chatMessage.fromid
            }
            val reference = FirebaseDatabase.getInstance().getReference("/users/$chatpartnerid")
            reference.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser = snapshot.getValue(User::class.java)

                    val username = chatPartnerUser?.username
                    val profImg = chatPartnerUser?.profImg

                    Picasso.get().load(profImg).into(viewHolder.itemView.imageview_latestmessages)
                    viewHolder.itemView.username_latestmessages.text = username
                }
                override fun onCancelled(error: DatabaseError) {     }

            })
        }
    }
    val adapter = GroupAdapter<ViewHolder>()

    private fun verifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null)
        {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.menu_newmessage -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_signout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
