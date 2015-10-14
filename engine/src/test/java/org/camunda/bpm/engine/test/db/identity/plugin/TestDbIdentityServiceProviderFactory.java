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
    return TestDbIdentityServiceProviderExtension.class;
  }

  @Override
  public Session openSession() {
    return new TestDbIdentityServiceProviderExtension();
  }

}
