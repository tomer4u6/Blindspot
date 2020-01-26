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

import static com.example.blindspot.FBref.refAuth;

/**
 * @author Tomer Ben Ari
 * @version 0.9.0
 * @since 0.4.0 (15/12/2019)
 *
 * Login Activity
 */

public class LoginActivity extends AppCompatActivity {

    EditText editText_login_email, editText_login_pass;
    CheckBox checkBox_login_stayConnected;

    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText_login_email = (EditText)findViewById(R.id.editText_login_email);
        editText_login_pass = (EditText)findViewById(R.id.editText_login_pass);
        checkBox_login_stayConnected = (CheckBox)findViewById(R.id.checkBox_login_stayConnected);
    }

    /**
     * Login to Firebase with email and password
     *
     * @param email User email
     * @param password User password
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
                }
                else{
                    Log.d("LoginActivity", "signInUserWithEmail:fail");
                    Toast.makeText(LoginActivity.this, "Email or Password are wrong!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * Checks if the user input is not missing
     *
     * @return boolean This returns false if input is missing and returns true if noting is missing
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

    public void login(View view) {
        email = editText_login_email.getText().toString();
        password = editText_login_pass.getText().toString();

        loginToAccount(email,password);
    }
}
