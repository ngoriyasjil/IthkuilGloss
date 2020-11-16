FROM alpine:latest
RUN apk add --no-cache openjdk8 gradle
RUN gradle --version > /dev/null
COPY . .
RUN gradle shadowJar
ADD https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=1534088303 .
ADD https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=499365516  .
ENTRYPOINT java -jar build/libs/tnilgloss-*.jar
