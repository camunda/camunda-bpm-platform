package com.camunda.fox.platform.qa.deployer.event;

import org.jboss.arquillian.test.spi.event.suite.TestEvent;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class BeforeFoxTest extends TestEvent {

  public BeforeFoxTest(TestEvent testEvent) {
    super(testEvent.getTestInstance(), testEvent.getTestMethod());
  }
}