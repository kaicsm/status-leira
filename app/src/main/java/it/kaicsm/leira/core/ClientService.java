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

    // Inicialize o cliente aqui
    client = new Client(IP, PORT);

    // Crie uma nova HandlerThread para lidar com as mensagens em segundo plano
    backgroundThread = new HandlerThread("ClientServiceBackground");
    backgroundThread.start();

    // Crie um novo Handler associado à HandlerThread para enviar as mensagens para a Activity (ou
    // outro componente)
    messageHandler =
        new Handler(backgroundThread.getLooper()) {
          @Override
          public void handleMessage(android.os.Message msg) {
            // Receba a mensagem do servidor e acesse a função getMessage() do cliente
            if (client.getMessage() != null) {
              String message = client.getMessage();

              int number = Integer.parseInt(message);
              if (number >= TEMPERATURE_LIMIT) {
                Notification notification =
                    NotificationFactory.createNotification(
                        getApplicationContext(), message + "°C Reafriar leira!", R.drawable.leira);

                // Use um NotificationManager para exibir a notificação
                NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // Defina um ID exclusivo para a notificação
                int notificationId = 501;

                // Exiba a notificação
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

    // Inicie uma tarefa em segundo plano para buscar mensagens do servidor a cada 1 segundo
    messageHandler.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            // Chame a função que lida com as mensagens do servidor
            messageHandler.sendEmptyMessage(0);

            // Agende a próxima execução após 1 segundo
            messageHandler.postDelayed(this, 1000);
          }
        },
        1000);

    // Inicie o Client como um Runnable em uma Thread separada
    Thread clientThread = new Thread(client);
    clientThread.start();

    // O serviço não deve ser reiniciado automaticamente se for morto pelo sistema
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // Pare a HandlerThread quando o serviço for destruído
    if (backgroundThread != null) {
      backgroundThread.quitSafely();
    }
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }
}
