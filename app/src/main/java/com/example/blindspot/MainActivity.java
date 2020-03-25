package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import static com.example.blindspot.FBref.refAuth;
import static com.example.blindspot.FBref.refUsers;

/**
 * @author Tomer Ben Ari
 * @version 0.15.0
 * @since 0.5.0 (20/12/2019)
 *
 * Main Activity
 */

public class MainActivity extends AppCompatActivity {

    TextView textView_username;
    User user = new User();

    TextToSpeech textToSpeech;

    NfcAdapter nfcAdapter;

    Boolean isToSpeak;




    /**
     * On activity create gets the user from Firebase and sets username on TextView
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        isToSpeak = settings.getBoolean("speakText",true);


        final ProgressDialog progressDialog = ProgressDialog.show(this,"Login",
                "Connecting...",true);
        FirebaseUser firebaseUser = refAuth.getCurrentUser();
        refUsers.child(firebaseUser.getEmail().replace("."," "))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       user.copyUser(dataSnapshot.getValue(User.class));
                        textView_username.setText("Welcome "+user.getName()+".");
                        progressDialog.dismiss();
                        if (isToSpeak) {
                            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    if (status != TextToSpeech.ERROR) {
                                        textToSpeech.setLanguage(Locale.US);
                                        textToSpeech.speak("Welcome " + user.getName() + ". " + getString(R.string.mainText), TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        textView_username = (TextView)findViewById(R.id.textView_username);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.add("Log Out");
        menu.getItem(0).setChecked(isToSpeak);

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
            finish();
        }

        if(item.getItemId() == R.id.textToSpeech_Checkbox){
            SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            if(item.isChecked()){
                if(textToSpeech != null){
                    textToSpeech.stop();
                    textToSpeech.shutdown();
                }
                item.setChecked(false);
                editor.putBoolean("speakText", false);
                editor.commit();
            }
            else {
                item.setChecked(true);
                editor.putBoolean("speakText", true);
                editor.commit();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void goToScanner(View view) {
        Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please log out properly by pressing 'Log Out' from the menu.",
                Toast.LENGTH_SHORT).show();
    }

    public void goToWardrobe(View view) {
        Intent intent = new Intent(MainActivity.this, WardrobeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);

        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
