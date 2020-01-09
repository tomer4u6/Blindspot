package com.example.blindspot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

/**
 * @author Tomer Ben Ari
 * @version 0.7.0
 * @since 0.6.0 (09/01/2020)
 *
 * Scanner Activity
 */

public class ScannerActivity extends AppCompatActivity {
    TextView textView_clothInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        textView_clothInfo = (TextView) findViewById(R.id.textView_clothInfo);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            textView_clothInfo.setText(new String(message.getRecords()[0].getPayload()));

        } else
            textView_clothInfo.setText("Waiting for NDEF Message");

    }
}
