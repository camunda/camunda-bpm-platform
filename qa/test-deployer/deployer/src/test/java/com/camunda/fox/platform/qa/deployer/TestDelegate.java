
package com.camunda.fox.platform.qa.deployer;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class TestDelegate implements JavaDelegate {

  public static boolean INVOKED = false;
  
  public void execute(DelegateExecution de) throws Exception {
    INVOKED = true;
  }
}
