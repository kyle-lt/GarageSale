#!/bin/bash

if [[ -f ".env_private" && -f "docker-compose.yml_private" && -f "kubernetes/ui-deployment.yaml_private" && -f "kubernetes/item-api-deployment.yaml_private" ]]; then
       echo "Repo already public."
else
	echo "Repo private, changing to public."
        # Transition from private .env file to public .env file
	echo "Transition from private .env file to public .env file"
	mv .env .env_private
	mv .env_public .env
	# Transition from private docker-compose.yml file to public docker-compose.yml file
	echo "Transition from private docker-compose.yml file to public docker-compose.yml file"
	mv docker-compose.yml docker-compose.yml_private
	mv docker-compose.yml_public docker-compose.yml
	# Transition private kubernetes specs to public kubernetes specs
	echo "Transition private kubernetes specs to public kubernetes specs"
	mv kubernetes/ui-deployment.yaml kubernetes/ui-deployment.yaml_private
	mv kubernetes/ui-deployment.yaml_public kubernetes/ui-deployment.yaml
	mv kubernetes/item-api-deployment.yaml kubernetes/item-api-deployment.yaml_private
	mv kubernetes/item-api-deployment.yaml_public kubernetes/item-api-deployment.yaml
fi

echo "Done!"

exit 0
