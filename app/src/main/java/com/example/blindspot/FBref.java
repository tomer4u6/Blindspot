package com.example.blindspot;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * <h1>FBref class</h1>
 *
 * Contains Firebase references.
 *
 * @author Tomer Ben Ari
 * @version 1.0.0
 * @since 0.3.0 (08/12/2019)
 */

public class FBref {

    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();
    public static DatabaseReference refUsers = FBDB.getReference("Users");
    public static DatabaseReference refClothes = FBDB.getReference("Clothes");
    public static DatabaseReference refWardrobe = FBDB.getReference("Wardrobes");

    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
}
