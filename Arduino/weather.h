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

#define MESSAGE_SET_TIME 1
#define MESSAGE_SAVE_TIME 2
#define MESSAGE_GET_DATA 3
#define MESSAGE_RESET 4

#define MEASURE_DELAY 1800 //30 minut
#define LCD_DELAY 1

struct Record
{
  unsigned long time;
  int temperature;
  int humidity;
};
