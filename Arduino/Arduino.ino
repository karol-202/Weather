#include <DHT.h>
#include <EEPROM.h>
#include <TimeLib.h>
#include <LiquidCrystal.h>
#include <Wire.h>
#include "PCF8574.h"
#include "weather.h"

DHT dht(PIN_DHT, DHT22);
LiquidCrystal lcd(PIN_LCD_RS, PIN_LCD_E, PIN_LCD_D0, PIN_LCD_D1, PIN_LCD_D2, PIN_LCD_D3);
PCF8574 expander;
long measureTime;
long lcdUpdateTime;
bool lightEnabled;
byte timeZone;

void setup()
{
  Serial.begin(9600);
  dht.begin();
  setupChars();
  lcd.begin(16, 2);
  expander.begin(32);
  pinMode(PIN_LED_RED, OUTPUT);
  pinMode(PIN_LED_GREEN, OUTPUT);
  pinMode(PIN_LED_BLUE, OUTPUT);
  pinMode(PIN_BUTTON_LIGHT, INPUT_PULLUP);
  expander.pinMode(PIN_EXP_LIGHT, OUTPUT);

  int addrTime = EEPROM.length() - 7;
  time_t time;
  EEPROM.get(addrTime, time);
  setTime(time);

  int addrZone = EEPROM.length() - 8;
  timeZone = EEPROM.read(addrZone) - 128;

  measureTime = now() + 10; //Czekaj 10 sekund przed pierwszym zapisem
  lcdUpdateTime = now();
  lightEnabled = true;

  toggleLED(PIN_LED_RED, false);
  toggleLED(PIN_LED_GREEN, false);
  toggleLED(PIN_LED_BLUE, false);
  expander.digitalWrite(PIN_EXP_LIGHT, LOW);
}

void setupChars()
{
  lcd.createChar(DEGREE_CHAR, degreeCharBytes);
  lcd.createChar(RAIN_0_CHAR, rain0CharBytes);
  lcd.createChar(RAIN_1_CHAR, rain1CharBytes);
  lcd.createChar(RAIN_2_CHAR, rain2CharBytes);
  lcd.createChar(RAIN_3_CHAR, rain3CharBytes);
  lcd.createChar(RAIN_4_CHAR, rain4CharBytes);
  lcd.createChar(RAIN_5_CHAR, rain5CharBytes);
}

void loop()
{
  while (now() < measureTime)
  {
    checkForData();
    checkButton();
    updateLCD();
    delay(10); //Czekaj 0.01s przed kolejnym sprawdzeniem
  }
  collectData();
  measureTime += MEASURE_DELAY;
}

void checkForData()
{
  if (Serial.available() > 0)
  {
    int message = Serial.read();
    switch (message)
    {
      case MESSAGE_SET_TIME:
        updateTime();
        break;
      case MESSAGE_SAVE_TIME:
        saveTime();
        break;
      case MESSAGE_GET_DATA:
        sendData();
        break;
      case MESSAGE_RESET:
        clearAll();
        break;
    }
  }
}

void checkButton()
{
  if (digitalRead(PIN_BUTTON_LIGHT) == LOW)
  {
    lightEnabled = !lightEnabled;
    expander.digitalWrite(PIN_EXP_LIGHT, lightEnabled ? LOW : HIGH);
    if (!lightEnabled)
    {
      toggleLED(PIN_LED_RED, false);
      toggleLED(PIN_LED_GREEN, false);
      toggleLED(PIN_LED_BLUE, false);
    }
    delay(200);
  }
}

void updateLCD()
{
  if (now() < lcdUpdateTime) return;
  lcdUpdateTime = now() + LCD_DELAY;

  float temperature = (float) dht.readTemperature();
  float humidity = (float) dht.readHumidity();
  int rain = analogRead(PIN_RAIN);

  lcd.clear();
  updateLCDDate();
  updateLCDWeather(temperature, humidity, rain);
}

void updateLCDDate()
{
  String date = "";
  /*int hour = hour() + HOUR * timeZone;
  int day = day();
  int month = month();
  int year = year();
  if(hour > 23)
  {
    hour -= 24;
    day++;
  }
  if(day*/
    
  date += (day() < 10 ? "0" : "") + String(day()) + ".";
  date += (month() < 10 ? "0" : "") + String(month()) + ".";
  date += (year() < 10 ? "0" : "") + String(year()).substring(2) + " ";
  date += (hour() < 10 ? "0" : "") + String(hour()) + ":";
  date += (minute() < 10 ? "0" : "") + String(minute());// + ":";
  //date += (second() < 10 ? "0" : "") + String(second());

  lcd.setCursor(0, 0);
  lcd.print(date);
}

