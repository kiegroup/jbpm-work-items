/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process.workitem.java;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.process.workitem.config.CustomConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CustomConfig.class)
public class MyJavaClass {

    @Autowired
    CustomConfig customConfig;

    private static final Logger logger = LoggerFactory.getLogger(MyJavaClass.class);

    public MyJavaClass() {
    }

    public static String staticMethod1() {
        return "Hello World";
    }

    public static String staticMethod2(String name) {
        return "Hello " + name;
    }

    public String myFirstMethod(String name,
                                Integer age) {
        return "Hello " + name + ", age " + age;
    }

    public String myFirstMethod(String name,
                                String age) {
        return "Hello " + name + ", age " + age;
    }

    public String myFirstMethod(String name,
                                Integer age,
                                String gender) {
        return "Hello " + name + ", age " + age + ", gender " + gender;
    }

    public List<String> mySecondMethod(String name,
                                       List<String> children) {
        List<String> result = new ArrayList<String>();
        for (String child : children) {
            result.add("Hello " + child);
        }
        return result;
    }

    // some custom config from the spring context is used in the java process
    // will throw a NPE as Spring Autowiring will not work with construction by reflection
    // https://github.com/kiegroup/jbpm-work-items/blob/main/java-workitem/src/main/java/org/jbpm/process/workitem/handler/JavaHandlerWorkItemHandler.java#L79
    // It should be possible to make a SpringHandlerWorkItemHandler that does a bean lookup instead of creating a new instance
    public String myThirdMethodThatUsesConfig(String name,
            Integer age) {
        String name1 = customConfig.getName();
        return "Hello " + name + ", age " + age;
    }

    public void writeHello(String name) {
        logger.info("Hello {}",
                    name);
    }
}
