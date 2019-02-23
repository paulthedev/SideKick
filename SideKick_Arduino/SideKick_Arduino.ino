#include <DHT.h>
#include <U8glib.h>
#include <Time.h>
#include <SoftwareSerial.h>
#include <string.h>

//Initialising OLED
// IMPORTANT NOTE: The complete list of supported devices
// with all constructor calls is here: http://code.google.com/p/u8glib/wiki/device
U8GLIB_SSD1306_128X64 u8g(U8G_I2C_OPT_NONE | U8G_I2C_OPT_DEV_0);

//Initialising DHT Pin
#define DHTPIN 4
#define DHTTYPE DHT22   // DHT 22  (AM2302)
DHT dht(DHTPIN, DHTTYPE);

//Temperature And Humidity variables
float humid;
float temp;

//Detect button change
int q = 0;

//Flag to see if dht is ready
int dhtReady = 1;

//Storing month and week in word
const char* monthString[] = {"", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
const char* weekString[] = {"","Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

//Set display features
const int centerX = u8g.getWidth() / 2;
const int centerY = u8g.getHeight() / 2;

//ClockFormat flag
int twelve=0;

//TemperatureFormat flag
int TempFormat=0;

//Bluetooth related stuff
SoftwareSerial bt(2, 3); // RX, TX
String string;
char command;

void setup()
{
  //Button pin
  pinMode(5, INPUT);
  bt.begin(9600);
  dht.begin();
}


void loop()
{
  //Reading data from bluetooth Sensor
  if (bt.available() > 0) 
    {string = "";}
    
    while(bt.available() > 0)
    {
      command = ((byte)bt.read());
      
      if(command == ':')
      {
        break;
      }
      
      else
      {
        string += command;
      }
    }

    //Extract Header
    String head = string.substring(0, 1);
    //Check for phone call
    //Extract and set Time
    if(head=="T")
    {
      int Hour=(string.substring(1,3)).toInt();
      int Minute=(string.substring(3,5)).toInt();
      int Second=(string.substring(5,7)).toInt();
      int Day=(string.substring(7,9)).toInt();
      int Month=(string.substring(9,11)).toInt();
      int Year=(string.substring(11)).toInt();
      setTime(Hour, Minute, Second, Day, Month, Year);
      string="";
    }
    
    // Setting Temperature format
    if(string=="3")
    {
          TempFormat=0;
          string="atmos";
    }
    if(string=="4")
    {
          TempFormat=1;
          string="atmos";
    }
       
    // Setting Clock Format
    if(string=="0")
    {
      twelve=0;
      string="";
    }
    if(string=="1")
    {
      twelve=1;
      string="";
    }
    
    if (digitalRead(5) == LOW)
    {
      if (string == "" || string == "I")
        string="atmos";
      else
       string = "";
    }
      
    if (string == "" || string == "I")
      drawClock();
    else if(head=="R")
      drawCall();
    else if(string == "atmos")
      drawAtmos();
}


void drawClock()
{
  u8g.firstPage();
  do {
    time_t t = now();
    u8g.setFontRefHeightExtendedText();
    u8g.setDefaultForegroundColor();
    u8g.setFontPosTop();
    String str = String("");
    int l;
    
    //Date with Month in word
    if (day(t) < 10)
      str += "0";
    str += day(t);
    str += "-";
    str += monthString[month(t)];
    str += "-";
    str += year(t);
    l=str.length()+1;
    char buff[l];
    str.toCharArray(buff, l);
    buff[l] = 0x00;
    l=u8g.getStrWidth(buff);
    u8g.drawStr(centerX-l/2 , centerY + 23 , buff);

    //Time
    str = String("");
    //Hour
    if(twelve==0)
    {
      if (hourFormat12(t) < 10)
        str += "0";
      str += hourFormat12(t);
    }
    else if(twelve==1)
    {
      if (hour(t) < 10)
        str += "0";
      str += hour(t);
    }    
    str += ":";

    //Minute
    if (minute(t) < 10)
      str += "0";
    str += minute(t);
    char buff1[l];
    str.toCharArray(buff1, l);
    buff1[l] = 0x00;
    u8g.setFont(u8g_font_courB14);
    l=u8g.getStrWidth(buff1);
    u8g.drawStr(centerX-l/2, centerY+10, buff1);

    u8g.setFont(u8g_font_fixed_v0);

    //Week in Word.
    u8g.drawStr(centerX-l/2, centerY - 12, weekString[weekday(t)]);

    //AmPm
    if (isAM(t))
      u8g.drawStr(centerX-l/2+40, centerY - 14, "AM");
    else
      u8g.drawStr(centerX-l/2+40, centerY - 14, "PM");
      
  } while (u8g.nextPage());
}


void drawAtmos()
{
  u8g.firstPage();
  do {
    
    int l;
    u8g.setFont(u8g_font_profont12);
    u8g.setFontRefHeightExtendedText();
    u8g.setDefaultForegroundColor();
    u8g.setFontPosTop();
    
    //Check DHT Sensor
    testDHT();
    
    //Is DHT sensor Ready?
    if (dhtReady == 1)
    {
      if(TempFormat==0)
      {
      // Read temperature as Celsius (the default)
      temp = dht.readTemperature();
      }
      if(TempFormat==1)
      {
      // Read temperature as Fahrenheit (isFahrenheit = true)
      temp = dht.readTemperature(true);
      }
      humid = dht.readHumidity();
    }
    
    //Drawing Temperature Data
    
    String str = String("");
    str="Temperature";
    l = str.length()+1;
    char buff[l];
    str.toCharArray(buff, l);
    buff[l] = 0x00;
    l=u8g.getStrWidth(buff);
    u8g.drawStr(centerX-l/2, centerY-25, buff);

    str = String("");
    str+=temp;
    
    if(TempFormat==0)
    {
      str+="\260C"; 
    }
    if(TempFormat==1)
    {
      str+="\260F";
    }
    
    l = str.length()+1;
    char buff1[l];
    str.toCharArray(buff1, l);
    buff1[l] = 0x00;
    l=u8g.getStrWidth(buff1);
    u8g.drawStr(centerX-l/2, centerY-10, buff1);

    //Drawing Humidity Data
    
    str="Humidity";
    l = str.length()+1;
    char buff2[l];
    str.toCharArray(buff2, l);
    buff2[l] = 0x00;
    l=u8g.getStrWidth(buff2);
    u8g.drawStr(centerX-l/2, centerY+5, buff2);

    str = String("");
    str+=humid;
    str+="%";
    l = str.length()+1;
    char buff3[l];
    str.toCharArray(buff3, l);
    buff3[l] = 0x00;
    l=u8g.getStrWidth(buff3);
    u8g.drawStr(centerX-l/2, centerY+20, buff3);
    
  } while (u8g.nextPage());
}


void drawCall()
{
  u8g.firstPage();
  do{
    u8g.setFont(u8g_font_fixed_v0);
    u8g.setFontRefHeightExtendedText();
    u8g.setDefaultForegroundColor();
    u8g.setFontPosTop();

    String s="Incoming Call";
    int l = s.length()+1;
    char buff[l];
    s.toCharArray(buff, l);
    buff[l] = 0x00;
    l=u8g.getStrWidth(buff);
    u8g.drawStr(centerX-l/2, centerY-20, buff);
    
    l = string.length();
    char buff1[l];
    string.substring(1).toCharArray(buff1, l);
    buff1[l] = 0x00;
    u8g.setFontPosBottom();
    u8g.setFont(u8g_font_profont12);
    l=u8g.getStrWidth(buff1);
    u8g.drawStr(centerX-l/2, centerY+20, buff1);
  } while (u8g.nextPage());
}

void testDHT()
{
  if (isnan(dht.readTemperature()) || isnan(dht.readHumidity()))
  {
    dhtReady = 0;
  }
  else
  {
    dhtReady = 1;
  }
}


