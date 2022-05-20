#!/bin/sh

docker compose build
docker compose up

#set -x



# Run rabbit-mq container
#docker run \
#  -d \
#  --hostname rabbit-mq-node \
#  --rm \
#  --name pleo-antaeus-queue \
#  -p 15672:15672 \
#  -p 5672:5672 \
#  rabbitmq:3-management


# Create a new image version with latest code changes.
#docker build . --tag pleo-antaeus
#
## Build the code.
#docker run \
#  --publish 7000:7000 \
#  --rm \
#  --interactive \
#  --tty \
#  --volume pleo-antaeus-build-cache:/root/.gradle \
#  pleo-antaeus



#docker run   -d   --rm   --name pleo-antaeus-queue   -p 15672:15672   -p 5672:5672  rabbitmq:3-management
