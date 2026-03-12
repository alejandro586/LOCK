package com.example.lockapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            // Cuando la pantalla se apaga, lanzamos la actividad del PIN
            Intent i = new Intent(context, PinActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(i);
        }
    }
}