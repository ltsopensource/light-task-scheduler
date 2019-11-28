FROM openjdk:8-jdk-alpine
VOLUME /tmp
#EXPOSE 8081
ARG VERSION=1.8.0-SNAPSHOT
COPY dist/lts-${VERSION}-bin.zip lts-${VERSION}-bin.zip
RUN unzip lts-${VERSION}-bin.zip
RUN rm -f lts-${VERSION}-bin.zip
RUN chmod 755 lts-${VERSION}-bin/bin/*
RUN apk add --no-cache bash