const int sensor = A0;
//lower number is wetter
//higher number is dryer

int air = 581;


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
}

void loop() {
  // put your main code here, to run repeatedly:
  int sensorVal = analogRead(sensor);
  float sensorVoltage = (3.0/1024)*sensorVal;
  Serial.print(sensorVal);
  Serial.print(" - (");
  Serial.print(sensorVoltage);
  Serial.print("V)\n");
  delay(100);
}
