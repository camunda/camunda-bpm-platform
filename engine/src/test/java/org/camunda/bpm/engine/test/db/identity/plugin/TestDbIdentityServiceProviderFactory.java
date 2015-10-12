/**
 *
 */
package org.camunda.bpm.engine.test.db.identity.plugin;

import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;

/**
 * @author Simon Jonischkeit
 *
 */
public class TestDbIdentityServiceProviderFactory implements SessionFactory{

  @Override
  public Class<?> getSessionType() {
    // TODO Auto-generated method stub
    return TestDbIdentityServiceProviderExtention.class;
  }

  @Override
  public Session openSession() {
    return new TestDbIdentityServiceProviderExtention();
  }

}
