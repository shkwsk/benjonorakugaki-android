package com.example.shkwsk.myapp01;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.UUID;
/**
 * Created by shkwsk on 15/09/30.
 */
public class AppPref {
    private static String uuid = null;
    private static String locations_str = null;
    private static ArrayList<Boolean> locations_flag = new ArrayList<>();
    private static final String UUID_KEY = "UUID_KEY";
    private static final String LOCATIONS_KEY = "LOCATIONS_KEY";

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

    public static ArrayList<Boolean> getLocationFlag(Context context) {
        if (locations_str != null) {// 既にapp内からinvokeされている場合
            System.out.println("already created location_flag.");
            return locations_flag;
        }

        Gson gson = new Gson();
        SharedPreferences sharedPreferences = context.getSharedPreferences(LOCATIONS_KEY, Context.MODE_PRIVATE);
        locations_str = sharedPreferences.getString(LOCATIONS_KEY, null);
        if (locations_str == null) {// 何も設定されていない場合
            System.out.println("create location_flag.");
            for (int i=0; i<Integer.parseInt(context.getString(R.string.location_num)); i++ ) {
                locations_flag.add(false);
            }
            Editor editor = sharedPreferences.edit();
            System.out.println(gson.toJson(locations_flag));
            editor.putString(LOCATIONS_KEY, gson.toJson(locations_flag));
            editor.commit();// 保存
            locations_str = sharedPreferences.getString(LOCATIONS_KEY, null);
        }
        locations_flag = gson.fromJson(locations_str, new TypeToken<ArrayList<Boolean>>(){}.getType());
        return locations_flag;
    }

    public static void setLocationFlag(Context context, String board_id) {
        int id = Integer.parseInt(board_id) - 1;

        SharedPreferences sharedPreferences = context.getSharedPreferences(LOCATIONS_KEY, Context.MODE_PRIVATE);
        String locations = sharedPreferences.getString(LOCATIONS_KEY, null);
        Gson gson = new Gson();
        if (locations == null) {// 何も設定されていない場合
            for (int i=0; i<Integer.parseInt(context.getString(R.string.location_num)); i++ ) {
                locations_flag.add(false);
            }
        }
        locations_flag.set(id, true);
        Editor editor = sharedPreferences.edit();
        editor.putString(LOCATIONS_KEY, gson.toJson(locations_flag));
        editor.commit();// 保存
    }
}
