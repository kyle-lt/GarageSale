#!/bin/bash

if [[ -f ".env_public" && -f "docker-compose.yml_public" ]]; then
       echo "Repo already private."	
else
	# Transition from public .env file to private .env file
	echo "Transition from public .env file to private .env file"
	mv .env .env_public
	mv .env_private .env
	# Transition from public docker-compose.yml file to private docker-compose.yml file
	echo "Transition from public docker-compose.yml file to private docker-compose.yml file"
	mv docker-compose.yml docker-compose.yml_public
	mv docker-compose.yml_private docker-compose.yml
fi

echo "Done!"

exit 0
