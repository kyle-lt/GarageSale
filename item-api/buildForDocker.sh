#!/bin/bash

mvn clean package && cp target/item-api-0.0.1-SNAPSHOT.jar docker

cd ..

docker-compose build item-api

docker-compose up -d

docker logs -f garagesale_item-api_1

exit 0
