package com.example.omeglewhatsapphybrid;

import android.content.Context;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder>{

    private ArrayList<Message> messages;
    private String myName, friendName;
    private Uri myImg, friendImg;
    private Context context;

    public MessageAdapter(ArrayList<Message> messages, String myName, String friendName, Uri myImg, Uri friendImg, Context context) {
        this.messages = messages;
        this.myName = myName;
        this.friendName = friendName;
        this.myImg = myImg;
        this.friendImg = friendImg;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_holder, parent, false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        Message currentMsg = messages.get(position);
        holder.messageData.setText(messages.get(position).getContent());
        if(currentMsg.getSender().equals(myName)) {
            UtilityFunctions.setImageOnImageView(context, myImg, holder.profileImage);
        }
        else{
            UtilityFunctions.setImageOnImageView(context, friendImg, holder.profileImage);
        }
        holder.userName.setText(currentMsg.getSender());
        holder.date.setText(UtilityFunctions.convertDateToLocalDate(currentMsg.getDate()));

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageHolder extends RecyclerView.ViewHolder{
        ConstraintLayout constraintLayout;
        ImageView profileImage;
        TextView userName;
        TextView date;
        TextView messageData;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.constraint);
            profileImage = itemView.findViewById(R.id.profilePic);
            userName = itemView.findViewById(R.id.usernameText);
            date = itemView.findViewById(R.id.dateText);
            messageData = itemView.findViewById(R.id.messageText);
        }
    }
}





