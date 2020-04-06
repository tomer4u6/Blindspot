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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
 * <h1>Wardrobe Activity</h1>
 *
 * The wardrobe screen where the user can handle his wardrobe.
 *
 * @author Tomer Ben Ari
 * @version 0.16.1
 * @since 0.9.0 (26/01/2020)
 */

public class WardrobeActivity extends AppCompatActivity {
    ListView listView_wardrobe;
    Spinner spinner_type;

    ArrayList<String> wardrobeList = new ArrayList<String>();

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

    String clothCode;
    String type, size, color, fullInfo;
    Long amount;

    TextToSpeech textToSpeech;

    NfcAdapter nfcAdapter;

    Boolean isToSpeak;


    /**
     * On activity create:
     * <br>Connects widgets to their view in xml;
     * <br>Displays the user's wardrobe in the ListView.
     *
     *
     * @param savedInstanceState Containing the activity's previously saved state.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        isToSpeak = settings.getBoolean("speakText",true);


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
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()){
                    Cloth cloth = childSnapShot.getValue(Cloth.class);
                    String value = cloth.toString();
                    wardrobeList.add(value);
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
             * When item is selected from Spinner:
             * <br>Changes the view in ListView,
             * sorted by the selected type from Spinner using Query.
             *
             * @param parent The AdapterView where the selection happened.
             * @param view The view within the AdapterView that was clicked.
             * @param position The position of the view in the adapter.
             * @param id The row id of the item that is selected.
             */

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 10){
                    query = refWardrobe_user;
                    query.addValueEventListener(valueEventListener);
                }
                else {
                    query = refWardrobe_user.orderByChild("type")
                            .equalTo(types[position]);
                    query.addValueEventListener(valueEventListener);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Creates the menu of the activity.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed,
     * if you return false it will not be shown.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
     * On activity stop: removes ValueEventListener from the Query.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (query != null) {
            query.removeEventListener(valueEventListener);
        }
    }

    /**
     * On activity resume:
     * <br>Checks if NFC and Android Beam is enabled in the phone:
     * if false creating dialog to open NFC settings,
     * if true adds the NFC adapter to the Foreground Dispatch system if is not null and
     * if the user enabled voice introduction: speaks the activity text.
     */

    @Override
    protected void onResume() {
        super.onResume();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null && nfcAdapter.isEnabled() && nfcAdapter.isNdefPushEnabled()){
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

            if (isToSpeak) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status != TextToSpeech.ERROR) {
                                    textToSpeech.setLanguage(Locale.US);
                                    textToSpeech.speak(getString(R.string.wardrobeText),
                                            TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        });
                    }
                }, 3500);
            }
        }
        else {
            Toast.makeText(this, getText(R.string.nfcDisabled), Toast.LENGTH_SHORT).show();
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
                    dialog.cancel();
                    finish();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }


    /**
     * Adding/Removing clothing item to/from wardrobe on Firebase using Android Beam.
     * <p>
     *     Gets the clothing item code from another device via Android Beam,
     *     <br>retrieves the information from Firebase according to the code,
     *     <br>opens dialog for adding or removing the item.
     * </p>
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
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    type = dataSnapshot.child("Type").getValue(String.class);
                    size = dataSnapshot.child("Size").getValue(String.class);
                    color = dataSnapshot.child("Color").getValue(String.class);

                    fullInfo = size + " " + color + " " + type;


                    textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                textToSpeech.setLanguage(Locale.US);
                                textToSpeech.speak(fullInfo, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                    });


                    AlertDialog.Builder builder = new AlertDialog.Builder(WardrobeActivity.this);
                    builder.setTitle("Adding/Removing clothing item");
                    builder.setMessage(fullInfo +
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
                                        amount = dataSnapshot.child(clothCode).getValue(Cloth.class).getAmount();
                                        amount += 1;
                                        refWardrobe_user.child(clothCode).child("amount").setValue(amount)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(WardrobeActivity.this, "Adding succeeded.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        Cloth cloth = new Cloth(type,color,size,1L);
                                        refWardrobe_user.child(clothCode).setValue(cloth)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(WardrobeActivity.this, "Adding succeeded.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
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
                                        refWardrobe_user.child(clothCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                amount = dataSnapshot.getValue(Cloth.class).getAmount();
                                                amount = amount - 1;
                                                if (amount == 0) {
                                                    refWardrobe_user.child(clothCode).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Toast.makeText(WardrobeActivity.this, "Removing succeeded.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                else {
                                                    refWardrobe_user.child(clothCode).child("amount").setValue(amount)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Toast.makeText(WardrobeActivity.this, "Removing succeeded.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
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

    /**
     * When back button is pressed: finishes the activity.
     */

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
