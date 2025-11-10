# docker build -t alonalbert/enphase-monitor-server .
# docker run -p 8080:8080 alonalbert/enphase-monitor-server
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
VOLUME /config
WORKDIR /config
COPY build/libs/server.jar /app.jar
ENTRYPOINT [ \
    "java", \
    "-jar", \
    "/app.jar" \
    ]
