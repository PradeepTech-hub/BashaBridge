package com.example.pbl;

import android.content.Context;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (!isInitialized) {
            Map config = new HashMap();
            config.put("cloud_name", "dkmp97ix2");
            config.put("secure", true);
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }
}
