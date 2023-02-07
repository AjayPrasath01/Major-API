

//=========================================== Don't Touch these values ========================================================
#include "ESP8266WiFi.h"
#include "WiFiClient.h"
#include <ESP8266HTTPClient.h>

#define CHARTTYPE "sd"
#define SERIAL_PORT 115200
const String SERVER_URL = "http://122.174.237.0:9990";
const String SSID = SSIDNAME;
const String PASSWORD = SSIDPASSWORD;
const String MACHINENAME = IMPORTANT;

WiFiClient wificlient;
HTTPClient http;

void connectWiFi(){
  // Connect to Wi-Fi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.print(SSID);
  Serial.println(".....");
  
  WiFi.begin(SSID, PASSWORD);
  WiFi.mode(WIFI_STA); 
  delay(100);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("Wi-Fi connected successfully");

  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.print("RSSI: ");
  Serial.println(WiFi.RSSI());

}

bool sendData(String dataValue, String dataType, String sensorName){
  String url = SERVER_URL + "/setter/data?dataValue=" + dataValue + "&dataType=" + dataType + "&machineName=" + MACHINENAME + "&sensorName=" + sensorName + "&sensorType=" + CHARTTYPE;
  http.begin(wificlient, url);
  int code = http.GET();
  Serial.println(code);
  if (code == 200){
    return true;    
  }else{
    Serial.println("Some error occured");
    return false;
  }
}

//================================================================================================================================




void setup() {
  /* Dont remove the below function */
  connectWiFi();
  Serial.begin(SERIAL_PORT);
  //=================================
  // put your setup code here, to run once:

}

void loop() {  
  // put your main code here, to run repeatedly:

  //Please Send data for every millisecond maximum one data per 30 second


}
