package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

import kotlinx.android.synthetic.main.user_row_newmessage.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_newmessage.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        //val adapter = GroupAdapter<ViewHolder>()
        //adapter.add(UserItem())
        //adapter.add(UserItem())
        //adapter.add(UserItem())
        //recycleview_newmessage.adapter = adapter

        fetchUsers()
    }
    companion object {
        val USER_KEY = "USER_KEY"
    }
    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                snapshot.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if(user != null && FirebaseAuth.getInstance().uid != user.uid)
                    {
                        adapter.add(UserItem(user))
                    }
                    adapter.setOnItemClickListener { item, view ->
                        val userItem = item as UserItem

                        val intent = Intent(view.context, ChatLogActivity::class.java)
                        //intent.putExtra(USER_KEY, userItem.user.username)
                        intent.putExtra(USER_KEY, userItem.user)
                        startActivity(intent)

                        finish()
                    }

                    Log.d("NewMessage", user.toString())
                    recycleview_newmessage.adapter = adapter
                }


            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        //will be called in our list for each user object later on...
        viewHolder.itemView.username_newmessage.text = user.username

        Picasso.get().load(user.profImg).into(viewHolder.itemView.imageview_newmessage)
    }
    override fun getLayout(): Int {
        return R.layout.user_row_newmessage
    }
}
//This is super tedious

//class CustomAdapter: RecyclerView.Adapter<ViewHolder>{
 //   override fun onBindViewHolder(holder: ViewHolder, position: Int) {

  //  }
//}