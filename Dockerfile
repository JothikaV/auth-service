# FROM gradle:jdk21 AS builder
# ARG CREDENTIAL
# ENV CREDENTIALS=$CREDENTIAL
# WORKDIR /home/root/build/
# COPY . .
# RUN gradle build -x test
# FROM openjdk:21-jdk-slim
FROM gradle:8.5-jdk21 AS builder
WORKDIR /home/root/build/
COPY . .
RUN gradle build -x test
FROM amazoncorretto:21-alpine
WORKDIR /home/root/authservice-app/
COPY --from=builder /home/root/build/build/libs/authservice-0.0.1-SNAPSHOT.jar /home/root/authservice-app/
ENTRYPOINT ["java","-jar","authservice-0.0.1-SNAPSHOT.jar"]