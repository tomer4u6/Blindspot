package com.example.blindspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;


/**
 * <h1>Welcome Activity</h1>
 *
 * The first screen of the application
 * where the user can move to the register screen or the login screen.
 *
 * @author Tomer Ben Ari
 * @version 1.1.0
 * @since 0.2.0 (05/12/2019)
 */


public class WelcomeActivity extends AppCompatActivity {

    TextToSpeech textToSpeech;

    NfcAdapter nfcAdapter;

    Boolean isToSpeak;

    Menu optionsMenu;

    TextView textView_versionName,textView_email,textView_icon1,textView_icon2;
    LinearLayout dialog_about;

    /**
     * On activity create:
     * <br>If the user enabled voice introduction: speaks the activity text.
     *
     * @param savedInstanceState Containing the activity's previously saved state.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        isToSpeak = settings.getBoolean("speakText",true);
        boolean isConnected = settings.getBoolean("stayConnected",false);

        if (!isConnected) {
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
                                    textToSpeech.speak(getString(R.string.welcomeText), TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        });
                    }
                }, 1500);
            }
        }


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
        menu.add("About");
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
        String itemName = item.getTitle().toString();
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
        if (itemName.equals("About")){
            PackageManager manager = this.getPackageManager();
            PackageInfo info = null;
            try {
                info = manager.getPackageInfo(
                        this.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String version = info.versionName;

            SpannableString emailString = new SpannableString(getString(R.string.email));
            ClickableSpan clickableSpanEmail = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.emailAddress) });
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Blindspot application:");
                    emailIntent.setType("message/email");
                    startActivity(Intent.createChooser(emailIntent, "Send Feedback:"));
                }
            };

            SpannableString iconString1 = new SpannableString(getString(R.string.icon1));
            ClickableSpan clickableSpanIcon1 = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.iconarchive.com/"));
                    startActivity(browserIntent);
                }
            };

            SpannableString iconString2 = new SpannableString(getString(R.string.icon2));
            ClickableSpan clickableSpanIcon2 = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.pngwing.com/"));
                    startActivity(browserIntent);
                }
            };

            dialog_about = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_about, null);
            textView_versionName = (TextView)dialog_about.findViewById(R.id.textView_versionName);
            textView_email = (TextView)dialog_about.findViewById(R.id.textView_email);
            textView_icon1 = (TextView)dialog_about.findViewById(R.id.textView_icon1);
            textView_icon2 = (TextView)dialog_about.findViewById(R.id.textView_icon2);

            textView_versionName.setText("Version: " + version);

            emailString.setSpan(clickableSpanEmail,7,emailString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView_email.setText(emailString);
            textView_email.setMovementMethod(LinkMovementMethod.getInstance());

            iconString1.setSpan(clickableSpanIcon1,0,iconString1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView_icon1.setText(iconString1);
            textView_icon1.setMovementMethod(LinkMovementMethod.getInstance());

            iconString2.setSpan(clickableSpanIcon2,0,iconString2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView_icon2.setText(iconString2);
            textView_icon2.setMovementMethod(LinkMovementMethod.getInstance());


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("About");

            builder.setView(dialog_about);
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On activity resume:
     * <br>Sets the voice introduction checkbox in accordance to the user selection
     * if options menu is not null;
     * <br>Adds the NFC adapter to the Foreground Dispatch system if is not null.
     */

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        isToSpeak = settings.getBoolean("speakText",true);
        if (optionsMenu != null){
            optionsMenu.getItem(0).setChecked(isToSpeak);
        }

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /**
     * On activity start:
     * <br>Moves to Main activity if the user selected to stay connected
     * and finishes this activity.
     */

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        boolean isConnected = settings.getBoolean("stayConnected",false);
        if(isConnected){
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        }
    }


    /**
     * When the button is pressed: moves to register activity.
     *
     * @param view Open register screen button.
     */

    public void moveToRegister(View view) {
        Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * When the button is pressed: moves to login activity.
     *
     * @param view Open login screen button.
     */

    public void moveToLogin(View view) {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
