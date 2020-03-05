package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import static com.example.blindspot.FBref.refClothes;

/**
 * @author Tomer Ben Ari
 * @version 0.12.0
 * @since 0.6.0 (09/01/2020)
 *
 * Scanner Activity
 */

public class ScannerActivity extends AppCompatActivity {
    TextView textView_clothInfo;
    String clothCode;
    String type,size,color,fullInfo,linkString;

    TextToSpeech tts;

    NfcAdapter nfcAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        textView_clothInfo = (TextView) findViewById(R.id.textView_clothInfo);
        textView_clothInfo.setText("Waiting for NDEF Message");
        fullInfo = null;
    }


    @Override
    protected void onResume() {
        super.onResume();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null && nfcAdapter.isEnabled() && nfcAdapter.isNdefPushEnabled()){
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
        else {
            Toast.makeText(this, "NFC or Android Beam is not active :(", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Open NFC settings");
            builder.setMessage("Press OPEN to open NFC settings:");
            builder.setCancelable(false);

            builder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                    dialog.dismiss();
                }
            });

            builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }


    /**
     * Gets the clothing item code from another device via Android Beam,
     * retrieves the information from Firebase according to code,
     * displays the information in TextView,
     * reads the information out loud
     *
     * @param intent
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
                    textView_clothInfo.setText(
                            "Type: " + type + "\n"
                                    + "Size: " + size + "\n"
                                    + "Color:" + color
                    );

                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                tts.setLanguage(Locale.US);
                                tts.speak(size + " " + color + " " + type, TextToSpeech.QUEUE_FLUSH, null);
                            }
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

    @Override
    protected void onPause() {
        super.onPause();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

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
    }
}
