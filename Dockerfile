FROM openjdk:14
VOLUME /tmp
ADD build/libs/project-service-0.0.1-SNAPSHOT.jar project-service.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","project-service.jar"]
