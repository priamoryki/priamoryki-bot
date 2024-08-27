#!/bin/bash

cd priamoryki-bot
git pull
docker-compose up --build --force-recreate -d
