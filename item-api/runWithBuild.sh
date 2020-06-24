#!/bin/bash

mvn clean package && java -jar target/item-api-0.0.1-SNAPSHOT.jar

exit 0
