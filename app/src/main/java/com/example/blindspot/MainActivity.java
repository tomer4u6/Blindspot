package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.blindspot.FBref.refAuth;
import static com.example.blindspot.FBref.refUsers;

/**
 * @author Tomer Ben Ari
 * @version 0.5.1
 * @since 0.5.0 (20/12/2019)
 *
 * Main Activity
 */

public class MainActivity extends AppCompatActivity {

    TextView textView_username;
    User user = new User();


    /**
     * On activity create gets the user from Firebase and sets username on TextView
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_username = (TextView)findViewById(R.id.textView_username);

        FirebaseUser firebaseUser = refAuth.getCurrentUser();
        refUsers.child(firebaseUser.getEmail().replace("."," "))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       user.copyUser(dataSnapshot.getValue(User.class));
                        textView_username.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public void goToScanner(View view) {
    }
}
