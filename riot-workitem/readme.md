Running Riot workitems inside KIE workbench
========================================

In addition to the usual defining the workitem handlers in your
projects deployment descriptor, in order to execute processes that include
Riot workitems you have to add the following repository to your projects pom.xml:

```xml
<repository>
      <id>jitpack.io</id>
      <name>JitPack Repository</name>
      <url>https://jitpack.io</url>
      <layout>default</layout>
      <releases>
        <enabled>true</enabled>
        <updatePolicy>never</updatePolicy>
      </releases>
      <snapshots>
        <enabled>true</enabled>
        <updatePolicy>daily</updatePolicy>
      </snapshots>
</repository>
```