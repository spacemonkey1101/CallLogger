package com.example.calllogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_LOG = 108;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_LOG) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readCallLog();
            } else {
                Toast.makeText(this, "This Permission was not granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_CALL_LOG) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            readCallLog();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG)) {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                Toast.makeText(this, "We need your call log for better user experience", Toast.LENGTH_SHORT).show();
            }
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_CALL_LOG);

        }
    }

    private String readCallLog() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Cursor cursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        while (cursor.moveToNext()) {
            String phNumber = cursor.getString(number);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            SimpleDateFormat ft =
                    new SimpleDateFormat ("yyyy.MM.dd");

            String callDuration = cursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }


            String json = "{\"phone number\":"+ phNumber +",\"callDayTime\":"+ft.format(callDayTime)+",\"callDuration\":"+callDuration+"}";

            try {

                JSONObject obj = new JSONObject(json);

                Log.d("My App", obj.toString());

            } catch (Throwable tx) {
                Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
            }

        }


        return null;
    }
}

