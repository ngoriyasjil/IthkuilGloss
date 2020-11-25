FROM alpine:latest
RUN apk add --no-cache maven curl
COPY pom.xml .
ENTRYPOINT java -jar target/tnilgloss-*-jar-with-dependencies.jar
RUN mvn package
COPY src/ test/ /
RUN mvn -o package -Dmaven.test.skip=true
COPY resources/ resources
