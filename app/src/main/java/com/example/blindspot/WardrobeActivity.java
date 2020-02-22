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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

import static com.example.blindspot.FBref.refClothes;
import static com.example.blindspot.FBref.refWardrobe;
import static com.example.blindspot.FBref.refAuth;

/**
 * @author Tomer Ben Ari
 * @version 0.11.1
 * @since 0.9.0 (26/01/2020)
 *
 * Wardrobe Activity
 */

public class WardrobeActivity extends AppCompatActivity {
    ListView listView_wardrobe;
    Spinner spinner_type;

    ArrayList<String> wardrobeList = new ArrayList<String>();
    ArrayList<String> codesList = new ArrayList<String>();

    ArrayAdapter<String> adapter_listView;
    ArrayAdapter<String> adapter_spinner;

    String[] types = {
            "Shirt",
            "T-Shirt",
            "Skirt",
            "Sweater",
            "Hoodie",
            "Dress",
            "Jeans",
            "Trousers",
            "Shorts",
            "Underpants",
            "Full Wardrobe"
    };

    DatabaseReference refWardrobe_user;
    ValueEventListener valueEventListener;
    Query query;

    String clothCode,value;
    String type,size,color;
    Long amount;

    TextToSpeech tts;

    NfcAdapter nfcAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);


        listView_wardrobe = (ListView)findViewById(R.id.listView_wardrobe);
        spinner_type = (Spinner)findViewById(R.id.spinner_type);


        adapter_spinner = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, types);
        spinner_type.setAdapter(adapter_spinner);

        adapter_listView = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, wardrobeList);
        listView_wardrobe.setAdapter(adapter_listView);

        FirebaseUser firebaseUser = refAuth.getCurrentUser();
        refWardrobe_user = refWardrobe.child(firebaseUser.getEmail().replace("."," "));

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wardrobeList.clear();
                codesList.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()){
                    String code = childSnapShot.getKey();
                    String type = childSnapShot.child("Type").getValue(String.class);
                    String size = childSnapShot.child("Size").getValue(String.class);
                    String color = childSnapShot.child("Color").getValue(String.class);
                    String amount = String.valueOf(childSnapShot.child("Amount").getValue(Long.class));
                    String value = size + ";" + color + ";" + type + ";" + amount + " pcs.";
                    wardrobeList.add(value);
                    codesList.add(code);
                }
                adapter_listView = new ArrayAdapter<String>(WardrobeActivity.this,
                        R.layout.support_simple_spinner_dropdown_item, wardrobeList);
                listView_wardrobe.setAdapter(adapter_listView);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        spinner_type.setSelection(10);
        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * When item is selected from Spinner: changes the view in ListView,
             * sorted by the selected type from Spinner using Query
             *
             * @param parent
             * @param view
             * @param position
             * @param id
             */

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 10){
                    query = refWardrobe_user;
                    query.addValueEventListener(valueEventListener);
                }
                else {
                    query = refWardrobe_user.orderByChild("Type")
                            .equalTo(types[position]);
                    query.addValueEventListener(valueEventListener);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        query.removeEventListener(valueEventListener);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Open NFC settings");
            builder.setMessage("Press OPEN to open NFC settings:");
            builder.setCancelable(false);

            builder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                }
            });

            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
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
     * Adding/Removing clothing item to/from wardrobe on Firebase
     * <p>
     *     Gets the clothing item code from another device via Android Beam,
     *     retrieves the information from Firebase according to code,
     *     opens AlertDialog for adding or removing the item
     * </p>
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
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    type = dataSnapshot.child("Type").getValue(String.class);
                    size = dataSnapshot.child("Size").getValue(String.class);
                    color = dataSnapshot.child("Color").getValue(String.class);

                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                tts.setLanguage(Locale.US);
                                tts.speak(size + " " + color + " " + type, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                    });


                    AlertDialog.Builder builder = new AlertDialog.Builder(WardrobeActivity.this);
                    builder.setTitle("Adding/Removing clothing item");
                    builder.setMessage(size + ";" + color + ";" + type +
                            "\n\nSelect Add to add this item to your wardrobe or Remove to remove it from your wardrobe if it contains this item");
                    builder.setCancelable(false);

                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            spinner_type.setSelection(10);
                            refWardrobe_user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(clothCode)){
                                        amount = dataSnapshot.child(clothCode).child("Amount").getValue(Long.class);
                                        amount += 1;
                                        refWardrobe_user.child(clothCode).child("Amount").setValue(amount);
                                    }
                                    else {
                                        refWardrobe_user.child(clothCode).child("Type").setValue(type);
                                        refWardrobe_user.child(clothCode).child("Color").setValue(color);
                                        refWardrobe_user.child(clothCode).child("Size").setValue(size);
                                        refWardrobe_user.child(clothCode).child("Amount").setValue(Long.valueOf(1));
                                    }
                                    Toast.makeText(WardrobeActivity.this, "Adding succeeded.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            dialog.dismiss();
                        }
                    });


                    builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            spinner_type.setSelection(10);
                            refWardrobe_user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(clothCode)){
                                        refWardrobe_user.child(clothCode).child("Amount").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                amount = dataSnapshot.getValue(Long.class);
                                                amount -= 1;
                                                if (amount == 0){
                                                    refWardrobe_user.child(clothCode).removeValue();
                                                }
                                                else {
                                                    refWardrobe_user.child(clothCode).child("Amount").setValue(amount);
                                                }
                                                Toast.makeText(WardrobeActivity.this, "Removing succeeded.", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(WardrobeActivity.this, "Your wardrobe doesn't contain this item!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            dialog.dismiss();
                        }
                    });

                    builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
