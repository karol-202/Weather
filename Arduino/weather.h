#define PIN_LCD_RS 2
#define PIN_LCD_E 3
#define PIN_LCD_D0 4
#define PIN_LCD_D1 5
#define PIN_LCD_D2 6
#define PIN_LCD_D3 7
#define PIN_DHT 8
#define PIN_LED_RED 9
#define PIN_LED_GREEN 10
#define PIN_LED_BLUE 11
#define PIN_BUTTON_LIGHT 12

#define PIN_EXP_LIGHT 0

#define PIN_RAIN A0

#define MESSAGE_SET_TIME 1
#define MESSAGE_SAVE_TIME 2
#define MESSAGE_GET_DATA 3
#define MESSAGE_RESET 4

#define MEASURE_DELAY 1800 //30 minut
#define LCD_DELAY 5
#define HOUR 3600

#define DEGREE_CHAR 0
#define RAIN_0_CHAR 1
#define RAIN_1_CHAR 2
#define RAIN_2_CHAR 3
#define RAIN_3_CHAR 4
#define RAIN_4_CHAR 5
#define RAIN_5_CHAR 6

struct Record
{
  unsigned long time;
  int temperature;
  int humidity;
  byte rain;
};

byte degreeCharBytes[] = { B00100,
                           B01010,
                           B00100,
                           B00000,
                           B00000,
                           B00000,
                           B00000,
                           B00000 };
byte rain0CharBytes[] = { B00000,
                          B00000,
                          B00000,
                          B00000,
                          B00000,
                          B00000,
                          B00000,
                          B00000 };
byte rain1CharBytes[] = { B10000,
                          B10000,
                          B10000,
                          B10000,
                          B10000,
                          B10000,
                          B10000,
                          B10000 };
byte rain2CharBytes[] = { B11000,
                          B11000,
                          B11000,
                          B11000,
                          B11000,
                          B11000,
                          B11000,
                          B11000 };
byte rain3CharBytes[] = { B11100,
                          B11100,
                          B11100,
                          B11100,
                          B11100,
                          B11100,
                          B11100,
                          B11100 };
byte rain4CharBytes[] = { B11110,
                          B11110,
                          B11110,
                          B11110,
                          B11110,
                          B11110,
                          B11110,
                          B11110 };
byte rain5CharBytes[] = { B11111,
                          B11111,
                          B11111,
                          B11111,
                          B11111,
                          B11111,
                          B11111,
                          B11111 };
