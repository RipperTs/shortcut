# 使用OpenJDK 8作为基础镜像
FROM registry.cn-hangzhou.aliyuncs.com/ripper/openjdk:8-jdk-alpine

# 设置工作目录
WORKDIR /app

COPY target/shortcut-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9527
ENTRYPOINT ["java", "-jar", "app.jar"]