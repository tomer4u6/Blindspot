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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import static com.example.blindspot.FBref.refAuth;
import static com.example.blindspot.FBref.refUsers;

/**
 * <h1>Register Activity</h1>
 *
 * The register screen where the user can register to the application.
 *
 * @author Tomer Ben Ari
 * @version 1.1.1
 * @since 0.3.0 (08/12/2019)
 */

public class RegisterActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    EditText editText_name, editText_email, editText_pass;
    CheckBox checkBox_stayConnected;

    TextToSpeech textToSpeech;
    Boolean isToSpeak;

    String name, email, password, uid;
    User userdb;

    char[] invalidChars = new char[6];

    /**
     * On activity create:
     * <br>If the user enabled voice introduction: speaks the activity text;
     * <br>Connects widgets to their view in xml.
     *
     * @param savedInstanceState Containing the activity's previously saved state.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        isToSpeak = settings.getBoolean("speakText",true);

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
                                textToSpeech.speak(getString(R.string.registerText), TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                    });
                }
            }, 1500);
        }



        editText_name = (EditText)findViewById(R.id.editText_name);
        editText_email = (EditText)findViewById(R.id.editText_email);
        editText_pass = (EditText)findViewById(R.id.editText_pass);
        checkBox_stayConnected = (CheckBox)findViewById(R.id.checkBox_stayConnected);


        invalidChars[0] = '.';
        invalidChars[1] = '$';
        invalidChars[2] = '#';
        invalidChars[3] = '[';
        invalidChars[4] = ']';
        invalidChars[5] = '/';


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
     * Creating a new user in Firebase.
     * <p>
     *     Registering a new user to Firebase using Firebase Auth with email and password,
     *     adding the new User object to Firebase Realtime Database.
     * </p>
     *
     * <p>
     *     Calls {@link #validateForm()} to check if user input is not missing and is valid.
     * </p>
     *
     * @param name User's name.
     * @param email User's email.
     * @param password User's password.
     */

    public void createAccount(final String name, final String email, String password) {

        Log.d("RegisterActivity", "createAccount:"+email);
        if(!validateForm()){
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this,"Register",
                "Registering...",true);

        refAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("stayConnected", checkBox_stayConnected.isChecked());
                            editor.commit();
                            Log.d("RegisterActivity", "createUserWithEmail:success");
                            FirebaseUser user = refAuth.getCurrentUser();
                            uid = user.getUid();
                            userdb = new User(name,email,uid);
                            refUsers.child(email.replace("."," ")).setValue(userdb)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(RegisterActivity.this, "Couldn't create user.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else{
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(RegisterActivity.this, "User with e-mail already exist!", Toast.LENGTH_SHORT).show();
                            }
                            else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                Toast.makeText(RegisterActivity.this, "Password is too weak!", Toast.LENGTH_SHORT).show();
                            }
                            else if (task.getException() instanceof FirebaseNetworkException){
                                Toast.makeText(RegisterActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                            }
                            else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(RegisterActivity.this, "The e-mail is badly formatted.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Log.w("RegisterActivity", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "User creation failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Checks if the user input is not missing and is valid.
     *
     * @return Returns false if input is missing or invalid, true if input is valid and not missing.
     */

    private boolean validateForm() {
        boolean valid = true;

        String name = editText_name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            editText_name.setError("Required.");
            valid = false;
        } else {
            editText_name.setError(null);
        }

        if(valid) {
            for (int i = 0; i < invalidChars.length; i++) {
                if (name.contains(Character.toString(invalidChars[i]))) {
                    editText_name.setError("Invalid Character.");
                    valid = false;
                    break;
                } else {
                    editText_name.setError(null);
                }
            }
        }

        String email = editText_email.getText().toString();
        if (TextUtils.isEmpty(email)) {
            editText_email.setError("Required.");
            valid = false;
        } else {
            editText_email.setError(null);
        }

        String password = editText_pass.getText().toString();
        if (TextUtils.isEmpty(password)) {
            editText_pass.setError("Required.");
            valid = false;
        } else {
            editText_pass.setError(null);
        }

        return valid;
    }

    /**
     * When the button is pressed:
     * <br>Calls {@link #createAccount(String, String, String)} method
     * using the email, password and name from the EditText.
     *
     * @param view Register button.
     */

    public void register(View view) {
        name = editText_name.getText().toString();
        email = editText_email.getText().toString();
        password = editText_pass.getText().toString();

        createAccount(name,email,password);
    }

    /**
     * On activity resume:
     * <br>Adds the NFC adapter to the Foreground Dispatch system if is not null.
     */

    @Override
    protected void onResume() {
        super.onResume();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
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
     * When back button is pressed: finishes the activity.
     */

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
