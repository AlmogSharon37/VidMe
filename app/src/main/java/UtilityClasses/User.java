package UtilityClasses;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    String username, profilePic, email, uid;
    HashMap<String, Boolean> friends,friendRequests;


    public HashMap<String, Boolean> getFriends() {
        return friends;
    }

    public void setFriends(HashMap<String, Boolean> friends) {
        this.friends = friends;
    }

    public HashMap<String, Boolean> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(HashMap<String, Boolean> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public User(){

    }

    public User(String username, String profilePic, String email, String uid) {
        this.username = username;
        this.profilePic = profilePic;
        this.email = email;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
