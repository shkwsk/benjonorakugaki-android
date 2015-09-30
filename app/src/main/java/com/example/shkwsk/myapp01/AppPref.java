package com.example.shkwsk.myapp01;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.UUID;
/**
 * Created by shkwsk on 15/09/30.
 */
public class AppPref {
    private static String uuid = null;
    private static final String UUID_KEY = "UUID_KEY";

    public static String getUUID(Context context) {
        if (uuid != null) {// 既にapp内からinvokeされている場合
            return uuid;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(UUID_KEY, Context.MODE_PRIVATE);
        uuid = sharedPreferences.getString(UUID_KEY, null);
        if (uuid == null) {// 何も設定されていない場合
            uuid = UUID.randomUUID().toString();// randomな文字列を生成
            Editor editor = sharedPreferences.edit();
            editor.putString(UUID_KEY, uuid);
            editor.commit();// 保存
        }
        return uuid;
    }

    public static String getLocationFlag(Context context) {
        if (uuid != null) {// 既にapp内からinvokeされている場合
            return uuid;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(UUID_KEY, Context.MODE_PRIVATE);
        uuid = sharedPreferences.getString(UUID_KEY, null);
        if (uuid == null) {// 何も設定されていない場合
            uuid = UUID.randomUUID().toString();// randomな文字列を生成
            Editor editor = sharedPreferences.edit();
            editor.putString(UUID_KEY, uuid);
            editor.commit();// 保存
        }
        return uuid;
    }
}
