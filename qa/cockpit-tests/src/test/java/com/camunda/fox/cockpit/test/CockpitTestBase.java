package com.camunda.fox.cockpit.test;


import javax.enterprise.inject.spi.Extension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;

import com.camunda.fox.cdi.BeanManagerLookup;
import com.camunda.fox.cdi.FoxProcessEngineLookup;
import com.camunda.fox.cdi.LocalBeanManagerLookupExtension;
import com.camunda.fox.cdi.ProgrammaticBeanLookup;
import com.camunda.fox.cdi.persistence.EntityManagerFactories;
import com.camunda.fox.cockpit.service.AuditService;
import com.camunda.fox.cockpit.service.ExecutionService;
import com.camunda.fox.cockpit.service.JobService;
import com.camunda.fox.cockpit.service.ProcessDefinitionService;
import com.camunda.fox.cockpit.service.ProcessService;
import com.camunda.fox.cockpit.service.producer.ProcessServicesProducer;
import com.camunda.fox.cockpit.service.VariableService;
import com.camunda.fox.cockpit.service.query.impl.MyBatisService;
import com.camunda.fox.cockpit.spi.engine.impl.PlatformProcessEngineLookup;
import com.camunda.fox.cockpit.spi.engine.impl.PlatformProcessEngines;
import com.camunda.fox.cockpit.spi.persistence.impl.EeEntityManagerFactories;
import com.camunda.fox.cockpit.spi.persistence.impl.EeEntityManagerProducer;
import com.camunda.fox.cockpit.test.service.InternalProcessEngineComponentsProducer;
import com.camunda.fox.license.entity.FoxComponent;


/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class CockpitTestBase {
  
  public static WebArchive createBaseDeployment() {
    return ShrinkWrap
      .create(WebArchive.class)
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
        .addAsWebInfResource("META-INF/beans.xml", "beans.xml")
        .addAsResource("persistence.xml", "META-INF/persistence.xml")
        .addAsResource("com/camunda/fox/cockpit/persistence/mappings.xml")
        .addAsResource("com/camunda/fox/cockpit/persistence/auditMapping.xml")
        .addAsResource("com/camunda/fox/cockpit/persistence/processInstanceMapping.xml")
        .addAsResource("com/camunda/fox/cockpit/persistence/variableMapping.xml")
        .addPackages(true, 
          "com.camunda.fox.cockpit.entity",
          "com.camunda.fox.cockpit.model",
          "com.camunda.fox.cockpit.persistence")
        .addClasses(
          ProgrammaticBeanLookup.class, 
          BeanManagerLookup.class, 
          FoxProcessEngineLookup.class, 
          EntityManagerFactories.class, 
          ProcessService.class,
          VariableService.class,
          MyBatisService.class,
          AuditService.class,
          JobService.class,
          ExecutionService.class,
          ProcessDefinitionService.class, 
          ProcessServicesProducer.class)
      
        // Transaction 
        .addPackages(true, 
          "com.camunda.fox.cdi.transaction")
        
        // Platform environment
        .addPackage("com.camunda.fox.cockpit.spi.engine")
        .addClasses(
          PlatformProcessEngineLookup.class, 
          PlatformProcessEngines.class, 
          EeEntityManagerFactories.class, 
          EeEntityManagerProducer.class)
        // Security API
        .addPackage("com.camunda.fox.security.api")
        .addClass(FoxComponent.class)
        // Test base
        .addClass(CockpitTestBase.class)
        .addClass(InternalProcessEngineComponentsProducer.class)
        .addPackages(true, 
          "com.camunda.fox.cockpit.test.rule",
          "com.camunda.fox.cockpit.test.deployments",
          "com.camunda.fox.cockpit.test.delegate",
          "com.camunda.fox.cockpit.test.util");
  }
  
//  @Rule
//  public TransactionRule transactionRule = new TransactionRule();
//  
//  @Rule
//  public CockpitCleanupRule cockpitCleanupRule = new CockpitCleanupRule();
}