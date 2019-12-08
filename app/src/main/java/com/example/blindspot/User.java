package com.example.blindspot;

public class User {
    private String name, email, uid;

    public User(){}

    public User(String name, String email, String uid){
        this.name = name;
        this.email = email;
        this.uid = uid;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return this.email;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getUid(){
        return this.uid;
    }
}
