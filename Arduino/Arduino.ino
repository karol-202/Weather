#include <DHT.h>
#include <EEPROM.h>
#include <Time.h>
#include <LiquidCrystal.h>

#define PIN_BUTTON_LIGHT 2
#define PIN_LCD_RS 3
#define PIN_LCD_E 4
#define PIN_LCD_D0 5
#define PIN_LCD_D1 6
#define PIN_LCD_D2 7
#define PIN_LCD_D3 8
#define PIN_DHT 9
#define PIN_LED_RED 10
#define PIN_LED_GREEN 11
#define PIN_LED_BLUE 12
#define PIN_LIGHT 13

#define MESSAGE_SET_TIME 1
#define MESSAGE_SAVE_TIME 2
#define MESSAGE_GET_DATA 3
#define MESSAGE_RESET 4

#define MEASURE_DELAY 1800 //30 minut

struct Record
{
  unsigned long time;
  byte temperature;
  byte humidity;
};

DHT dht(PIN_DHT, DHT11);
LiquidCrystal lcd(PIN_LCD_RS, PIN_LCD_E, PIN_LCD_D0, PIN_LCD_D1, PIN_LCD_D2, PIN_LCD_D3);
int measureDelay;
bool lightEnabled;

void setup()
{
  Serial.begin(9600);
  dht.begin();
  lcd.begin(16, 2);
  pinMode(PIN_LED_RED, OUTPUT);
  pinMode(PIN_LED_GREEN, OUTPUT);
  pinMode(PIN_LED_BLUE, OUTPUT);
  pinMode(PIN_BUTTON_LIGHT, INPUT_PULLUP);
  pinMode(PIN_LIGHT, OUTPUT);

  int addrTime = EEPROM.length() - 7;
  time_t time;
  EEPROM.get(addrTime, time);
  setTime(time);

  measureDelay = 10; //Czekaj 10 sekund przed pierwszym zapisem
  lightEnabled = true;

  digitalWrite(PIN_LED_RED, HIGH);
  digitalWrite(PIN_LED_GREEN, HIGH);
  digitalWrite(PIN_LED_BLUE, HIGH);
  digitalWrite(PIN_LIGHT, HIGH);

  attachInterrupt(digitalPinToInterrupt(PIN_BUTTON_LIGHT), toggleLight, FALLING);
}

void loop()
{
  unsigned long lastTime = now();
  while(now() < lastTime + measureDelay)
  {
    if(Serial.available() > 0)
    {
      int message = Serial.read();
      switch(message)
      {
      case MESSAGE_SET_TIME:
        updateTime();
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
      }
    }
    updateLCD();
    delay(500); //Czekaj 0.5s przed kolejnym sprawdzeniem
  }

  digitalWrite(PIN_LED_GREEN, LOW);
  collectData();
  delay(1000);
  digitalWrite(PIN_LED_GREEN, HIGH);
  measureDelay = MEASURE_DELAY - 1; //1 - czas zużyty na świecenie diody LED
}

void updateTime()
{
  digitalWrite(PIN_LED_RED, LOW);
  while(Serial.available() < 4) delay(10);
  int first = Serial.read();
  int second = Serial.read();
  int third = Serial.read();
  int fourth = Serial.read();
  time_t time = bytesToLong(first, second, third, fourth);
  setTime(time);
  digitalWrite(PIN_LED_RED, HIGH);
}

void sendData()
{
  int length = getLength();
  Serial.write((byte) length);
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
  digitalWrite(PIN_LED_BLUE, LOW);
  for(int i = 0; i < EEPROM.length(); i++) EEPROM.update(i, 255);
  digitalWrite(PIN_LED_BLUE, HIGH);
}

unsigned long bytesToLong(int first, int second, int third, int fourth)
{
  return ((unsigned long) first         & 0xff) |
        (((unsigned long) second << 8 ) & 0xff00) |
        (((unsigned long) third  << 16) & 0xff0000) |
        (((unsigned long) fourth << 24) & 0xff000000);      
}

void longToBytes(unsigned long number, byte* bytes)
{
  bytes[0] =  number        & 0xff;
  bytes[1] = (number >> 8 ) & 0xff;
  bytes[2] = (number >> 16) & 0xff;
  bytes[3] = (number >> 24) & 0xff;
}

void updateLCD()
{
  int temperature = (int) dht.readTemperature();
  int humidity = (int) dht.readHumidity();
  lcd.clear();
  updateLCDDate();
  updateLCDWeather(temperature, humidity);
}

void updateLCDDate()
{
  String date = "";
  date += (day() < 10 ? "0" : "") + String(day()) + ".";
  date += (month() < 10 ? "0" : "") + String(month()) + ".";
  date += (year() < 10 ? "0" : "") + String(year()).substring(2) + " ";
  date += (hour() < 10 ? "0" : "") + String(hour()) + ":";
  date += (minute() < 10 ? "0" : "") + String(minute());// + ":";
  //date += (second() < 10 ? "0" : "") + String(second());

  lcd.setCursor(0, 0);
  lcd.print(date);
}

void updateLCDWeather(int temp, int hum)
{
  String temperature = String(temp) + "'C";
  lcd.setCursor(0, 1);
  lcd.print(temperature);

  String humidity = String(hum) + "%";
  lcd.setCursor(6, 1);
  lcd.print(humidity);
}

void toggleLight()
{
  lightEnabled = !lightEnabled;
  digitalWrite(PIN_LIGHT, lightEnabled ? LOW : HIGH);
}
