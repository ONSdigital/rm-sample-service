FROM eclipse-temurin:21-jre-alpine

RUN apk update
RUN groupadd -r appuser && useradd -r -g appuser -u 9000 appuser && chown -R appuser:appuser
USER appuser
COPY target/samplesvc.jar /opt/samplesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/samplesvc.jar" ]
