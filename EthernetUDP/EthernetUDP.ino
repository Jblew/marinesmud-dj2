/*
   UIPEthernet UdpClient example.

   UIPEthernet is a TCP/IP stack that can be used with a enc28j60 based
   Ethernet-shield.

   UIPEthernet uses the fine uIP stack by Adam Dunkels <adam@sics.se>

        -----------------

   This UdpClient example tries to send a packet via udp to 192.168.0.1
   on port 5000 every 5 seconds. After successfully sending the packet it
   waits for up to 5 seconds for a response on the local port that has been
   implicitly opened when sending the packet.

   Copyright (C) 2013 by Norbert Truchsess (norbert.truchsess@t-online.de)
*/

#include <UIPEthernet.h>

EthernetUDP udp;
unsigned long next;

const uint8_t id = 0;
#define TIMEOUT_MS 500

void setup() {
  pinMode(3, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(9, OUTPUT);
  analogWrite(3, 253);
  analogWrite(5, 253);
  analogWrite(6, 253);
  analogWrite(9, 253);
  
  Serial.begin(9600);

  uint8_t mac[6] = {0xB0, 0xF1, 0xA2, 0xC3, 0x08, 0x00 + id}; //last: 0+i

  Ethernet.begin(mac, IPAddress(192, 168, 1, 10 + id)); //last: 10+i


  next = millis() + 2000;

}

byte recvBuf[5];
byte sendBuf[5];
void loop() {
  int success;
  int len = 0;
  byte received = 0;

  /*********RECEIVE********/
  next = millis() + TIMEOUT_MS;
  success = udp.begin(5759);
  if (!success) Serial.println("Cannot listen on 5759");
  else {
    do
    {
      //check for new udp-packet:
      success = udp.parsePacket();
    }
    while (!success && ((signed long)(millis() - next)) < 0);
    if (!success) Serial.println("RECV Timeout");
    else {
      do
      {
        int c = udp.read();
        if (len < 5) recvBuf[len] = c;
        len++;
      }
      while ((success = udp.available()) > 0);
      if (len >= 5) received = 1;
    }
    udp.flush();
  }
  if(received > 0) {
    analogWrite(3, recvBuf[0]);
    analogWrite(5, recvBuf[1]);
    analogWrite(6, recvBuf[2]);
    analogWrite(9, recvBuf[3]);
    //Serial.println("Received packet "+(uint8_t)recvBuf[4]);
  }

  sendBuf[0] = 65;
  sendBuf[1] = received + ' ';
  sendBuf[2] = id;
  sendBuf[3] = (received > 0 ? recvBuf[4] : 0);
  sendBuf[4] = 0;

  


  /******SEND******/
  next = millis() + TIMEOUT_MS;
  do
  {
    success = udp.beginPacket(IPAddress(192, 168, 1, 2), 5761);
    //beginPacket fails if remote ethaddr is unknown. In this case an
    //arp-request is send out first and beginPacket succeeds as soon
    //the arp-response is received.
  }
  while (!success && ((signed long)(millis() - next)) < 0);
  if (success) {
    success = udp.write(sendBuf, 5);
    if(!success) Serial.println("Error on udp.write");
    success = udp.endPacket();
    if(!success) Serial.println("Error on endPacket");
    //else Serial.println("Sent packet");
  }
  else Serial.println("Error on beginPacket");

  udp.stop();

  delay(0);
}
