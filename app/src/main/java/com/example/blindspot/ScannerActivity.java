package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import static com.example.blindspot.FBref.refAuth;
import static com.example.blindspot.FBref.refClothes;
import static com.example.blindspot.FBref.refWardrobe;

/**
 * <h1>Scanner Activity</h1>
 *
 * The Scanner screen where the user can scan clothes
 * and share them with others.
 *
 * @author Tomer Ben Ari
 * @version 1.1.2
 * @since 0.6.0 (09/01/2020)
 */

public class ScannerActivity extends AppCompatActivity {
    TextView textView_clothInfo;

    String clothCode;
    String type,size,color,fullInfo,linkString;
    Long amount;

    DatabaseReference refWardrobe_user;

    TextToSpeech textToSpeech;

    NfcAdapter nfcAdapter;

    Boolean isToSpeak;

    boolean speakAgain,firstOpen;


    /**
     * On activity create:
     * <br>Connects widgets to their view in xml.
     *
     * @param savedInstanceState Containing the activity's previously saved state.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        isToSpeak = settings.getBoolean("speakText",true);

        speakAgain = true;
        firstOpen = true;

        FirebaseUser firebaseUser = refAuth.getCurrentUser();
        refWardrobe_user = refWardrobe.child(firebaseUser.getEmail().replace("."," "));

        textView_clothInfo = (TextView) findViewById(R.id.textView_clothInfo);
        textView_clothInfo.setText("Waiting for NDEF Message");
        fullInfo = null;
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
        menu.add("Open Wardrobe");
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
        if (itemName.equals("Open Wardrobe")){
            Intent intent = new Intent(ScannerActivity.this, WardrobeActivity.class);
            startActivity(intent);
            finish();
        }

        if (item.getItemId() == R.id.instructions){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Instructions");
            builder.setMessage(getString(R.string.scannerText));

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On activity resume:
     * <br>Checks if NFC and Android Beam is enabled in the phone:
     * if false creating dialog to open NFC settings,
     * if true adds the NFC adapter to the Foreground Dispatch system and
     * if the user enabled voice introduction: speaks the activity text.
     */

    @Override
    protected void onResume() {
        super.onResume();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null && nfcAdapter.isEnabled() && nfcAdapter.isNdefPushEnabled()){
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

            if (isToSpeak && speakAgain) {
                speakAgain = false;
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status != TextToSpeech.ERROR) {
                                    textToSpeech.setLanguage(Locale.US);
                                    textToSpeech.speak(getString(R.string.scannerText),
                                            TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        });
                    }
                }, 1500);
            }
        }
        else {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Open NFC settings");
            builder.setMessage(getString(R.string.nfcDisabled));
            builder.setCancelable(false);

            builder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    firstOpen = false;
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                    dialog.dismiss();
                }
            });

            builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            if (firstOpen) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status != TextToSpeech.ERROR) {
                                    textToSpeech.setLanguage(Locale.US);
                                    textToSpeech.speak(getString(R.string.nfcDisabled), TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        });
                    }
                }, 500);
            }
        }
    }


    /**
     * Gets the clothing item code from another device via Android Beam,
     * <br>retrieves the information from Firebase according to code,
     * <br>displays the information in TextView and speaks the information.
     *
     * @param intent The new intent that was started for the activity
     *               (Intent to start an activity when a tag with NDEF payload is discovered).
     */

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            clothCode = new String(message.getRecords()[0].getPayload());
            refClothes.child(clothCode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    type = dataSnapshot.child("Type").getValue(String.class);
                    size = dataSnapshot.child("Size").getValue(String.class);
                    color = dataSnapshot.child("Color").getValue(String.class);
                    linkString = dataSnapshot.child("Uri").getValue(String.class);
                    fullInfo = size + " " + color + " " + type;

                    refWardrobe_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(clothCode)){
                                amount = dataSnapshot.child(clothCode)
                                        .child("amount").getValue(Long.class);
                            }
                            else {
                                amount = 0L;
                            }

                            textView_clothInfo.setText(
                                    "Type: " + type + "\n"
                                            + "Size: " + size + "\n"
                                            + "Color:" + color + "\n"
                                            + "You have " + amount.toString() + " of these in your wardrobe."
                            );

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        @Override
                                        public void onInit(int status) {
                                            if (status != TextToSpeech.ERROR) {
                                                textToSpeech.setLanguage(Locale.US);
                                                textToSpeech.speak(fullInfo + ". "
                                                        + "You have " + amount.toString()
                                                        + " of these in your wardrobe.", TextToSpeech.QUEUE_FLUSH, null);
                                            }
                                        }
                                    });
                                }
                            }, 700);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        else {
            textView_clothInfo.setText("Waiting for NDEF Message");
            fullInfo = null;
        }
    }

    /**
     * On activity pause:
     * <br>Removes NFC adapter from Foreground Dispatch system if is not null;
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

    /**
     * When button is pressed:
     * <br>If cloth was scanned: opens sharing options.
     *
     * @param view Click to share button.
     */
    public void shareInfo(View view) {
        if(fullInfo!=null){

            Intent sendIntent = new Intent();

            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "My item info");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "I WANT TO BUY THIS NEW ITEM: \n" + fullInfo + "\n\n" + linkString);

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        else {
            Toast.makeText(this, "Cloth wasn't scanned!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When back button is pressed: finishes the activity.
     */

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
