/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.client.impl.extensions;

import javax.inject.Inject;

import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ProcessEngine;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * This demonstrates how you can use something like 
 * {@link TenantAwareActivitiServices} to manage your
 * process engines in a contextual way.
 * 
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class TestTenantAwareActivitiServices {
  
  @Deployment
  public static JavaArchive createDeployment() {
    return ShrinkWrap.create(JavaArchive.class)
      .addPackages(true, "org.activiti.cdi")
      .addClass(TenantAwareActivitiServices.class)
      .addClass(MockProcessArchiveSupport.class)
      .addClass(MockPrincipal.class)
      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
  }
    
  @Inject
  private MockPrincipal mockPrincipal;
  
  @Test
  public void testTenant1() {
   mockPrincipal.setName("tenant1");
   Assert.assertEquals("tenant1Engine", ProgrammaticBeanLookup.lookup(ProcessEngine.class).getName());
  }
  
  @Test
  public void ensureRequestScoped() {
    // this is just to demonstrate that in the context of the same request, you
    // will always get the same process engine.
    mockPrincipal.setName("tenant1");
    Assert.assertEquals("tenant1Engine", ProgrammaticBeanLookup.lookup(ProcessEngine.class).getName());    
    mockPrincipal.setName("tenant2");
    // if you want this to be 'tenant2Engine', you'd have to alter 
    // the scope of the producers in TenantAwareActivitiServices to 
    // @Dependent
    Assert.assertEquals("tenant1Engine", ProgrammaticBeanLookup.lookup(ProcessEngine.class).getName());
  }
  
  @Test
  public void testTenant2() {
    mockPrincipal.setName("tenant2");
    Assert.assertEquals("tenant2Engine", ProgrammaticBeanLookup.lookup(ProcessEngine.class).getName());
  }
}
