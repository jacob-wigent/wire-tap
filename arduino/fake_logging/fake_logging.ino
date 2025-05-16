unsigned long lastPrintTime = 0;
unsigned long nextPrintDelay = 0;

float temp = 24.5;
float voltage = 3.3;
int cpuLoad = 12;

void setup() {
  Serial.begin(9600);
  randomSeed(analogRead(A0));
  scheduleNextPrint();
}

void loop() {
  unsigned long now = millis();
  if (now - lastPrintTime >= nextPrintDelay) {
    printFakeLogLine();
    scheduleNextPrint();
    lastPrintTime = now;
  }
}

void scheduleNextPrint() {
  nextPrintDelay = random(200, 800); // 0.2s to 0.8s between logs
}

void printFakeLogLine() {
  // Timestamp
  Serial.print("[");
  Serial.print(millis());
  Serial.print(" ms] ");

  int type = random(100);

  if (type < 60) {
    // Sensor data
    updateFakeData();
    Serial.print("DATA | Temp: ");
    Serial.print(temp, 1);
    Serial.print(" C | Vbat: ");
    Serial.print(voltage, 2);
    Serial.print(" V | CPU: ");
    Serial.print(cpuLoad);
    Serial.println(" %");
  } else if (type < 85) {
    // Normal system log
    printStatusMessage();
  } else if (type < 95) {
    // Warning
    printWarningMessage();
  } else {
    // Simulated packet dump
    printHexDump();
  }
}

void updateFakeData() {
  temp += random(-10, 11) * 0.1;      // Vary temp by ±1.0
  voltage += random(-5, 6) * 0.01;    // Vary voltage by ±0.05
  cpuLoad += random(-3, 4);           // Vary CPU by ±3%

  // Clamp values
  if (temp < 20) temp = 20;
  if (temp > 40) temp = 40;
  if (voltage < 2.9) voltage = 2.9;
  if (voltage > 3.6) voltage = 3.6;
  if (cpuLoad < 0) cpuLoad = 0;
  if (cpuLoad > 100) cpuLoad = 100;
}

void printStatusMessage() {
  const char* msgs[] = {
    "INFO | Sync complete",
    "INFO | Watchdog check OK",
    "INFO | Sensor bus active",
    "INFO | Loop stable",
    "DEBUG | Uptime: ",
    "DEBUG | Tick = "
  };
  int choice = random(0, sizeof(msgs) / sizeof(char*));
  Serial.print(msgs[choice]);
  if (choice >= 4) {
    Serial.println(millis() / 1000);
  } else {
    Serial.println();
  }
}

void printWarningMessage() {
  const char* warnings[] = {
    "WARNING | Temp sensor glitch detected",
    "WARNING | CPU usage spike",
    "WARNING | Serial buffer overflow",
    "WARNING | Voltage unstable"
  };
  Serial.println(warnings[random(0, sizeof(warnings) / sizeof(char*))]);
}

void printHexDump() {
  Serial.print("RX[0x");
  Serial.print(random(0x10, 0xFF), HEX);
  Serial.print("] | ");
  for (int i = 0; i < 6; i++) {
    int byteVal = random(0x00, 0xFF);
    if (byteVal < 0x10) Serial.print("0");
    Serial.print(byteVal, HEX);
    Serial.print(" ");
  }
  Serial.println();
}
