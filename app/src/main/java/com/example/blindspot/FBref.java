package com.example.blindspot;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * @author Tomer Ben Ari
 * @version 0.6.0
 * @since 0.3.0 (08/12/2019)
 *
 * Fbref class
 * <p>
 *     Contains Firebase references
 * </p>
 */

public class FBref {

    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();
    public static DatabaseReference refUsers = FBDB.getReference("Users");

    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
}
