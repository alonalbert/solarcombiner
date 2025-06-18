# docker build -t alonalbert/padserver .
# docker run -p 8080:8080 alonalbert/padserver
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
VOLUME /config
WORKDIR /config
COPY build/libs/server.jar /app.jar
ENTRYPOINT [ \
    "java", \
    "-jar", \
    "/app.jar" \
    ]
