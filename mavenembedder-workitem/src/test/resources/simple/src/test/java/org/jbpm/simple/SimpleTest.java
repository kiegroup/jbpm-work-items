package org.jbpm.simple;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SimpleTest
        extends TestCase {

    public AppTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(SimpleTest.class);
    }

    public void testSimple() {
        assertTrue(true);
    }
}