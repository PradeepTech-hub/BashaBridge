package com.example.pbl;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.Locale;

public class SpeechHelper {

    public static final int SPEECH_REQUEST_CODE = 100;

    public static void startListening(Activity activity, boolean isHindi) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        
        // Add calling package to improve reliability
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        
        String lang = isHindi ? "hi-IN" : "en-US";
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, lang);
        
        // Increase the timeout for speech recognition to help "catch" the voice better
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L);
        
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        
        try {
            activity.startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Speech recognition is not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    public static void startListening(Activity activity) {
        startListening(activity, false);
    }
}
