package com.example.pbl;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

public class TTSHelper {
    private static final String TAG = "TTSHelper";
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private Locale pendingLocale = Locale.US;

    public TTSHelper(Context context) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(pendingLocale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported: " + pendingLocale);
                } else {
                    isInitialized = true;
                }
            } else {
                Log.e(TAG, "Initialization failed");
            }
        });
    }

    public void setLanguage(Locale locale) {
        this.pendingLocale = locale;
        if (isInitialized) {
            int result = tts.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported: " + locale);
                isInitialized = false;
            }
        }
    }

    public void speak(String text) {
        if (isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Log.e(TAG, "TTS not initialized or language not supported");
        }
    }

    public void setSpeechRate(float rate) {
        if (tts != null) {
            tts.setPitch(1.0f);
            tts.setSpeechRate(rate);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
