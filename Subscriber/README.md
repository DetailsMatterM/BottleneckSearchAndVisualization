# Group03-BlindSpot and Bottleneck Listner

# Description

This repository contains software that listens to messages that is broadcasted about blindSpots and bottlenecks that are filtered after transporation requests in public transportation system located in the greater Göteborg area.

## Prerequirements

* installed Eclipse Mosquitto (mqtt broker)
* Installed maven 3.5 or above
* Installed Java JDK 8 or above

## Applicablity

1. Receive published message about blindSpots and bottlenecks

## Configuration

1. Clone repository
2. Open terminal and change directory to the root folder of the repository
3. Run "mvn clean install"
4. Change directory to the target folder of the repository
5. Run java -jar subscriber.jar
6. upon arrival of message, data will be printed out in terminal


## Comments

* Dont worry about the warning messages if they apear while executing the jar file.
