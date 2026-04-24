package com.example.pbl;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import java.util.Locale;

public class SpeechHelper {

    public static final int SPEECH_REQUEST_CODE = 100;

    public static void startListening(Activity activity, boolean isHindi) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        if (isHindi) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        }
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        
        try {
            activity.startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startListening(Activity activity) {
        startListening(activity, false);
    }
}
