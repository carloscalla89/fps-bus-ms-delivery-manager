#!/bin/sh
java -javaagent:/usr/src/service/newrelic.jar -Dspring.profiles.active=$environment -jar /usr/src/service/service.jar