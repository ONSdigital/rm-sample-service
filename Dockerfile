FROM openjdk:8-jre-slim

RUN apt-get update
COPY target/samplesvc-UNVERSIONED.jar /opt/samplesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/samplesvc.jar" ]
