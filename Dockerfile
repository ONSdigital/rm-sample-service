FROM openjdk 
ARG jar
VOLUME /tmp
ADD $jar samplesvc.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java -jar /samplesvc.jar" ]
