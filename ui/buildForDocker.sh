#!/bin/bash

mvn clean package && cp target/ui-0.0.1-SNAPSHOT.jar docker

cd ..

docker-compose build ui

docker-compose up -d

docker logs -f garagesale_ui_1

exit 0
