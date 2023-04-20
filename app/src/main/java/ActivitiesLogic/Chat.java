package ActivitiesLogic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import UtilityClasses.Message;
import com.example.omeglewhatsapphybrid.R;
import UtilityClasses.UtilityFunctions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    FirebaseUser currentUser;

    ProgressBar progressBar;
    ImageButton backBtn;
    TextView friendInTitle;
    EditText messageBox;
    ImageButton sendMessageBtn;
    ImageButton callBtn;
    RecyclerView recyclerView;

    String friendUid, friendName, friendImg;

    ArrayList<Message> messages;
    MessageAdapter messageAdapter;
    String chatId;





    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        recyclerView = findViewById(R.id.recycler);
        progressBar = findViewById(R.id.progressBar);
        backBtn = findViewById(R.id.backBtn);
        friendInTitle = findViewById(R.id.friendNameBig);
        messageBox = findViewById(R.id.messageTextBox);
        sendMessageBtn = findViewById(R.id.sendMsgBtn);
        callBtn = findViewById(R.id.callBtn);

        currentUser = mAuth.getCurrentUser();

        friendUid = getIntent().getStringExtra("friendUid");
        friendName = getIntent().getStringExtra("friendName");
        friendImg = getIntent().getStringExtra("friendImg");
        messages = new ArrayList<>();


        friendInTitle.setText(friendName);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                finish();
            }
        });


        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("work");
                if(messageBox.getText().toString().equals(""))
                    return;

                else {
                    Message newMsg = null;
                    newMsg = new Message(currentUser.getDisplayName(), friendName, messageBox.getText().toString(), UtilityFunctions.convertLocalDateIntoDate(LocalDateTime.now()));

                    mDatabase.getReference("Messages/" + chatId).push().setValue(newMsg);
                    messageBox.setText("");
                }
            }
        });

        messageAdapter = new MessageAdapter(messages, currentUser.getDisplayName(), friendName, currentUser.getPhotoUrl(),
                Uri.parse(friendImg), Chat.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(Chat.this));
        recyclerView.setAdapter(messageAdapter);

        setUpChatRoom();


    }

    // the "bigger" id goes first so we always have the same room no matter who is the current user!
    void setUpChatRoom(){
        if(currentUser.getUid().compareTo(friendUid) > 0){
            chatId = currentUser.getUid() + friendUid;
        }
        else{
            chatId = friendUid + currentUser.getUid();
        }
        updateMessages();
    }


    void updateMessages(){
        mDatabase.getReference("Messages/" + chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot child:snapshot.getChildren()) {
                    messages.add(child.getValue(Message.class));
                    System.out.println("message got");
                }
                System.out.println("finished setting up messages");

                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messages.size() - 1);
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
