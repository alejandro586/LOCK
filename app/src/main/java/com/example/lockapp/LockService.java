package com.example.lockapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class LockService extends Service {

    private ScreenReceiver screenReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        screenReceiver = new ScreenReceiver();

        // Filtro para detectar cuando se apaga la pantalla o se intenta cerrar diálogos del sistema
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        // Usamos ContextCompat para registrar el receptor con los flags necesarios en Android 14+
        // Se usa RECEIVER_EXPORTED porque ACTION_CLOSE_SYSTEM_DIALOGS es un broadcast del sistema no protegido
        ContextCompat.registerReceiver(this, screenReceiver, filter, ContextCompat.RECEIVER_EXPORTED);

        // Crear canal de notificación para el servicio en primer plano (Android Oreo+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("lock_service", "Servicio de Bloqueo", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, "lock_service")
                .setContentTitle("Lock App")
                .setContentText("Protección activa")
                .setSmallIcon(R.mipmap.ic_logo)
                .setOngoing(true)
                .build();

        // Iniciar servicio en primer plano
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (screenReceiver != null) {
            unregisterReceiver(screenReceiver);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
