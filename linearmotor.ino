// Define pin constants for easy modifications
const int ENA = 9;   // Motor enable pin (PWM capable pin)
const int IN1 = 8;   // Motor input 1
const int IN2 = 10;  // Motor input 2


void linear_up(int power, int time);
void linear_down(int power, int time);
void linear_stop(int power);


void setup() {
  // Setup all pins as outputs
  pinMode(ENA, OUTPUT);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
}


void loop() {
  // Set motor to run forward
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  analogWrite(ENA, 63); // Set speed at 50% of maximum (255 is max)


  delay(5000); // Run motor for 5 seconds


  // Set motor to run reverse
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH);
  analogWrite(ENA, 240); // Maintain speed at 50% of maximum


  delay(5000); // Run motor for another 5 seconds


  // Stop the motor
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  analogWrite(ENA, 0); // No power to the motor


  // Prevent restarting the loop
  while(true);
}


void linear_up(int power, int time) {
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  analogWrite(ENA, power);
  delay(time);
}


void linear_down(int power, int time) {
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH);
  analogWrite(ENA, power);
  delay(time);
}


void linear_stop(int power) {
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  analogWrite(ENA, power);
}








