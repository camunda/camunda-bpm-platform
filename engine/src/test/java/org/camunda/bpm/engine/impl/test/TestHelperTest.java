package org.camunda.bpm.engine.impl.test;

import junit.framework.TestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class TestHelperTest extends TestCase {

  @Test
  public void testGetMethodShouldWorkForPublicAccessor() throws NoSuchMethodException {
    // WHEN we call get method to retrieve a method with public accessor, no exception should be thrown
    TestHelper.getMethod(SomeTestClass.class, "testSomethingWithPublicAccessor", new Class[0]);
  }

  @Test
  public void testGetMethodShouldWorkForPackagePrivateAccessor() throws NoSuchMethodException {
    // WHEN we call get method to retrieve a method with package private accessor, no exception should be thrown
    TestHelper.getMethod(SomeTestClass.class, "testSomethingWithPackagePrivateAccessor", new Class[0]);
  }

  @Test
  public void testGetMethodShouldWorkForProtectedAccessor() throws NoSuchMethodException {
    // WHEN we call get method to retrieve a method with protected accessor, no exception should be thrown
    TestHelper.getMethod(SomeTestClass.class, "testSomethingWithProtected", new Class[0]);
  }

  @Deployment
  static class SomeTestClass {

    public void testSomethingWithPublicAccessor() {}
    void testSomethingWithPackagePrivateAccessor() {}
    protected void testSomethingWithProtected() {}
  }
}
