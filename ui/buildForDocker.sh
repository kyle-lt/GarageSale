#!/bin/bash

mvn clean package && cp target/ui-0.0.1-SNAPSHOT.jar docker

cd ..

docker-compose build --no-cache ui

exit 0
