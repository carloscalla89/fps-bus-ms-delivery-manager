#!/bin/sh

#Environment variable
TAG="${artifactId}"
LOGS_FOLDER="C:\var\log\docker\us-${artifactId}"
PORT="${port}"
CONTAINER_NAME="${artifactId}"
LOG_LEVEL_SYSTEM=ERROR
LOG_LEVEL_BUSINESS=INFO

$(aws ecr get-login --region us-west-2 | sed -e 's/-e none//g')
cd ..
#building image
docker build -t $TAG -f devops/Dockerfile .
echo "image created"
#creating container
docker run -d -p $PORT:#### --name=$CONTAINER_NAME -e environment=ci -e LOG_LEVEL_SYSTEM=$LOG_LEVEL_SYSTEM -e LOG_LEVEL_BUSINESS=$LOG_LEVEL_BUSINESS -v $LOGS_FOLDER:/var/log/"${artifactId}" $TAG
echo "container created"
read -rsp $'Press any key to continue...\n' -n1 key