package edu.hebut.retrofittest;

import android.app.Application;
import android.content.Context;

import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;

public class MyApp extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);
        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
}
