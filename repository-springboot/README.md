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
java -jar target/repository-springboot-7.10.0-SNAPSHOT.jar
```

Now you can access the repository at :

```
http://localhost:8090/repository
```

You can change the default port and context path via src/main/resources/application.properties.

Running the repository on Heroku
------------------------------
Heroku requirements:
1. Create account and log in
2. Create app called "repository-springboot". If you choose a different name for your app
make sure to pass it to maven when running heroku:deploy command with -Dheroku.appName=YOUR_APP_NAME
3. Download the Heroku CLI and use it to log in with "heroku login" and enter in your credentials

Build the jbpm-work-items project

```
mvn clean install
```

Go to the repository-springboot module

```
cd repository-springboot
```

Deploy the repository springboot jar to Heroku

```
mvn clean heroku:deploy
```
Remember if you used a different app name pass it like so

```
mvn clean heroku:deploy -Dheroku.appName=YOUR_APP_NAME
```

Open the Heroku dashboard, click on your app and the "Open app" button. You can also view any logs via the dashboard as well

As example you can view http://repository-springboot.herokuapp.com/repository/ but it 
might not include the latest version so deploying to your local Heroku app is recommended to 
control deployed version.
