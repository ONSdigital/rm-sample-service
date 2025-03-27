FROM eclipse-temurin:21-jre-alpine

RUN apk update
RUN addgroup -S appuser && adduser -S appuser -G appuser
USER appuser
COPY target/samplesvc.jar /opt/samplesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/samplesvc.jar" ]
