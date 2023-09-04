package it.kaicsm.leira.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import it.kaicsm.leira.R;
import it.kaicsm.leira.utils.NotificationFactory;

public class ClientService extends Service {

  private static final String TAG = "ClientService";

  private static final int TEMPERATURE_LIMIT = 30;
  private static final String IP = "192.168.3.26";
  private static final int PORT = 7070;

  private Client client;
  private Handler messageHandler;

  private HandlerThread backgroundThread;

  @Override
  public void onCreate() {
    super.onCreate();

    client = new Client(IP, PORT);

    backgroundThread = new HandlerThread("ClientServiceBackground");
    backgroundThread.start();

    messageHandler =
        new Handler(backgroundThread.getLooper()) {
          @Override
          public void handleMessage(android.os.Message msg) {
            // Receber as mensagem do servidor e acesse a função getMessage() do cliente
            if (client.getMessage() != null) {
              String message = client.getMessage();

              int number = Integer.parseInt(message);
              if (number >= TEMPERATURE_LIMIT) {
                Notification notification =
                    NotificationFactory.createNotification(
                        getApplicationContext(), message + "°C Reafriar leira!", R.drawable.leira);

                NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                int notificationId = 501;

                // Exibir a notificação
                notificationManager.notify(notificationId, notification);
              }

              Intent intent = new Intent("get_message");
              intent.putExtra("message", message);
              sendBroadcast(intent);
              Log.d(TAG, message);
            }
          }
        };
  }

  @Override
  public int onStartCommand(Intent arg0, int arg1, int arg2) {
    Notification notificaton =
        NotificationFactory.createNotification(getApplicationContext(), "Serviço em execução", R.drawable.leira);
    startForeground(500, notificaton);

    // Iniciar uma tarefa em segundo plano para buscar mensagens do servidor a cada 1 segundo
    messageHandler.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            messageHandler.sendEmptyMessage(0);

            // Agendae a próxima execução
            messageHandler.postDelayed(this, 1000);
          }
        },
        1000);

    // Iniciar o Client como um Runnable em uma Thread separada
    Thread clientThread = new Thread(client);
    clientThread.start();

    // O serviço não deve ser reiniciado automaticamente se for morto pelo sistema
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (backgroundThread != null) {
      backgroundThread.quitSafely();
    }
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }
}
