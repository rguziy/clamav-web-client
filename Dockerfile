FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/clamav-web-client*.jar
COPY ${JAR_FILE} clamav-web-client.jar
ENTRYPOINT ["java","-jar","/clamav-web-client.jar"]