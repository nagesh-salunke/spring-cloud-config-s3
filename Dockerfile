FROM amazoncorretto:latest

VOLUME /tmp

EXPOSE 8080

ADD target/spring-cloud-config-s3*.jar spring-cloud-config-s3.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/spring-cloud-config-s3.jar"]

