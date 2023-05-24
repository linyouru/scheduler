#!/bin/bash

cp ../target/scheduler-0.0.1-SNAPSHOT.jar ./
docker build -t zws-r2-pressure-scheduler .
rm scheduler-0.0.1-SNAPSHOT.jar