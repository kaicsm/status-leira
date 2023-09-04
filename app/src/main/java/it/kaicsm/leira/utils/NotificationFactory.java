package it.kaicsm.leira.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;

public class NotificationFactory {

  // Os IDs devem ser gerados de forma sequencial
  // esse erro é de proposito.
  private static final int NOTIFICATION_ID = 1;
  
  private static final String NOTIFICATION_CHANNEL = "StatusLeira";

  private Context ctx;

  public NotificationFactory(Context ctx) {
    this.ctx = ctx;
  }

  public void setup() {
    // Verificar e solicitar permissão para notificações, se necessário
    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          (Activity) ctx, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "Status Leira Channel";
      String description = "Estado da leira da escola ETE LUIS ALVEZ LACERDA";
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, name, importance);
      channel.setDescription(description);

      NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  public void push(String content, int icon) {
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL)
            .setSmallIcon(icon) // Ícone da notificação
            .setContentText(content)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    NotificationManager notificationManager =
        (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(NOTIFICATION_ID, builder.build());
  }

  public static Notification createNotification(Context ctx, String content, int icon) {
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL)
            .setSmallIcon(icon) // Ícone da notificação
            .setContentText(content)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    return builder.build();
  }
}
