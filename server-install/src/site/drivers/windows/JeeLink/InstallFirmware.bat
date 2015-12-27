@echo off

IF "%1"=="" GOTO :ERROR
bin\avrdude -Cbin\avrdude.conf -v -patmega328p -carduino -P%1 -b115200 -D -Uflash:w:bin/CUL.cpp.hex:i
GOTO :eof

:ERROR
ECHO.
ECHO uploads OpenNetHome firmware to a JeeLink Classic
ECHO.
ECHO USAGE: flash.bat [Serial Port]
ECHO.
ECHO For Example: flash.bat COM7