void updateLCDWeather(float temp, float hum, int rainVal)
{
  String temperature = String(temp, 1) + "'C";
  lcd.setCursor(0, 1);
  lcd.print(temperature);
  lcd.setCursor(temperature.length() - 2, 1); //Degree char
  lcd.write(byte(DEGREE_CHAR));

  String humidity = String(hum, 1) + "%";
  lcd.setCursor(7, 1);
  lcd.print(humidity);

  byte rain[2];
  getRainChars(rainVal, rain);
  lcd.setCursor(13, 1);
  lcd.print("D");
  lcd.write(rain[0]);
  lcd.write(rain[1]);
}

void getRainChars(int rain, byte* chars)
{
  int level = map(rain, 0, 1024, 11, 0);
  if (level <= 5) chars[1] = RAIN_0_CHAR;
  else chars[0] = RAIN_5_CHAR;
  switch (level)
  {
    case 0:
      chars[0] = RAIN_0_CHAR;
      break;
    case 1:
      chars[0] = RAIN_1_CHAR;
      break;
    case 2:
      chars[0] = RAIN_2_CHAR;
      break;
    case 3:
      chars[0] = RAIN_3_CHAR;
      break;
    case 4:
      chars[0] = RAIN_4_CHAR;
      break;
    case 5:
      chars[0] = RAIN_5_CHAR;
      break;
    case 6:
      chars[1] = RAIN_0_CHAR;
      break;
    case 7:
      chars[1] = RAIN_1_CHAR;
      break;
    case 8:
      chars[1] = RAIN_2_CHAR;
      break;
    case 9:
      chars[1] = RAIN_3_CHAR;
      break;
    case 10:
      chars[1] = RAIN_4_CHAR;
      break;
    case 11:
      chars[1] = RAIN_5_CHAR;
      break;
  }
}

void toggleLED(int led, bool state)
{
  int value = state ? LOW : HIGH;
  if (state && !lightEnabled) return;
  digitalWrite(led, value);
}


void collectData()
{
  toggleLED(PIN_LED_GREEN, true);

  struct Record record;
  record.time = now();
  record.temperature = (int) (dht.readTemperature() * 10.0);
  record.humidity = (int) (dht.readHumidity() * 10.0);
  record.rain = (byte) map(analogRead(PIN_RAIN), 0, 1024, 100, 0);

  int length = getLength();
  int addr = length * sizeof(Record);
  EEPROM.put(addr, record);

  int addrLength = EEPROM.length() - 2;
  int addrEmpty = EEPROM.length() - 3;

  EEPROM.put(addrLength, ++length);
  EEPROM.update(addrEmpty, 0);

  delay(1000);
  toggleLED(PIN_LED_GREEN, false);
}

void updateTime()
{
  toggleLED(PIN_LED_RED, true);
  while (Serial.available() < 5) delay(10);
  int first = Serial.read();
  int second = Serial.read();
  int third = Serial.read();
  int fourth = Serial.read();
  time_t newTime = bytesToLong(first, second, third, fourth);
  time_t timeDifference = newTime - now();
  setTime(newTime);
  
  timeZone = Serial.read() - 128;
  
  measureTime += timeDifference;
  lcdUpdateTime += timeDifference;
  toggleLED(PIN_LED_RED, false);
}

void saveTime()
{
  int addrTime = EEPROM.length() - 7;
  int addrZone = EEPROM.length() - 8;
  EEPROM.put(addrTime, now());
  EEPROM.write(addrZone, timeZone + 128);
}

void sendData()
{
  int length = getLength();
  Serial.write((byte) length);
  for (int i = 0; i < length; i++)
  {
    int addr = i * sizeof(Record);
    Record record;
    EEPROM.get(addr, record);

    byte time[4];
    byte temperature[4];
    byte humidity[4];
    longToBytes(record.time, time);
    longToBytes((long) record.temperature, temperature);
    longToBytes((long) record.humidity, humidity);

    Serial.write(time, 4);
    Serial.write(temperature, 4);
    Serial.write(humidity, 4);
    Serial.write(record.rain);
  }
}

void clearAll()
{
  toggleLED(PIN_LED_BLUE, true);
  for (int i = 0; i < EEPROM.length(); i++) EEPROM.update(i, 255);
  toggleLED(PIN_LED_BLUE, false);
}

int getLength()
{
  int addrLength = EEPROM.length() - 2;
  int addrEmpty = EEPROM.length() - 3;

  int length;
  EEPROM.get(addrLength, length);
  if (EEPROM.read(addrEmpty) == 255) length = 0;

  return length;
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
