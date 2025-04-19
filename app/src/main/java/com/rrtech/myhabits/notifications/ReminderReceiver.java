package com.rrtech.myhabits.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.rrtech.myhabits.R;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String habitName = intent.getStringExtra("habitName");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "habit_channel")
                .setSmallIcon(R.drawable.ic_notification) // ⚠️ à créer si besoin
                .setContentTitle("Rappel d’habitude")
                .setContentText("N’oublie pas : " + habitName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(habitName.hashCode(), builder.build());
    }
}
