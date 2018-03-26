jBPM work item repository with SpringBoot
========================================

Allows you to run the jBPM work items repository as a SpringBoot application.

Running the repository locally
------------------------------
Build the jbpm-work-items project

```
mvn clean install
```

Go to the repository-springboot module

```
cd repository-springboot
```

Next you can start the repository with:

```
java -jar target/repository-springboot-7.8.0-SNAPSHOT.jar
```

Now you can access the repository at :

```
http://localhost:8090/repository
```

You can change the default port and context path via src/main/resources/application.properties.