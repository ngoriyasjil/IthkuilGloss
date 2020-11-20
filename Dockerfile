FROM alpine:latest
RUN apk add --no-cache maven curl
COPY pom.xml .
ENTRYPOINT java -jar target/tnilgloss-*-jar-with-dependencies.jar
RUN mvn package
COPY src/ src
RUN mvn -o package
COPY resources/ resources
