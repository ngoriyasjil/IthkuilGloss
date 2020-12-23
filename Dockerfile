FROM alpine:latest
RUN apk add --no-cache openjdk9-jre-headless git
ENV VERSION 0.1.0-0.18.3
ENTRYPOINT java -jar -Dorg.slf4j.simpleLogger.logFile=System.out -Dorg.slf4j.simpleLogger.log.tnilgloss=trace tnilgloss-$VERSION-jar-with-dependencies.jar
COPY target/tnilgloss-$VERSION-jar-with-dependencies.jar /
COPY .git .git
COPY resources resources
