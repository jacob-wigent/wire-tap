int currentValue = 10;
unsigned long lastUpdateTime = 0;
unsigned long nextUpdateInterval = 0;

void setup() {
  Serial.begin(9600);
  randomSeed(analogRead(0));
  scheduleNextUpdate();
}

void loop() {
  unsigned long currentTime = millis();
  if (currentTime - lastUpdateTime >= nextUpdateInterval) {
    int delta = random(-1, 2);
    currentValue += delta;

    if (currentValue < 0) currentValue = 0;
    if (currentValue > 20) currentValue = 20;

    Serial.print(currentValue);

    lastUpdateTime = currentTime;
    scheduleNextUpdate();
  }
}

void scheduleNextUpdate() {
  nextUpdateInterval = random(500, 2001);
}