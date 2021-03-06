package io.github.wootwoot1234;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.File;
import java.nio.charset.Charset;


public class WebkitLocalStorageReaderModule extends ReactContextBaseJavaModule {

    public WebkitLocalStorageReaderModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "WebkitLocalStorageReader";
    }

    @ReactMethod
    public void get(Promise promise) {
        WritableMap kv = new WritableNativeMap();
        String dataDir = getReactApplicationContext().getApplicationInfo().dataDir;
        File localstorage = new File(dataDir + "/app_webview/Local Storage/file__0.localstorage");
        File localforage = new File(dataDir + "/databases/localforage");

        if (!localstorage.exists() && !localforage.exists()) {
            promise.resolve(kv);
            return;
        }

        if (localstorage.exists()) {
            readLocalStorage(localstorage, kv, promise);
        } else {
            readLocalForage(localforage, kv, promise);
        }

    }

    private void readLocalStorage(File appDatabase, WritableMap kv, Promise promise) {
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            File dbfile = getReactApplicationContext().getDatabasePath(appDatabase.getPath());
            dbfile.setWritable(true);
            db = SQLiteDatabase.openDatabase(dbfile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);

            String sql = "SELECT key,value FROM ItemTable";
            cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String key = cursor.getString(0);
                byte[] itemByteArray = cursor.getBlob(1);
                String value = new String(itemByteArray, Charset.forName("UTF-16LE"));

                kv.putString(key, value);
                cursor.moveToNext();
            }
            promise.resolve(kv);
        } catch (Exception e) {
            e.printStackTrace();
            promise.resolve(kv);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    private void readLocalForage(File appDatabase, WritableMap kv, Promise promise) {
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            File dbfile = getReactApplicationContext().getDatabasePath(appDatabase.getPath());
            dbfile.setWritable(true);
            db = SQLiteDatabase.openDatabase(dbfile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);

            String sql = "SELECT key,value FROM keyvaluepairs";
            cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                String key = cursor.getString(0);
                String value = cursor.getString(1);

                kv.putString(key, value);

                if (cursor.getString(0).equals("mobileConfigurations")) {
                    break;
                }

                cursor.moveToNext();
            }
            promise.resolve(kv);
        } catch (Exception e) {
            e.printStackTrace();
            promise.resolve(kv);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }
}
