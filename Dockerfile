# 使用 Java 11 运行环境
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 复制打包好的 jar 文件
COPY target/*.jar app.jar

# 暴露端口（Render 默认用 8080）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]