FROM openjdk:8
MAINTAINER Linyouru
VOLUME /data/pressure2/scheduler
WORKDIR /home/pressure2scheduler
ADD scheduler-0.0.1-SNAPSHOT.jar app.jar
CMD mkdir /home/pressure2scheduler/config
EXPOSE 9310
ENTRYPOINT java -Xms800m -Xmx800m -jar app.jar --spring.config.location=/home/pressure2scheduler/config/application.yml