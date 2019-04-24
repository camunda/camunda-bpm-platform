/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.integrationtest.functional.transactions;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.functional.transactions.beans.FailingDelegate;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.transaction.Status;
import javax.transaction.UserTransaction;


/**
 * <p>Checks that activiti / application transaction sharing works as expected</p>
 *
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class TransactionIntegrationTest extends AbstractFoxPlatformIntegrationTest {
    
  @Deployment
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
      .addClass(FailingDelegate.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/transactions/TransactionIntegrationTest.testProcessFailure.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/transactions/TransactionIntegrationTest.testApplicationFailure.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/transactions/TransactionIntegrationTest.testTxSuccess.bpmn20.xml")     
      .addAsWebInfResource("persistence.xml", "classes/META-INF/persistence.xml");
  }
  
  @Inject
  private UserTransaction utx;
  
  @Inject
  private RuntimeService runtimeService;
  
  @Test
  public void testProcessFailure() throws Exception {
    
    /* if we start a transaction here and then start
     * a process instance which synchronously invokes a java delegate,
     * if that delegate fails, the transaction is marked rollback only
     */
    
    try {
      utx.begin();
                        
      try {
        runtimeService.startProcessInstanceByKey("testProcessFailure");
        Assert.fail("Exception expected");
      }catch (Exception ex) {
        if(!(ex instanceof RuntimeException)) {
          Assert.fail("Wrong exception of type "+ex+" RuntimeException expected!");
        }    
        if(!ex.getMessage().contains("I'm a complete failure!")) {
          Assert.fail("Different message expected");
        }
      }
      
      // assert that now our transaction is marked rollback-only:
      Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK, utx.getStatus());
      
    } finally {
      // make sure we always rollback
      utx.rollback();      
    }
  }
  
  @Test
  public void testApplicationFailure() throws Exception {
    
    /* if we start a transaction here and then successfully start
     * a process instance, if our transaction is rolled back, 
     * the process instnace is not persisted.
     */
    
    try {
      utx.begin();
                           
      String id = runtimeService.startProcessInstanceByKey("testApplicationFailure").getId();
      
      // assert that the transaction is in good shape:
      Assert.assertEquals(Status.STATUS_ACTIVE, utx.getStatus());
     
      // now rollback the transaction (simmulating an application failure after the process engine is done).
      utx.rollback();
      
      utx.begin();
      
      // the process instance does not exist:
      ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(id)
        .singleResult();
      
      Assert.assertNull(processInstance);
      
      utx.commit();
    }catch (Exception e) {
      utx.rollback();
      throw e;
    }
  }
  

  @Test
  public void testTxSuccess() throws Exception {
        
    try {
      utx.begin();
                           
      String id = runtimeService.startProcessInstanceByKey("testTxSuccess").getId();
      
      // assert that the transaction is in good shape:
      Assert.assertEquals(Status.STATUS_ACTIVE, utx.getStatus());
      
      // the process instance is visible form our tx:
      ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(id)
        .singleResult();
      
      Assert.assertNotNull(processInstance);
     
      utx.commit();
      
      utx.begin();
      
      // the process instance is visible in a new tx:
      processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(id)
        .singleResult();
      
      Assert.assertNotNull(processInstance);
      
      utx.commit();
    }catch (Exception e) {
      utx.rollback();
      throw e;
    }
  }
  
  
  
}
