FROM 519901771307.dkr.ecr.us-west-2.amazonaws.com/open-jdk-alpine
COPY /backend/target/us-order-manager.jar /usr/src/service/service.jar
COPY /devops/setup.sh /
RUN chmod 777 /setup.sh
ENV LOG_LEVEL_SYSTEM=ERROR
ENV LOG_LEVEL_BUSINESS=INFO
EXPOSE ####

CMD [ /setup.sh ]
