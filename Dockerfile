FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

COPY --from=build /app/target/moneywise.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "-U"]

# default value
CMD ["timebetov"]