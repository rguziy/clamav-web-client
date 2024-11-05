#!/bin/bash
mvn clean package
docker image rm rguziy/clamav-web-client:latest
docker build -t rguziy/clamav-web-client:latest .