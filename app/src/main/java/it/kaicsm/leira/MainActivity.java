package it.kaicsm.leira;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import it.kaicsm.leira.core.ClientService;
import it.kaicsm.leira.databinding.ActivityMainBinding;
import it.kaicsm.leira.utils.NotificationFactory;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "App";

  private static final long UPDATE_INTERVAL = 1000;

  private ActivityMainBinding binding;
  private TextView textMain;
  private Typeface fontFamily;

  private NotificationFactory notify;

  private Timer timer;

  private volatile String message;
  private BroadcastReceiver broadcastRecv =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
          // TODO: Implement this method
          if ("get_message".equals(intent.getAction())) {
            // Eu quero receber essa mensagsm a cada 1 segundo
            message = intent.getStringExtra("message");
            Log.d(TAG, message);
          } else {
            Log.d(TAG, "Algo esta errado");
          }
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Inflate and get instance of binding
    binding = ActivityMainBinding.inflate(getLayoutInflater());

    // set content view to binding's root
    setContentView(binding.getRoot());

		Intent serviceIntent = new Intent(getApplicationContext(), ClientService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(serviceIntent);
    } else {
      startService(serviceIntent);
    }
	
    notify = new NotificationFactory(this);
    notify.setup();

    fontFamily = Typeface.createFromAsset(getAssets(), "font.ttf");

    textMain = binding.textStatus;
    textMain.setTextSize(30);
    textMain.setTypeface(fontFamily);
    textMain.setText("----");

		final Handler handler = new Handler();
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Atualize o textMain com a mensagem recebida
                    textMain.setText(message != null ? message+"°C" : "Conectando...");
                }
            });
        }
    }, 0, UPDATE_INTERVAL);
     
  }

  @Override
  protected void onStart() {
    super.onStart();
    // TODO: Implement this method
    IntentFilter filter = new IntentFilter("get_message");
    registerReceiver(broadcastRecv, filter);
  }
	
  @Override
  protected void onStop() {
    super.onStop();
    // TODO: Implement this method
    unregisterReceiver(broadcastRecv);
  }
  

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
  }
}
