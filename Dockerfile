FROM openjdk:8-jre

VOLUME /tmp
ARG JAR_FILE=samplesvc*.jar
COPY target/$JAR_FILE /opt/samplesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/samplesvc.jar" ]
