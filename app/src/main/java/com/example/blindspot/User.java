package com.example.blindspot;


/**
 * @author Tomer Ben Ari
 * @version 0.15.1
 * @since 0.3.0 (08/12/2019)
 *
 * User class
 * <p>
 *     Contains user info
 * </p>
 */

public class User {
    private String name, email, uid;

    public User(){
        this.name = "";
        this.email = "";
        this.uid = "";
    }

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

    public void copyUser(User user){
        this.name = user.getName();
        this.email = user.getEmail();
        this.uid = user.getUid();
    }
}
