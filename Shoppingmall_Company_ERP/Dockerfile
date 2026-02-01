# Build stage
FROM gradle:8.4-jdk17 AS build
WORKDIR /app
COPY . .
# Verify file structure and run build with info for debugging
RUN ls -la && gradle clean build -x test --info

# Run stage
FROM tomcat:10.1-jdk17-temurin
# Copy the generated war file to Tomcat's webapps directory as ROOT.war
COPY --from=build /app/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
