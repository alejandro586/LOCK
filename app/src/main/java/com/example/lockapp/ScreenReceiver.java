package com.example.lockapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            launchPinActivity(context);
        } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
            String reason = intent.getStringExtra("reason");
            // "globalactions" es el motivo cuando se intenta abrir el menú de apagado
            if (reason != null && (reason.equals("globalactions") || reason.equals("homekey"))) {
                launchPinActivity(context);
            }
        }
    }

    private void launchPinActivity(Context context) {
        Intent i = new Intent(context, PinActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(i);
    }
}