// Present a "Will be back soon web page", as stand-in webserver.
// 2011-01-30 <jc@wippler.nl> http://opensource.org/licenses/mit-license.php
 
#include <EtherCard.h>

#define STATIC 1  // set to 1 to disable DHCP (adjust myip/gwip values below)

#if STATIC
// ethernet interface ip address
static byte myip[] = { 192,168,77,51 };
// gateway ip address
static byte gwip[] = { 192,168,77,1 };
#endif

// ethernet mac address - must be unique on your network
static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };

byte Ethernet::buffer[500]; // tcp/ip send and receive buffer

//callback that prints received packets to the serial port
void processMMUDP(uint16_t dest_port, uint8_t src_ip[IP_LEN], uint16_t src_port, const char *data, uint16_t len){
  if(len > 4) {
    Serial.print("Pckt no ");
    Serial.println((int)data[4]);
    Serial.print("src_ip: ");
    ether.printIp(src_ip);
    
    byte resp [1];
    resp[0] = data[4];
    //static void sendUdp (char *data,uint8_t len,uint16_t sport, uint8_t *dip, uint16_t dport);      
    ether.sendUdp(resp, 1, 5760, src_ip, 5761);
  }  
}

void setup(){
  Serial.begin(57600);
  Serial.println(F("\n[backSoon]"));

  if (ether.begin(sizeof Ethernet::buffer, mymac) == 0)
    Serial.println(F("Failed to access Ethernet controller"));
#if STATIC
  ether.staticSetup(myip, gwip);
#else
  if (!ether.dhcpSetup())
    Serial.println(F("DHCP failed"));
#endif

  ether.printIp("IP:  ", ether.myip);
  ether.printIp("GW:  ", ether.gwip);
  ether.printIp("DNS: ", ether.dnsip);

  ether.udpServerListenOnPort(&processMMUDP, 5759);
}

void loop(){
  //this must be called for ethercard functions to work.
  ether.packetLoop(ether.packetReceive());
}
