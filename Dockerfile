FROM maven:3.6.2-jdk-8-slim

RUN apt-get update

# For Headless testing
RUN apt-get install -y --upgrade  \
    git \
    curl \
    chromium chromium-driver \
    firefox-esr

ENV CHROME_BIN=/usr/bin/chromium \
    CHROME_PATH=/usr/lib/chromium/

RUN apt-get clean && apt-get autoremove -y

COPY pom.xml /tmp/pom.xml
RUN mvn -B -f /tmp/pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
