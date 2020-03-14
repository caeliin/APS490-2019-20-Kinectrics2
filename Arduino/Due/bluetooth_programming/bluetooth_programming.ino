//connect bluetooth module to RX2/TX2
int enable = 9; //bluetooth enable pin

void setup() {
  // put your setup code here, to run once:
  pinMode(enable, OUTPUT);
  digitalWrite(enable, HIGH);
  
  Serial.begin(9600);
  Serial2.begin(38400);

  
  while (!Serial);
  while (!Serial2);
}

void loop() {
  // put your main code here, to run repeatedly:
  if (Serial.available()){
    Serial2.write(Serial.read());
  }
  if (Serial2.available()) {
    Serial.write(Serial2.read());
  }

}
