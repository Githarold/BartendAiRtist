// Define pin constants for easy modifications
const int ENA = 11;   // Motor enable pin (PWM capable pin)
const int IN1 = 12;   // Motor input 1
const int IN2 = 13;  // Motor input 2


void setup() {
  // Setup all pins as outputs
  pinMode(ENA, OUTPUT);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
}


void loop() {
  // Ramp up the motor speed gradually
  for (int speed = 0; speed <= 63; speed++) {
    analogWrite(ENA, speed);
    delay(10); // Short delay between speed increments
  }


  // Set motor to run forward
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  delay(1000); // Run motor forward for 1 second


  // Ramp down the motor speed gradually
  for (int speed = 63; speed >= 0; speed--) {
    analogWrite(ENA, speed);
    delay(10); // Short delay between speed decrements
  }


  // Set motor to run reverse
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH);
  delay(1000); // Run motor reverse for 1 second


  // Ramp up the motor speed gradually in reverse
  for (int speed = 0; speed <= 63; speed++) {
    analogWrite(ENA, speed);
    delay(10); // Short delay between speed increments
  }


  // Ramp down the motor speed gradually from reverse
  for (int speed = 63; speed >= 0; speed--) {
    analogWrite(ENA, speed);
    delay(10); // Short delay between speed decrements
  }
}



