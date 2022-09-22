#!/bin/sh
java $newrelic  -Dspring.profiles.active=$environment -jar /usr/src/service/service.jar
