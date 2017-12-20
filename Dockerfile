ARG JAR_FILE=samplesvc*.jar
FROM openjdk:8-jre

VOLUME /tmp
ARG JAR_FILE
COPY target/$JAR_FILE /opt/samplesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/samplesvc.jar" ]
