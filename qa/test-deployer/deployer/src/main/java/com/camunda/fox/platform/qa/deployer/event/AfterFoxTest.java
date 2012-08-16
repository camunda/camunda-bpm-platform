package com.camunda.fox.platform.qa.deployer.event;

import org.jboss.arquillian.test.spi.event.suite.TestEvent;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class AfterFoxTest extends TestEvent {

  public AfterFoxTest(TestEvent testEvent) {
    super(testEvent.getTestInstance(), testEvent.getTestMethod());
  }
}