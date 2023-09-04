package it.kaicsm.leira.core;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {
  
  private static final String TAG  = "Client";
  
  private String serverIP;
  private int serverPort;
  private volatile String receivedMessage;

  public Client(String serverIP, int serverPort) {
    this.serverIP = serverIP;
    this.serverPort = serverPort;
  }

  @Override
  public void run() {
    while (true) {
      try (Socket socket = new Socket(serverIP, serverPort);
          BufferedReader bufferedReader =
              new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

        String line;
        while ((line = bufferedReader.readLine()) != null) {
          // remove 31(.40) for example
          receivedMessage = line.substring(0, line.length() - 3);
          //Log.i(TAG, "Received: " + receivedMessage);
        }

      } catch (UnknownHostException e) {
        Log.i(TAG, "Unknown host:" + e.getMessage());
      } catch (IOException e) {
        Log.i(TAG, "IO error: " + e.getMessage());
      }

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public String getMessage() {
    return receivedMessage;
  }
}
