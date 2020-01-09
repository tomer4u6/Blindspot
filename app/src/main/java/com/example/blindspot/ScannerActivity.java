package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import static com.example.blindspot.FBref.refClothes;

/**
 * @author Tomer Ben Ari
 * @version 0.8.0
 * @since 0.6.0 (09/01/2020)
 *
 * Scanner Activity
 */

public class ScannerActivity extends AppCompatActivity {
    TextView textView_clothInfo;
    String clothCode;
    String type,size,color;

    TextToSpeech tts;

    NfcAdapter nfcAdapter;
    AlertDialog.Builder adb;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        textView_clothInfo = (TextView) findViewById(R.id.textView_clothInfo);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    /**
     * Gets the cloth code from another device via Android Beam,
     * Retrieves the cloth information from Firebase according to code,
     * displays the information in TextView,
     * reads the information out loud
     *
     */

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null && nfcAdapter.isEnabled() && nfcAdapter.isNdefPushEnabled()) {
            Intent intent = getIntent();
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                        NfcAdapter.EXTRA_NDEF_MESSAGES);

                NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
                clothCode = new String(message.getRecords()[0].getPayload());
                refClothes.child(clothCode).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        type = dataSnapshot.child("Type").getValue().toString();
                        size = dataSnapshot.child("Size").getValue().toString();
                        color = dataSnapshot.child("Color").getValue().toString();
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


            } else
                textView_clothInfo.setText("Waiting for NDEF Message");

        }
        else {
            Toast.makeText(this, "NFC or Android Beam is not active :(", Toast.LENGTH_SHORT).show();

            adb = new AlertDialog.Builder(this);
            adb.setTitle("Open NFC settings");
            adb.setMessage("Press OPEN to open NFC settings:");
            adb.setCancelable(false);

            adb.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                }
            });

            adb.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    dialog.cancel();
                }
            });

            AlertDialog ad = adb.create();
            ad.show();
        }
    }
}
