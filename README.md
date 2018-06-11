NetHome Server
==============

Welcome to the NetHomeServer project. NetHomeServer is the core of OpenNetHome, 
which is an open home automation project. For more details, see the project home page:
http://opennethome.org/

Development Environment
-----------------------

Dependencies: Java SE SDK 1.6, Maven 2.

The project is built using Maven, which makes it quite easy to load it into any
standard development IDE such as Eclipse or IntelliJ. Since the project uses
the rxtx serial library which is platform
dependant, you have to run a platform setup script to configure the build
environment for your development platform before you can build it.
These scripts are located under server-install/src/main/scripts. So to configure the
environment for a 64 bits Windows 7 platform you would have to:

    cd server-install\src\main\scripts
    setup_win64.bat

On Linux and MAC you also have to make the script executable before running it.
For example:

    cd server-install/src/main/scripts
    chmod +x setup_macosx_cocoa64.sh
    ./setup_macosx_cocoa64.sh

This will copy the correct version of the rxtx runtime libraries to the
root of the project so you can run the application from your IDE.

Also, the NetHome Server depends on the projects NetHome/Utils, NetHome/Coders,
Nethome/ZWave and Nethome/cybergarage-upnp.
These projects need to be cloned locally and built and installed in your local 
Maven repository:

    cd <location-of NetHome/Utils>
    mvn install
    cd <location-of NetHome/Coders>
    mvn install
    cd <location-of NetHome/ZWave>
    mvn install
    cd <location-of NetHome/Coders>
    mvn install
    
Overview
--------
The project consists of a number of separate maven modules.

* external - here 3:rd party software which is built from source is located
* home-items - this module contains all NetHome Server Home Items
* server - the NetHomeServer core
* server-install - this module build the installation package

How to Build
------------

To just build the jar file of the project, you issue:

    mvn package

from the root of the project.
To build the entire deployment package you issue:

    mvn install

The installable result will be located under:
server-install/target

Open from IntelliJ
------------------

IntelliJ can read Maven project files, so the project can be opened directly.
All you have to do is to create a run-configuration. Select:

run->edit configurations

and create a new application. Select the main class as:

```nu.nethome.home.start.StaticHomeManagerStarter``` (in server-install modules)

And the configuration should be able to build and run the application.

Open from Eclipse
-----------------

Maven can create an eclipse project by issuing the command:

    mvn eclipse:eclipse

After that you can import the project into your workspace. You will have to
create a variable called M2_REPO which points to your local maven repository,
which on windows is located under your personal folder in a folder called

.m2\repository

For example: ```C:\Users\Stefan\.m2\repository```
