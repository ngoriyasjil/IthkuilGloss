FROM alpine:latest
RUN apk add --no-cache maven curl git
COPY pom.xml .
ENTRYPOINT java -jar target/tnilgloss-*-jar-with-dependencies.jar
RUN mvn package
COPY src src
COPY test test
RUN mvn -o package
COPY .git .git
COPY resources resources
