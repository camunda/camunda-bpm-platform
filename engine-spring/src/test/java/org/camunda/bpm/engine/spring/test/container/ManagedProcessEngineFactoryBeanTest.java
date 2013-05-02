/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.spring.test.container;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.spring.container.ManagedProcessEngineFactoryBean;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>Testcase for {@link ManagedProcessEngineFactoryBean}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ManagedProcessEngineFactoryBeanTest {
  
  @Test
  public void testProcessApplicationDeployment() {
    
    // initially, no process engine is registered:
    Assert.assertNull(BpmPlatform.getDefaultProcessEngine());
    Assert.assertEquals(0, BpmPlatform.getProcessEngineService().getProcessEngines().size());
    
    // start spring application context
    AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("org/camunda/bpm/engine/spring/test/container/ManagedProcessEngineFactoryBean-context.xml");
    applicationContext.start();
    
    // assert that now the process engine is registered:
    Assert.assertNotNull(BpmPlatform.getDefaultProcessEngine());      
    
    // close the spring application context
    applicationContext.close();
    
    // after closing the application context, the process engine is gone
    Assert.assertNull(BpmPlatform.getDefaultProcessEngine());
    Assert.assertEquals(0, BpmPlatform.getProcessEngineService().getProcessEngines().size());
    
  }
    
}
