package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import static com.example.blindspot.FBref.refAuth;
import static com.example.blindspot.FBref.refUsers;

/**
 * @author Tomer Ben Ari
 * @version 0.9.0
 * @since 0.3.0 (08/12/2019)
 *
 * Register Activity
 */

public class RegisterActivity extends AppCompatActivity {

    EditText editText_name, editText_email, editText_pass;
    CheckBox checkBox_stayConnected;

    String name, email, password, uid;
    User userdb;

    char[] invalidChars = new char[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Register Activity");


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
     * Creating a new user in Firebase
     * <p>
     *     Registering a new user to Firebase using Firebase Auth with Email and Password
     *     Adding the new User object to Firebase Realtime Database
     * </p>
     *
     * @param name User name
     * @param email User email
     * @param password User password
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
                            refUsers.child(email.replace("."," ")).setValue(userdb);
                            Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else{
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(RegisterActivity.this, "User with e-mail already exist!", Toast.LENGTH_SHORT).show();
                            }
                            else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                Toast.makeText(RegisterActivity.this, "Password is too weak!", Toast.LENGTH_SHORT).show();
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
     * Checks if the user input is not missing and valid
     *
     * @return boolean This returns false if input is missing and returns true if noting is missing
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

    public void register(View view) {
        name = editText_name.getText().toString();
        email = editText_email.getText().toString();
        password = editText_pass.getText().toString();

        createAccount(name,email,password);
    }
}
