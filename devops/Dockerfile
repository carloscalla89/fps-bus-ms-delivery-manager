FROM 519901771307.dkr.ecr.us-west-2.amazonaws.com/open-jdk-alpine:jre-11-tz-lima
COPY /target/us-deliverymanager.jar /usr/src/service/service.jar
COPY /devops/setup.sh /
RUN chmod 777 /setup.sh
ENV LOG_LEVEL_SYSTEM=ERROR
ENV LOG_LEVEL_BUSINESS=INFO

ADD ./devops/newrelic/newrelic.jar /usr/src/service/newrelic.jar
ADD ./devops/newrelic/newrelic.yml /usr/src/service/newrelic.yml

ENV NEW_RELIC_APP_NAME="Delivery Manager"
ENV NEW_RELIC_LICENSE_KEY="license_key"
ENV NEW_RELIC_LOG_FILE_NAME="STDOUT"

EXPOSE 8031

CMD [ "/setup.sh" ]
