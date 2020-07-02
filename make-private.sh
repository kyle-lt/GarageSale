#!/bin/bash

if [[ -f ".env_public" && -f "docker-compose.yml_public" && -f "kubernetes/ui-deployment.yaml_public" && -f "kubernetes/item-api-deployment.yaml_public" ]]; then
        echo "Repo already private."	
else
        echo "Repo public, changing to private."
        # Transition from public .env file to private .env file
	echo "Transition from public .env file to private .env file"
	mv .env .env_public
	mv .env_private .env
	# Transition from public docker-compose.yml file to private docker-compose.yml file
	echo "Transition from public docker-compose.yml file to private docker-compose.yml file"
	mv docker-compose.yml docker-compose.yml_public
	mv docker-compose.yml_private docker-compose.yml
	# Transition private kubernetes specs to public kubernetes specs
        echo "Transition public kubernetes specs to private kubernetes specs"
        mv kubernetes/ui-deployment.yaml kubernetes/ui-deployment.yaml_public
        mv kubernetes/ui-deployment.yaml_private kubernetes/ui-deployment.yaml
        mv kubernetes/item-api-deployment.yaml kubernetes/item-api-deployment.yaml_public
        mv kubernetes/item-api-deployment.yaml_private kubernetes/item-api-deployment.yaml
fi

echo "Done!"

exit 0
