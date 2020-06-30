#!/bin/bash

mvn clean package && cp target/item-api-0.0.1-SNAPSHOT.jar docker

cd ..

docker-compose build item-api

exit 0
