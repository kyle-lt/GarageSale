#!/bin/bash

if [[ -f ".env_private" && -f "docker-compose.yml_private" ]]; then
       echo "Repo already public."     
else
	# Transition from private .env file to public .env file
	echo "Transition from private .env file to public .env file"
	mv .env .env_private
	mv .env_public .env
	# Transition from private docker-compose.yml file to public docker-compose.yml file
	echo "Transition from private docker-compose.yml file to public docker-compose.yml file"
	mv docker-compose.yml docker-compose.yml_private
	mv docker-compose.yml_public docker-compose.yml
fi

echo "Done!"

exit 0
