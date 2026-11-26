FROM eclipse-temurin:26-jdk AS build
ARG MODULE
WORKDIR /build
COPY auth-service/mvnw ./mvnw
COPY auth-service/.mvn ./.mvn
COPY ${MODULE}/pom.xml ./pom.xml
COPY ${MODULE}/src/main ./src/main
RUN sh mvnw -B -Dmaven.test.skip=true package && mv target/*.jar /build/application.jar

FROM caddy:2 AS proxy

FROM eclipse-temurin:26-jre
ARG MODULE
ENV MODULE=${MODULE}
COPY --from=proxy /usr/bin/caddy /usr/local/bin/caddy
COPY render/start.sh render/peers /app/
WORKDIR /app
COPY --from=build /build/application.jar /app/application.jar
USER 10001
ENV SPRING_PROFILES_ACTIVE=render
ENTRYPOINT ["bash", "/app/start.sh"]
