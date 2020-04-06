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
import com.google.firebase.auth.AuthResult;

import java.util.Locale;

import static com.example.blindspot.FBref.refAuth;

/**
 * <h1>Login Activity</h1>
 *
 * The login screen where the user can login to the application.
 *
 * @author Tomer Ben Ari
 * @version 0.16.1
 * @since 0.4.0 (15/12/2019)
 */

public class LoginActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;

    EditText editText_login_email, editText_login_pass;
    CheckBox checkBox_login_stayConnected;

    TextToSpeech textToSpeech;

    String email, password;

    Boolean isToSpeak;

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
        setContentView(R.layout.activity_login);


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
                                textToSpeech.speak(getString(R.string.loginText), TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                    });
                }
            }, 1500);

        }

        editText_login_email = (EditText)findViewById(R.id.editText_login_email);
        editText_login_pass = (EditText)findViewById(R.id.editText_login_pass);
        checkBox_login_stayConnected = (CheckBox)findViewById(R.id.checkBox_login_stayConnected);
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
     * Login to Firebase with email and password.
     * <p>
     *     Calls {@link #validateForm()} to check if the user filled out all the fields.
     * </p>
     *
     * @param email User's email.
     * @param password User's password.
     */

    public void loginToAccount(String email, String password){
        Log.d("LoginActivity", "loginToAccount:"+email);
        if(!validateForm()){
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this,"Login",
                "Connecting...",true);

        refAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();

                if(task.isSuccessful()){
                    SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("stayConnected", checkBox_login_stayConnected.isChecked());
                    editor.commit();
                    Log.d("LoginActivity", "signInUserWithEmail:success");
                    Toast.makeText(LoginActivity.this, "Login succeeded", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Log.d("LoginActivity", "signInUserWithEmail:fail");
                    Toast.makeText(LoginActivity.this, "Email or Password are wrong!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * Checks if the user's input is not missing.
     *
     * @return Returns false if input is missing, true if noting is missing.
     */
    private boolean validateForm() {
        boolean valid = true;


        String email = editText_login_email.getText().toString();
        if (TextUtils.isEmpty(email)) {
            editText_login_email.setError("Required.");
            valid = false;
        } else {
            editText_login_email.setError(null);
        }

        String password = editText_login_pass.getText().toString();
        if (TextUtils.isEmpty(password)) {
            editText_login_pass.setError("Required.");
            valid = false;
        } else {
            editText_login_pass.setError(null);
        }

        return valid;
    }

    /**
     * When the button is pressed:
     * <br>Calls {@link #loginToAccount(String, String)} method
     * using the email and password from the EditText.
     *
     * @param view Login button.
     */

    public void login(View view) {
        email = editText_login_email.getText().toString();
        password = editText_login_pass.getText().toString();

        loginToAccount(email,password);
    }

    /**
     * On activity resume
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
