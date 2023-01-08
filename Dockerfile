FROM eclipse-temurin:11-jre-alpine

COPY target/pronouns-standalone.jar /pronoun.is/app.jar

ENV PORT 3000

EXPOSE 3000

CMD ["java", "-XX:-OmitStackTraceInFastThrow", "-jar", "/pronoun.is/app.jar" ]
