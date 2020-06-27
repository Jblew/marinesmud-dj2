#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

int outs[] = {9,10,11,3,5,6};
#define FIRST_CHANNEL 12
#define NUM_OF_CHANS 6

#define FIRST_CHANNEL_EXT 18
#define NUM_OF_CHANS_EXT 15

Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();

void setup() {
  pwm.begin();
  pwm.setPWMFreq(200);
  
  for(int i = 0;i < NUM_OF_CHANS;i++) {
    pinMode(outs[i], OUTPUT);
    analogWrite(outs[i], random(50, 200));
  }

  for (uint8_t pwmnum=0; pwmnum < 15; pwmnum++) {
      pwm.setPin(pwmnum, random(1, 4095), true);
  }

  pinMode(13, INPUT);
  
  Serial.begin(57600);
}

unsigned long lastTime;
unsigned long currentTime;
int chan = 0;
void loop() {
  unsigned int v;
  while(true) {
    while(!Serial.available());
    v = Serial.read();
    Serial.write(v);
    Serial.flush();
    //Serial.println(v);

    currentTime = micros();

    if(currentTime-lastTime > 1500) chan = 0;
    //Serial.println((currentTime-lastTime));
    lastTime = currentTime;


    //Serial.print("chan=");
    //Serial.println(chan);

    if(chan-FIRST_CHANNEL < NUM_OF_CHANS) {
      //Serial.print("outs[");
      //Serial.print(outs[chan-FIRST_CHANNEL]);
      //Serial.print("]=");
      //Serial.println((255-v));
      analogWrite(outs[chan-FIRST_CHANNEL], 255-v);
    }
    else if(chan-FIRST_CHANNEL_EXT < NUM_OF_CHANS_EXT) {
      pwm.setPin(chan-FIRST_CHANNEL_EXT, v*16, true);
    }
    
    /*if(ledState == LOW) ledState = HIGH;
    else ledState = LOW;
    digitalWrite(13, ledState);*/

    chan++;
  }
}
