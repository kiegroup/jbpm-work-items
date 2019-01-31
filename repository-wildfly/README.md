jBPM work item repository with WildFly
========================================

This module builds the workitem repository war, as well as standalone
distribution zip including WildFly.

Running the standalone wildfly distribution
---------------------------------------------

Build the jbpm-work-items project

```
mvn clean install
```

Go to the repository-wildfly module

```
cd repository-wildfly
```

Unzip the repository-server-7.18.0-SNAPSHOT zip file into a directory
of your choice:

```
unzip jbpm-work-items/repository-wildfly/target/repository-server-7.18.0-SNAPSHOT.zip
```

Go to the wildfly server bin directory:

```
cd bin
```

Start the server:

```
./standalone.sh
```

Now you can access the repository at :

```
http://localhost:8080/repository
```
