package com.example.pbl;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dkmp97ix2"); // Updated to the cloud name found in logs
        config.put("secure", "true");
        MediaManager.init(this, config);
    }
}
