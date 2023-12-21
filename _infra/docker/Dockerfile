FROM eclipse-temurin:17-jre-alpine

RUN apk update
COPY target/samplesvc.jar /opt/samplesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/samplesvc.jar" ]
