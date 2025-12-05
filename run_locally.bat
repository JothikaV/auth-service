@echo off
echo Running Spring Boot Application using Gradle Wrapper...

:: Clean and build without running tests
call .\gradlew clean build -x test

:: Start the Spring Boot application with the 'local' profile
call .\gradlew bootRun --args="--spring.profiles.active=local"

pause
