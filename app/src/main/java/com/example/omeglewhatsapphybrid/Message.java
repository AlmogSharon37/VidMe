package com.example.omeglewhatsapphybrid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Message {
    String sender, reciever, content;
    Date date;

    public Message(String sender, String reciever, String content, Date date) {
        this.sender = sender;
        this.reciever = reciever;
        this.content = content;
        this.date = date;
    }

    public Message(){}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
