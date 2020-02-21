FROM openjdk:8-jre-slim

RUN groupadd --gid 999 samplesvc && \
    useradd --create-home --system --uid 999 --gid samplesvc samplesvc
USER samplesvc

COPY samplesvc.jar /opt/samplesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/samplesvc.jar" ]