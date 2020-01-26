package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
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

import static com.example.blindspot.FBref.refWardrobe;
import static com.example.blindspot.FBref.refAuth;

/**
 * @author Tomer Ben Ari
 * @version 0.9.0
 * @since 0.9.0 (26/01/2020)
 *
 * Wardrobe Activity
 */

public class WardrobeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        listView_wardrobe = (ListView)findViewById(R.id.listView_wardrobe);
        spinner_type = (Spinner)findViewById(R.id.spinner_type);

        listView_wardrobe.setOnItemClickListener(this);
        listView_wardrobe.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

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
                    String code = childSnapShot.getKey();
                    String type = childSnapShot.child("Type").getValue(String.class);
                    String size = childSnapShot.child("Size").getValue(String.class);
                    String color = childSnapShot.child("Color").getValue(String.class);
                    String value = size + ";" + color + ";" + type;
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
                wardrobeList.clear();
                codesList.clear();
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
        query.removeEventListener(valueEventListener);
    }

    /**
     * When item is selected from ListView: creates AlertDialog,
     * the user can choose to delete the selected clothing item from Firebase
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
        if(parent.getId() == R.id.listView_wardrobe){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Removing clothing item from your wardrobe:");
            builder.setMessage("Would you like to delete the selected clothing item?");
            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String value = codesList.get(position);
                    refWardrobe_user.child(value).removeValue();

                    Toast.makeText(WardrobeActivity.this,
                            "Deleting succeeded.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
}
