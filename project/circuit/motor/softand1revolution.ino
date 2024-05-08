softand1revolution

// Define pin constants for easy modifications
const int ENA = 2;   // Motor enable pin
const int IN1 = 3;   // Motor input 1
const int IN2 = 4;   // Motor input 2
const int ENB = 5;   // Second motor enable pin
const int IN3 = 6;   // Second motor input 1
const int IN4 = 7;   // Second motor input 2


// Delay times for acceleration and deceleration phases
const int delays[] = {45, 38, 32, 27, 23, 20, 17, 15, 13, 11, 10, 9, 8, 7, 7, 6, 6, 6, 5, 5, 5, 5, 5, 5, 4};


// Reverse delay times for deceleration phase
int reverseDelays[sizeof(delays) / sizeof(delays[0])];
void setupReverseDelays() {
  int size = sizeof(delays) / sizeof(delays[0]);
  for (int i = 0; i < size; i++) {
    reverseDelays[i] = delays[size - 1 - i];
  }
}


void setup() {
  // Setup all pins as outputs and turn on the motor
  pinMode(ENA, OUTPUT);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(ENB, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);


  digitalWrite(ENA, HIGH);
  digitalWrite(ENB, HIGH);


  // Setup reverse delays
  setupReverseDelays();
}


void loop() {


 
  // Perform motor steps with acceleration phase
  performSteps(delays);


  // Activate the second motor
  digitalWrite(ENB, HIGH);
 
  // Perform additional steps for the second motor
  performAdditionalSteps(0);


  // Perform motor steps with deceleration phase
  performSteps(reverseDelays);


  // Turn off the motors after completing the steps
  digitalWrite(ENA, LOW);
  digitalWrite(ENB, LOW);


  // Prevent restarting the loop
  while (true);
}


void performSteps(const int delayArray[]) {
  for (int i = 0; i < sizeof(delays) / sizeof(delays[0]); i++) {
    stepMotorSoftStart(ENA, IN1, IN2, IN3, IN4, delayArray[i]);
  }
}


void stepMotorSoftStart(int enablePinA, int pin1A, int pin2A, int pin1B, int pin2B, int stepDelay) {
  // Step sequence to drive the motor
  digitalWrite(enablePinA, HIGH);
  digitalWrite(pin1A, HIGH);
  digitalWrite(pin2A, LOW);
  digitalWrite(pin1B, HIGH);
  digitalWrite(pin2B, LOW);
  delay(stepDelay);


  digitalWrite(pin1A, HIGH);
  digitalWrite(pin2A, LOW);
  digitalWrite(pin1B, LOW);
  digitalWrite(pin2B, HIGH);
  delay(stepDelay);


  digitalWrite(pin1A, LOW);
  digitalWrite(pin2A, HIGH);
  digitalWrite(pin1B, LOW);
  digitalWrite(pin2B, HIGH);
  delay(stepDelay);


  digitalWrite(pin1A, LOW);
  digitalWrite(pin2A, HIGH);
  digitalWrite(pin1B, HIGH);
  digitalWrite(pin2B, LOW);
  delay(stepDelay);
}


void performAdditionalSteps(int steps) {
  // Step sequence to perform additional steps for the second motor
  int d = 4; // Delay in milliseconds
  for (int i = 0; i < steps; i++) {
    digitalWrite(IN1, HIGH);
    digitalWrite(IN2, LOW);
    digitalWrite(IN3, HIGH);
    digitalWrite(IN4, LOW);
    delay(d);


    digitalWrite(IN1, HIGH);
    digitalWrite(IN2, LOW);
    digitalWrite(IN3, LOW);
    digitalWrite(IN4, HIGH);
    delay(d);


    digitalWrite(IN1, LOW);
    digitalWrite(IN2, HIGH);
    digitalWrite(IN3, LOW);
    digitalWrite(IN4, HIGH);
    delay(d);


    digitalWrite(IN1, LOW);
    digitalWrite(IN2, HIGH);
    digitalWrite(IN3, HIGH);
    digitalWrite(IN4, LOW);
    delay(d);
  }
}
