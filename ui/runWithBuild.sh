#!/bin/bash

mvn clean package && java -jar target/ui-0.0.1-SNAPSHOT.jar

exit 0
