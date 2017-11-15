FROM openjdk:8-jre

VOLUME /tmp
COPY target/samplesvc*.jar /opt/samplesvc.jar

ENTRYPOINT [ "java", "-jar", "/opt/samplesvc.jar" ]
