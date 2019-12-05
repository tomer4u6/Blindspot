package com.example.blindspot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.Locale;


/**
 * @author Tomer Ben Ari
 * @version 0.2.0
 * @since 0.2.0 (05/12/2019)
 * short description:
 * This activity is the first one the user sees when he opens the app.
 * You can get from it to the Login activity or Register activity.
 */


public class WelcomeActivity extends AppCompatActivity {

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getSupportActionBar().setTitle("");

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    tts.speak(getString(R.string.welcomeText), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        tts.stop();
    }
}
