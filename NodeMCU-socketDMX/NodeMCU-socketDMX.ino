
#include <ESP8266WiFi.h>
//#include <WiFiUDP.h>

#include "Arduino.h"

// Includes of Expressif SDK

extern "C"{
  #include "pwm.h"
  #include "user_interface.h"
}
#define PWM_PERIOD 5000
#define PWM_CHANNELS 7


#define CH_G1 5
#define CH_R1 4
#define CH_B1 2
#define CH_U 14

#define CH_G2 12
#define CH_R2 13
#define CH_B2 15

#define LED 2

uint32 io_info[PWM_CHANNELS][3] = {
  // MUX, FUNC, PIN
  {PERIPHS_IO_MUX_GPIO5_U, FUNC_GPIO5,   5}, // D1
  {PERIPHS_IO_MUX_GPIO4_U, FUNC_GPIO4,   4}, // D2
//  {PERIPHS_IO_MUX_GPIO0_U, FUNC_GPIO0,   0}, // D3
  {PERIPHS_IO_MUX_GPIO2_U, FUNC_GPIO2,   2}, // D4
  {PERIPHS_IO_MUX_MTMS_U,  FUNC_GPIO14, 14}, // D5
  {PERIPHS_IO_MUX_MTDI_U,  FUNC_GPIO12, 12}, // D6
  {PERIPHS_IO_MUX_MTCK_U,  FUNC_GPIO13, 13}, // D7
  {PERIPHS_IO_MUX_MTDO_U,  FUNC_GPIO15 ,15}, // D8
                         // D0 - not have PWM :-(
};

// PWM initial duty: all off

uint32 pwm_duty_init[PWM_CHANNELS];



byte id_bit0 = 0x23;
byte id_bit1 = 0x01;

const char* ssid = "TeofileNovum"; const char* password = "fovea@costalis";
//const char* ssid = "Poszefka"; const char* password = "lucyCPH4";
//const char* ssid = "lgora"; const char* password = "AraJumpa20102";
//const char* ssid = "penicure"; const char* password = "kanalizacjaswietlna13";//admin:admin

//const char* host = "192.168.1.11";//lgora
const char* host = "192.168.43.22";//TeofileNovum
#define PORT 5534

void setup(void){
  // preparing GPIOs
  pinMode(LED, OUTPUT);
  pinMode(CH_R1, OUTPUT);
  pinMode(CH_G1, OUTPUT);
  pinMode(CH_B1, OUTPUT);
  pinMode(CH_U, OUTPUT);
  pinMode(CH_R2, OUTPUT);
  pinMode(CH_G2, OUTPUT);
  pinMode(CH_B2, OUTPUT);

  digitalWrite(CH_R1, LOW);
  digitalWrite(CH_G1, LOW);
  digitalWrite(CH_B1, LOW);
  digitalWrite(CH_U, LOW);
  digitalWrite(CH_R2, LOW);
  digitalWrite(CH_G2, LOW);
  digitalWrite(CH_B2, LOW);
  for (uint8_t channel = 0; channel < PWM_CHANNELS; channel++) {
    pwm_duty_init[channel] = 5000;
  }
  uint32_t period = PWM_PERIOD;
  pwm_init(period, pwm_duty_init, PWM_CHANNELS, io_info);
  pwm_start(); //commit
  
  //analogWriteRange(255);
  //analogWriteFreq(512);

  //analogWrite(LED, 127);
  
  delay(1000);
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  Serial.println("");
  //analogWrite(LED, 255);

  // Wait for connection
  uint8_t i = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    i++;
    //if(i%2 == 0) analogWrite(LED, 127);
    //else analogWrite(LED, 255);
  }
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  //analogWrite(LED, 255);
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
    
    /*int r1 = client.read();
    int g1 = client.read();
    int b1 = client.read();
    int r2 = client.read();
    int g2 = client.read();
    int b2 = client.read();
    int u = client.read();*/
    uint16_t r1 = client.read() << 8 | client.read(); //reversed byte order
    uint16_t g1 = client.read() << 8 | client.read();
    uint16_t b1 = client.read() << 8 | client.read();
    uint16_t r2 = client.read() << 8 | client.read();
    uint16_t g2 = client.read() << 8 | client.read();
    uint16_t b2 = client.read() << 8 | client.read();
    uint16_t u = client.read() << 8 | client.read();
    
    if(!client.connected()) {
      client.stop();
      Serial.println("Client disconnected or wrong data. Waiting 5 secs...");
      delay(5000);
      return;
    }
    //
    client.write(id_bit0);
    client.write(id_bit1);
    client.write((byte)ticker);
    client.flush();

    delay(0);

    pwm_set_duty(r1, 0);
    pwm_set_duty(g1, 1);
    pwm_set_duty(b1, 2);
    pwm_set_duty(r2, 3);
    pwm_set_duty(g2, 4);
    pwm_set_duty(b2, 5);
    pwm_set_duty(u, 6);
    pwm_start(); // commit
    
    /*analogWrite(CH_R1, r1);
    analogWrite(CH_G1, g1);
    analogWrite(CH_B1, b1);
    analogWrite(CH_U, u);

    analogWrite(CH_R2, r2);
    analogWrite(CH_G2, g2);
    analogWrite(CH_B2, b2);*/

    //if(ticker % 2 == 0) analogWrite(LED, 127);
    //else analogWrite(LED, 255);
    
    delay(0);

    /*if(ticker % 20 == 0) {
      client.write(0x204);
      client.write(0x255);
      client.flush();
    }*/
    
    ticker++;
  }
} 
