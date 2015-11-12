package org.camunda.bpm.engine.rest.standalone;

import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.ClassRule;

public class NoServletAuthenticationFilterTest extends AbstractAuthenticationFilterTest {

  @ClassRule
  public static TestContainerRule testContainer = new TestContainerRule();

}
