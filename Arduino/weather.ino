#include <DHT.h>
#include <EEPROM.h>
#include <Time.h>

#define PIN_DHT 8
#define PIN_CONNECT 9
#define PIN_RESET 2
#define PIN_LED 13

#define MESSAGE_SET_TIME 1
#define MESSAGE_SAVE_TIME 2
#define MESSAGE_GET_DATA 3
#define MESSAGE_RESET 4

struct Record
{
  unsigned long time;
  byte temperature;
  byte humidity;
};

DHT dht(8, DHT11);
const int measureDelay = 60 * 30;

void setup()
{
  Serial.begin(9600);
  dht.begin();
  pinMode(PIN_CONNECT, INPUT_PULLUP);
  pinMode(PIN_RESET, INPUT_PULLUP);
  pinMode(PIN_LED, OUTPUT);

  int addrTime = EEPROM.length() - 7;
  time_t time;
  EEPROM.get(addrTime, time);
  setTime(time);
}

void loop()
{
  digitalWrite(PIN_LED, HIGH);
  collectData();
  delay(1000);
  digitalWrite(PIN_LED, LOW);
  
  unsigned long lastTime = now();
  while(now() < lastTime + measureDelay)
  {
    if(Serial.available() > 0)
    {
      int message = Serial.read();
      switch(message)
      {
      case MESSAGE_SET_TIME:
        readTime();
        break;
      case MESSAGE_SAVE_TIME:
      {
        int addrTime = EEPROM.length() - 7;
        EEPROM.put(addrTime, now());
        break;
      }
      case MESSAGE_GET_DATA:
        sendData();
        break;
      case MESSAGE_RESET:
        clearAll();
        break;
      case 65:
        for(int i = 0; i < 1024; i++) Serial.println(EEPROM.read(i));
      }
    }
    delay(500);
  }
}

void readTime()
{
  while(Serial.available() != 4) delay(10);
  time_t time = bytesToLong(Serial.read(), Serial.read(), Serial.read(), Serial.read());
  setTime(time);
}

void sendData()
{
  int length = getLength();
  Serial.write(length);
  for(int i = 0; i < length; i++)
  {
    int addr = i * sizeof(Record);
    Record record;
    EEPROM.get(addr, record);

    byte time[4];
    longToBytes(record.time, time);
    Serial.write(time, 4);
    Serial.write(record.temperature);
    Serial.write(record.humidity);
  }
}

void collectData()
{
  struct Record record;
  record.time = now();
  record.temperature = dht.readTemperature();
  record.humidity = dht.readHumidity();

  int length = getLength();
  int addr = length * sizeof(Record);
  EEPROM.put(addr, record);

  int addrLength = EEPROM.length() - 2;
  int addrEmpty = EEPROM.length() - 3;
  
  EEPROM.put(addrLength, ++length);
  EEPROM.update(addrEmpty, 0);
}

int getLength()
{
  int addrLength = EEPROM.length() - 2;
  int addrEmpty = EEPROM.length() - 3;
  
  int length;
  EEPROM.get(addrLength, length);
  if(EEPROM.read(addrEmpty) == 255) length = 0;
  
  return length;
}

void clearAll()
{
  for(int i = 0; i < EEPROM.length(); i++) EEPROM.update(i, 255);
}

unsigned long bytesToLong(int first, int second, int third, int fourth)
{
  return (((unsigned long) first  << 24) & 0xff000000) |
         (((unsigned long) second << 16) & 0xff0000) |
         (((unsigned long) third  << 8 ) & 0xff00) |
          ((unsigned long) fourth        & 0xff);
}

void longToBytes(unsigned long number, byte* bytes)
{
  bytes[0] = (number >> 24) & 0xff;
  bytes[1] = (number >> 16) & 0xff;
  bytes[2] = (number >> 8 ) & 0xff;
  bytes[3] =  number        & 0xff;
}
