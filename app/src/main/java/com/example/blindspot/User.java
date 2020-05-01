package com.example.blindspot;


/**
 * <h1>User class</h1>
 *
 * Contains user information.
 *
 * @author Tomer Ben Ari
 * @version 1.1.3
 * @since 0.3.0 (08/12/2019)
 */

public class User {
    private String name, email, uid;

    /**
     * Creates User object with empty fields.
     */

    public User(){
        this.name = "";
        this.email = "";
        this.uid = "";
    }

    /**
     * Creates User object with the given name, email and uid.
     *
     * @param name Name of the user.
     * @param email Email of the user.
     * @param uid Uid of the user.
     */

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

    /**
     * Copies the information from another user.
     *
     * @param user User to copy from.
     */

    public void copyUser(User user){
        this.name = user.getName();
        this.email = user.getEmail();
        this.uid = user.getUid();
    }
}
