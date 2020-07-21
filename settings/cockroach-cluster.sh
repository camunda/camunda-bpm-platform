#!/bin/bash

# Guide: https://www.cockroachlabs.com/docs/v19.2/start-a-local-cluster-in-docker-linux.html

# DB name: postgres
# DB port: 26257
# DB username: root
# No DB password


# Create bridge network
# if a network is not created, execute this command manually
docker network create -d bridge roachnet

# Create nodes:
docker run -d \
	--name=roach1 \
	--hostname=roach1 \
	--net=roachnet \
	-p 26257:26257 \
	-p 8080:8080 \
	-v "${PWD}/cockroach-data/roach1:/cockroach/cockroach-data" \
	cockroachdb/cockroach:v20.1.3 start \
	--insecure \
	--join=roach1,roach2,roach3

docker run -d \
	--name=roach2 \
	--hostname=roach2 \
	--net=roachnet \
	-v "${PWD}/cockroach-data/roach2:/cockroach/cockroach-data" \
	cockroachdb/cockroach:v20.1.3 start \
	--insecure \
	--join=roach1,roach2,roach3

docker run -d \
	--name=roach3 \
	--hostname=roach3 \
	--net=roachnet \
	-v "${PWD}/cockroach-data/roach3:/cockroach/cockroach-data" \
	cockroachdb/cockroach:v20.1.3 start \
	--insecure \
	--join=roach1,roach2,roach3


# initialize cluster
docker exec -it roach1 ./cockroach init --insecure

# Uncomment below to create a `processEngine` database
# docker exec -it roach1 ./cockroach sql --insecure --execute "CREATE DATABASE processEngine;"