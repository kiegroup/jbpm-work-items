jBPM Workitem Integration Tests
========================================

jBPM workitem integration tests is a springboot sample application that is based on jbpm-spring-boot-starter-basic 
and provides all the infrastructure needed to test jBPM springboot with workitem handlers. 
It provides all services from jbpm-services-api that are directly available for injection/autowire.

Use following command to build the project:

```
mvn clean install -DskipTests
```

For repackaging after building the project (including start class in the MANIFEST.MF), use instead :

```
mvn clean package spring-boot:repackage -DskipTests
```


Running the application
------------------------------

Use following command to execute the project:

```
java -jar target/jbpm-workitem-itests-7.54.0.Final.jar evaluation:evaluation:1.0.0-SNAPSHOT
```

last part is the kjar that you would like to deploy in GAV format: **groupId:artifactId:version**

