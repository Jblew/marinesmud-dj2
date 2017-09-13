package pl.jblew.mmdj2;


import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * UWAGI:
 * - gdy bluetooth jest włączony na macbooku, występują lagi
 * @author teofil
 */
public class DMXSocketTest {

    int serverPort = 5534;
    int fps = 18;

    public DMXSocketTest() throws IOException {
        ServerSocket serverSocket = new ServerSocket(serverPort);

        //int i = 0;
        while (true) {
            Socket clientSocket = serverSocket.accept();
            clientSocket.setKeepAlive(true);
            clientSocket.setTcpNoDelay(true);
            //clientSocket.setTrafficClass(0x10);
            clientSocket.setTrafficClass(0x08);
            clientSocket.setReceiveBufferSize(3);
            clientSocket.setSendBufferSize(14);

            System.out.println("New client!");
            new Thread(() -> {
                int[] times = new int[150];
                int pos = 0;

                try {
                    /*
                    Buffered streams do all the magic! Why? 
                     */
                    BufferedInputStream is = new BufferedInputStream(clientSocket.getInputStream());
                    BufferedOutputStream os = new BufferedOutputStream(clientSocket.getOutputStream());

                    System.out.println("Client in thread!");
                    float hue = 0f;

                    while (true) {
                        hue += 0.004f;
                        if (hue > 1f) {
                            hue = 0;
                        }

                        try {
                            //System.out.println("v = "+v);
                            long sTime = System.currentTimeMillis();

                           // double vald = Math.pow(d, 2d);
                            //int val = (int) Math.floor(4999d * vald);

                            //byte b0 = (byte) (val >>> 8);
                            //byte b1 = (byte) (val);

                            Color c = Color.getHSBColor(hue, 1f, 1f);
                            int r255 = c.getRed();
                            int g255 = c.getGreen();
                            int b255 = c.getBlue();
                            double rd = (double)r255/255d;///Math.pow((double) r255 / 255d, 2d);
                            double gd = (double)g255/255d;//Math.pow((double) g255 / 255d, 2d);
                            double bd = (double)b255/255d;//Math.pow((double) b255 / 255d, 2d);
                            int r5000 = (int) Math.floor(rd * 5000d);
                            int g5000 = (int) Math.floor(gd * 5000d);
                            int b5000 = (int) Math.floor(bd * 5000d);
                            byte rb0 = (byte) (r5000 >>> 8);
                            byte rb1 = (byte) (r5000);
                            byte gb0 = (byte) (g5000 >>> 8);
                            byte gb1 = (byte) (g5000);
                            byte bb0 = (byte) (b5000 >>> 8);
                            byte bb1 = (byte) (b5000);

                            /*
                            CHAN 00 -> GREEN_UP
                            CHAN 01 -> RED_UP
                            CHAN 02 -> none
                            CHAN 03 -> none
                            CHAN 04 ->  BLUE_BOTH
                            CHAN 05 -> RED BOTTOM
                            CHAN 06 -> GREEN_BOTTOM
                             */
                            byte[] out = new byte[14];
                            out[0] = gb0;
                            out[1] = gb1;
                            out[2] = rb0;
                            out[3] = rb1;
                            out[4] = 0;
                            out[5] = 0;
                            out[6] = 0;
                            out[7] = 0;
                            out[8] = bb0;
                            out[9] = bb1;
                            out[10] = rb0;
                            out[11] = rb1;
                            out[12] = gb0;
                            out[13] = gb1;

                            os.write(out);
                            os.flush();

                            byte[] recvBuf = new byte[3];
                            is.read(recvBuf);

                            long ellapsedTime = (System.currentTimeMillis() - sTime);
                            times[pos] = (int) ellapsedTime;
                            pos++;
                            if (pos > times.length - 1) {
                                printStats(times);
                                pos = 0;
                            }

                            //System.out.println("recv = "+recvBuf[0]);
                            //System.out.println(" > in "+ellapsedTime+"ms");
                            long timeToSleep = (1000 / fps) - ellapsedTime;

                            if (timeToSleep > 0) {
                                TimeUnit.MILLISECONDS.sleep(timeToSleep);
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(DMXSocketTest.class.getName()).log(Level.SEVERE, null, ex);
                            break;
                        }

                    }
                    System.out.println("Finished. Closing socket...");
                    is.close();
                    os.close();
                    clientSocket.close();

                } catch (Exception ex) {
                    Logger.getLogger(DMXSocketTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        }
        /*clientSocket.getOutputStream().write((25*i)%255);
            clientSocket.getOutputStream().flush();
            clientSocket.close();
            i++;*/
    }

    public static void main(String[] args) throws IOException {
        new DMXSocketTest();
    }

    private void printStats(int[] times) {
        int sum = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        int numOfLags = 0;
        int lagTimeMs = 0;

        for (int i = 0; i < times.length; i++) {
            int v = times[i];
            sum += v;

            if (v > max) {
                max = v;
            }
            if (v < min) {
                min = v;
            }

            if (v > (1000 / fps)) {
                numOfLags++;
                lagTimeMs += v;
            }
        }

        int avg = sum / times.length;

        double sumOfSDs = 0;
        for (int i = 0; i < times.length; i++) {
            int v = times[i];
            sumOfSDs += Math.pow((double) v - (double) avg, 2d);
        }

        double sd = Math.sqrt(sumOfSDs / times.length);

        System.out.println("Max = " + max);
        System.out.println("Min = " + min);
        System.out.println("Avg = " + avg);
        System.out.println("SD = " + sd);
        System.out.println("lags = " + numOfLags + "/" + times.length + " = " + ((double) numOfLags / (double) times.length * 100d) + "%");
        System.out.println("lagTime = " + lagTimeMs + "ms = " + ((double) lagTimeMs / (double) (fps * times.length) * 100d) + "% of time");
    }

}

/*
ARDUINO C:


#include <ESP8266WiFi.h>
//#include <WiFiUDP.h>

const char* ssid = "TeofileNovum"; const char* password = "fovea@costalis";
//const char* ssid = "Poszefka"; const char* password = "lucyCPH4";
//const char* ssid = "lgora"; const char* password = "AraJumpa20102";
//const char* ssid = "penicure"; const char* password = "kanalizacjaswietlna13";//admin:admin

//const char* host = "192.168.1.11";//lgora
const char* host = "192.168.43.22";//TeofileNovum
#define PORT 5534

#define LED 2
#define BRIGHT    350


void setup(void){
  // preparing GPIOs
  pinMode(LED, OUTPUT);
  analogWriteRange(512);
  analogWriteFreq(256);
  
  
  delay(1000);
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  Serial.println("");

  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

}
 
void loop(void){
  Serial.println("Connecting to server...");
  WiFiClient client;
  if (!client.connect(host, PORT)) {
    Serial.println("Waiting 5 secs...");
    delay(5000);
    return;
  }

  uint8_t ticker = 0;
  while(true) {
    while(!client.available() && client.connected()) delayMicroseconds(10);
    int data = client.read();
    int data2 = client.read();
    int data3 = client.read();
    int data4 = client.read();
    if(data == -1 || data > 255 || data < 0 || !client.connected()) {
      client.stop();
      Serial.println("Client disconnected or wrong data. Waiting 5 secs...");
      delay(5000);
      return;
    }
    //
    client.write((byte)data2);
    client.write((byte)data3);
    client.write((byte)data4);
    client.flush();
    
    analogWrite(LED, 512-data);
    delay(0);
    
    ticker++;
  }
} 
*/
