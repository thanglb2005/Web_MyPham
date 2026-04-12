# ---------- Stage 1: build JAR ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Tách layer: tải dependency trước (cache khi chỉ sửa code)
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Stage 2: runtime ----------
# JRE slim (Debian) — ổn định với JDBC SQL Server hơn Alpine một số trường hợp
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8", "-jar", "app.jar"]
