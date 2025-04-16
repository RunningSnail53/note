package edu.hebut.retrofittest.Util;

import android.content.Context;
import android.content.SharedPreferences;

public class NumberManager {
    private static final String PREF_NAME = "ShareCounter";
    private static final String KEY_COUNTER = "counter";

    private final SharedPreferences sharedPreferences;

    public NumberManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 获取当前序号并自增
    public synchronized int getAndIncrement() {
        int current = sharedPreferences.getInt(KEY_COUNTER, 1);
        sharedPreferences.edit().putInt(KEY_COUNTER, current + 1).apply();
        return current;
    }

    // 重置序号（可选）
    public void resetCounter() {
        sharedPreferences.edit().putInt(KEY_COUNTER, 1).apply();
    }
}