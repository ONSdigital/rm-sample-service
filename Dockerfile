FROM openjdk:8-jre

VOLUME /tmp
COPY target/samplesvc*.jar /opt/samplesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/samplesvc.jar" ]
