package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
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
 * <h1>Main Activity</h1>
 *
 * The main screen of the application
 * where the user can move to the Scanner screen
 * or the Wardrobe screen.
 *
 * @author Tomer Ben Ari
 * @version 0.16.1
 * @since 0.5.0 (20/12/2019)
 */

public class MainActivity extends AppCompatActivity {

    TextView textView_username;
    User user = new User();

    TextToSpeech textToSpeech;

    NfcAdapter nfcAdapter;

    Boolean isToSpeak;

    Menu optionsMenu;


    /**
     * On activity create:
     * <br>Gets the user from Firebase and sets username on TextView;
     * <br>If the user enabled voice introduction: speaks the activity text;
     * <br>Connects widgets to their view in xml.
     *
     * @param savedInstanceState Containing the activity's previously saved state.
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
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    textToSpeech = new TextToSpeech(getApplicationContext(),
                                            new TextToSpeech.OnInitListener() {
                                                @Override
                                                public void onInit(int status) {
                                                    if (status != TextToSpeech.ERROR) {
                                                        textToSpeech.setLanguage(Locale.US);
                                                        textToSpeech.speak("Welcome " + user.getName() +
                                                                        ". " + getString(R.string.mainText),
                                                                TextToSpeech.QUEUE_FLUSH, null);
                                                    }
                                                }
                                            });
                                }
                            }, 2000);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        textView_username = (TextView)findViewById(R.id.textView_username);
    }

    /**
     * Creates the menu of the activity.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed, if you return false it will not be shown.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.add("Log Out");
        optionsMenu = menu;
        menu.getItem(0).setChecked(isToSpeak);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handling item selection from the menu.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String itemName = item.getTitle().toString();
        if(itemName.equals("Log Out")){
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

    /**
     * When the button is pressed: moves to scanner activity.
     *
     * @param view Open scanner button.
     */

    public void goToScanner(View view) {
        Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
        startActivity(intent);
    }

    /**
     * When back button is pressed:
     * <br>Makes a Toast telling the user to log out from the menu.
     */

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please log out properly by pressing 'Log Out' from the menu.",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * When the button is pressed: moves to wardrobe activity.
     *
     * @param view Open my wardrobe button.
     */

    public void goToWardrobe(View view) {
        Intent intent = new Intent(MainActivity.this, WardrobeActivity.class);
        startActivity(intent);
    }

    /**
     * On activity resume:
     * <br>Sets the voice introduction checkbox in accordance to the user selection
     * if options menu is not null;
     * <br>Adds the NFC adapter to the Foreground Dispatch system if is not null.
     */

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        isToSpeak = settings.getBoolean("speakText",true);
        if (optionsMenu != null){
            optionsMenu.getItem(0).setChecked(isToSpeak);
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    /**
     * On activity pause:
     * <br>Removes NFC adapter from Foreground Dispatch system if is not null,
     * <br>Stops and shuts down TextToSpeech object if is not null.
     */

    @Override
    protected void onPause() {
        super.onPause();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }

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
