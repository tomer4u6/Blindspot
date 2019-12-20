package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.blindspot.FBref.refAuth;
import static com.example.blindspot.FBref.refUsers;

/**
 * @author Tomer Ben Ari
 * @version 0.5.0
 * @since 0.5.0 (20/12/2019)
 *
 * Main Activity
 */

public class MainActivity extends AppCompatActivity {

    TextView textView_username;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_username = (TextView)findViewById(R.id.textView_username);
    }

    /**
     * On activity start gets the current user and displays the username
     * <p>
     */

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = refAuth.getCurrentUser();
        refUsers.child(firebaseUser.getEmail().replace("."," "))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if(user != null){
                    textView_username.setText(user.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
