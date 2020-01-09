package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import static com.example.blindspot.FBref.refAuth;
import static com.example.blindspot.FBref.refUsers;

/**
 * @author Tomer Ben Ari
 * @version 0.8.0
 * @since 0.5.0 (20/12/2019)
 *
 * Main Activity
 */

public class MainActivity extends AppCompatActivity {

    TextView textView_username;
    User user = new User();

    TextToSpeech tts;


    /**
     * On activity create gets the user from Firebase and sets username on TextView
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_username = (TextView)findViewById(R.id.textView_username);

        final ProgressDialog progressDialog = ProgressDialog.show(this,"Login",
                "Connecting...",true);
        FirebaseUser firebaseUser = refAuth.getCurrentUser();
        refUsers.child(firebaseUser.getEmail().replace("."," "))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       user.copyUser(dataSnapshot.getValue(User.class));
                        textView_username.setText("Welcome "+user.getName());
                        progressDialog.dismiss();
                        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status != TextToSpeech.ERROR){
                                    tts.setLanguage(Locale.US);
                                    tts.speak( "Welcome "+user.getName() ,TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Log Out");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String string = item.getTitle().toString();
        if(string.equals("Log Out")){
            refAuth.signOut();
            SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("stayConnected", false);
            editor.commit();
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void goToScanner(View view) {
        Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }
}
