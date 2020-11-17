FROM alpine:latest
RUN apk add --no-cache maven curl
COPY pom.xml .
ENTRYPOINT java -jar target/tnilgloss-*-jar-with-dependencies.jar
RUN mvn package
COPY src/ src
COPY resources/ resources
RUN mvn -o package
COPY token.txt .
RUN curl -sL 'https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=1534088303' | sed 1d | cut -d $'\t' -f -5  >   roots.tsv \
 && curl -sL 'https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=499365516'  | sed 1d | cut -d $'\t' -f -11 > affixes.tsv
