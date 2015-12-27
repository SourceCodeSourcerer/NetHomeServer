#include <JeeLib.h>

#define SEND_BUFFER_SIZE 200
#define INPUT_BUFFER_SIZE 20
#define LED_PIN 9
#define FIRMWARE_VERSION "VOpenNetHome 0.2"

static char inputBuffer[INPUT_BUFFER_SIZE];
static unsigned int sendBuffer[SEND_BUFFER_SIZE];
static int sendBufferPointer = 0;
static unsigned int transmitPointer = 0;

static void activityLed (byte on) {
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, !on);
}

char readInput() {
  while (Serial.available() == 0) ;
  char result = Serial.readBytesUntil('\n', inputBuffer, INPUT_BUFFER_SIZE);
  inputBuffer[result] = 0;
  return result;
}

/*
 * Converts a hex string to a buffer. Not hex characters will be skipped
 * Returns the hex bytes found. Single-Nibbles wont be converted.
 */
int
fromhex(const char *in, unsigned char *out, int buflen)
{
  unsigned char *op = out, c, h = 0, fnd, step = 0;
  while((c = *in++)) {
    fnd = 0;
    if(c >= '0' && c <= '9') { h |= c-'0';    fnd = 1; }
    if(c >= 'A' && c <= 'F') { h |= c-'A'+10; fnd = 1; }
    if(c >= 'a' && c <= 'f') { h |= c-'a'+10; fnd = 1; }
    if(!fnd) {
      if(c != ' ')
        break;
      continue;
    }
    if(step++) {
      *op++ = h;
      if(--buflen <= 0)
        return (op-out);
      step = 0;
      h = 0;
    } else {
      h <<= 4;
    }
  }
  return op-out;
}

static const char hexString[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

char* printhex(int data) {
  Serial.print(hexString[(data >> 12) & 0xF]);
  Serial.print(hexString[(data >> 8) & 0xF]);
  Serial.print(hexString[(data >> 4) & 0xF]);
  Serial.println(hexString[(data) & 0xF]);
}


/**
 * Add a flank length to the send buffer
 */
void
addRawFlank(unsigned int flank) {
  // Check if it fits in one byte
  if (flank > (254 * 16)) {
    // No, signal two byte value by adding 255-byte
    sendBuffer[sendBufferPointer++] = 255;					// Mark long pulse
    sendBuffer[sendBufferPointer++] = flank >> (8 + 4);		// High byte
    sendBuffer[sendBufferPointer++] = (flank >> 4) & 0xFF;	// Lo Byte
  }
  else {
    // Flank fits in one byte
    sendBuffer[sendBufferPointer++] = (flank >> 4) & 0xFF;// One Byte
  }
}

/**
 * Add a new pulse to the transmit buffer.
 * \param in parameter string: "Ammmmssss" where "mmmm" is the mark flank length in us
 * in HEX format and "ssss" is the following space flank
 */
void
addRawPulse(char *in) {
  unsigned char pulseByte;
  unsigned int pulseWord;

  if (sendBufferPointer >= SEND_BUFFER_SIZE) {
    Serial.println('e');
    return;
  }

  // Add the Mark flank
  fromhex(in+1, &pulseByte, 1);					// Read high byte
  pulseWord = pulseByte << 8;
  fromhex(in+3, &pulseByte, 1);					// Read low byte
  pulseWord += pulseByte;
  addRawFlank(pulseWord);

  // Add the Space flank
  fromhex(in+5, &pulseByte, 1);					// Read high byte
  pulseWord = pulseByte << 8;
  fromhex(in+7, &pulseByte, 1);					// Read low byte
  pulseWord += pulseByte;
  addRawFlank(pulseWord);
  Serial.print('o');
  printhex(sendBufferPointer);
}

void printVersion() {
  Serial.println(FIRMWARE_VERSION);
}

/**
 * Reset send buffer write pointer
 */
void
resetSendBuffer(char* in) {
  sendBufferPointer = 0;
  Serial.print('o');
  printhex(sendBufferPointer);
}

/**
 * Get the length of the next flank in the send buffer
 */
unsigned int
getRawFlank(void) {
  unsigned int result = 0;
  // Check if it is a two byte value
  if (sendBuffer[transmitPointer] == 255) {
    // Yes, get the high byte first
    transmitPointer++;
    result = sendBuffer[transmitPointer++] << (8 + 4);
  }
  result += sendBuffer[transmitPointer++] << 4;
  return result;
}

/**
 * Transmit the content of the sendBuffer via RF
 */
void
sendRawMessage(char* in) {
  unsigned char repeat = 0;
  unsigned char repeatCount;
  unsigned char *t;
  unsigned int pulseLength;
  unsigned char onPeriod = 0;
  unsigned char offPeriod = 0;
  unsigned int repeatPointer = 0;
  unsigned char repeatPoint = 0;
  unsigned int pulseCounter = 0;
  transmitPointer = 0;

  // Get repeat count
  fromhex(in + 1, &repeat, 1);

  // Check if modulation period is specified
  if (in[3] != 0) {
    // Yes, it is. Read it.
    fromhex(in + 3, &onPeriod, 1);
    fromhex(in + 5, &offPeriod, 1);

    // Check if repeat offset is specified
    if (in[7] != 0) {
      fromhex(in + 7, &repeatPoint, 1);
    }
  }

  // Send the data from the transmit buffer "repeat" times
  for (repeatCount = 0; repeatCount < repeat; repeatCount++) {
    pulseCounter = 0;
    for (transmitPointer = repeatPointer; transmitPointer < sendBufferPointer;) {
      // Since a pulse in the transmit buffer may take one or three bytes depending
      // on pulse length, we have to find on which transmit pointer position the
      // repeat point really is. The repeatCounter counts number of pulses so we
      // can find out when we have reached the repeat point and can save the
      // repeat pointer position
      if ((repeatCount == 0) && (pulseCounter == repeatPoint)) {
        repeatPointer = transmitPointer;
      }
      rf12_onOff(1);
      activityLed(1);
      delayMicroseconds(getRawFlank() + 181);
      rf12_onOff(0);
      activityLed(0);
      delayMicroseconds(getRawFlank()- 181);
      pulseCounter += 2;
    }
  }
  Serial.print('o');
  printhex(transmitPointer);
}

void setup() {
  Serial.begin(115200);
  rf12_initialize(0, RF12_433MHZ);
  printVersion();
}

void loop() {  
  char size = readInput();
  activityLed(1);
  if (inputBuffer[0] == 'A' && size >= 9) {
    addRawPulse(&inputBuffer[0]);
  } else if (inputBuffer[0] == 'S' && size >= 1) {
    sendRawMessage(&inputBuffer[0]);
  } else if (inputBuffer[0] == 'E' ) {
    resetSendBuffer(&inputBuffer[0]);
  } else if (inputBuffer[0] == 'V' ) {
    printVersion();
  } else {
    Serial.println("e");
  }
  Serial.flush();
  activityLed(0);  
}
