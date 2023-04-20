package UtilityClasses;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.omeglewhatsapphybrid.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import UtilityClasses.User;

public class UtilityFunctions {


    //need to do friends list, friend request, and sent friend requests
    //need to figure out how to do chats
    public static void writeNewUser(DatabaseReference mDatabase, String userId, String name, String profilePic, String email) {
        User user = new User(name, profilePic, email, userId);

        mDatabase.child("Users").child(userId).setValue(user);

    }

    public static void sendFriendRequest(FirebaseDatabase mDatabase, String uidTo, String uidFrom) {
        mDatabase.getReference("Users/" + uidTo).child("requests").child(uidFrom).setValue(true);
    }

    public static void removeRequestFromUser(FirebaseDatabase mDatabase, String requestId, String userId) {
        mDatabase.getReference("Users/" + userId).child("requests").child(requestId).removeValue();
    }
    public static void removeFriendFromUser(FirebaseDatabase mDatabase, String userId, String friendId) {
        mDatabase.getReference("Users/" + userId).child("friends").child(friendId).removeValue();
        mDatabase.getReference("Users/" + friendId).child("friends").child(userId).removeValue();
    }

    public static void addFriendToUser(FirebaseDatabase mDatabase, String friendId, String userId) {
        mDatabase.getReference("Users/" + userId + "/friends").child(friendId).setValue(true);
        mDatabase.getReference("Users/" + friendId + "/friends").child(userId).setValue(true);
    }


    public static void setImageOnImageView(Context context, Uri uri, ImageView imageView) {
        Glide.with(context).load(uri).error(R.drawable.default_profile_pic).placeholder(R.drawable.default_profile_pic)
                .into(imageView);
    }

    public static String convertDateToLocalDate(Date dateToConvert) {
        LocalDateTime ldt;
        ldt = LocalDateTime.ofInstant(dateToConvert.toInstant(),
                ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return ldt.format(formatter);
    }

    public static Date convertLocalDateIntoDate(LocalDateTime localDateTime) {
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }

}


