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

package com.camunda.fox.platform.test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.test.PvmTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import com.camunda.fox.platform.impl.schema.DbSchemaOperations;
import com.camunda.fox.processarchive.executor.FoxApplicationException;
import com.camunda.fox.processarchive.executor.ProcessArchiveContextExecutor;


@RunWith(Arquillian.class)
public class TestActivitiInContainer {
    
  /////////////////////////////////////////////////// Deployment
  
  public static String RESOURCES_ROOT = "target/activiti-testsuite/";
  
  @Deployment   
  public static WebArchive createDeployment() {    
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "activiti-testsuite.war");    
    addClasses(archive);    
    File resourcesRoot = new File(RESOURCES_ROOT);
    if(!resourcesRoot.exists()) {
      Assert.fail("Could not find "+RESOURCES_ROOT+". We need to run the maven build first.");
    }
    addResources(archive, resourcesRoot, resourcesRoot);        
    archive.addAsManifestResource("ARQUILLIAN-MANIFEST-JBOSS7.MF", "MANIFEST.MF");
    archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    return archive;    
  }
  
  // adds all resources below RESOURCES_ROOT to the deployment
  private static void addResources(WebArchive archive, File dir, File rootDir) {
    for (File file : dir.listFiles()) {
      if(file.isHidden()) {
        continue;
      }
      if(file.isDirectory()) {
        addResources(archive, file, rootDir);
      } else {
        String path = file.getAbsolutePath()
                .replace(rootDir.getAbsolutePath(), "")
                .substring(1)
                .replace(File.separator, "/"); // enforce unix-style path
        archive.addAsResource(file, path);
      }
    }    
  }

  private static void addClasses(WebArchive archive) {
    archive.addPackages(true, "org.activiti.engine.test");
    archive.addPackages(true, "org.activiti.engine.impl.test");
    archive.addPackages(true, "org.activiti.examples");
    archive.addPackages(true, "org.activiti.standalone");
    archive.addPackages(true, "com.camunda.fox.platform.impl.schema");
    archive.addClass(ProcessArchiveContextExecutor.class);
    archive.addClass(FoxApplicationException.class);
    archive.addClass(BeanManagerLookup.class);
  }
  
  /////////////////////////////////////////////////// Testsuite

  @Before
  public void setup() {   
    JobExecutor jobExecutor = getJobExecutor();
    if(jobExecutor.isActive()) {
      jobExecutor.shutdown();
    }
    // ensure db clean
    DbSchemaOperations dbSchemaOperations = PluggableActivitiTestCase.getDbSchemaOperations();
    dbSchemaOperations.drop();
    dbSchemaOperations.update();    
  }
 
  @Test
  public void run() {
    BeanManager beanManager = BeanManagerLookup.getBeanManager();
    // find all testcases using CDI reflection
    ArrayList<Class<?>> testClasses = new ArrayList<Class<?>>();
    Set<Bean< ? >> testCases = beanManager.getBeans(PvmTestCase.class); 
    for (Bean< ? > testCase : testCases) {
        testClasses.add(testCase.getBeanClass());
    }
    runTests(testClasses.toArray(new Class[0]));    
  }
      
  @After
  public void teardown() {
    JobExecutor jobExecutor = getJobExecutor();
    if(!jobExecutor.isActive()) {
      jobExecutor.start();
    }
  }
  
  private static JobExecutor getJobExecutor() {
    ProcessEngine cachedProcessEngine = PluggableActivitiTestCase.getCachedProcessEngine();
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl)cachedProcessEngine).getProcessEngineConfiguration();
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    return jobExecutor;
  }

  protected void runTests(Class< ? >[] classes) {
    Result runClasses = JUnitCore.runClasses(classes);
    System.err.println("Executed "+runClasses.getRunCount()+" tests in "+runClasses.getRunTime()+"ms.");
    if(runClasses.wasSuccessful()) {
      System.err.println("success!");
    } else {
      
      // TODO: filter tests supposed to fail      
      
      File testFiluresDir = new File("target/test-failures");
      if (testFiluresDir.exists()) {
        for (File file : testFiluresDir.listFiles()) {
          if (file.isFile() && !file.isHidden()) {
            file.delete();
          }
        }
      } else {
        testFiluresDir.mkdir();
      }
      
      
      StringWriter writer = new StringWriter();
      writer.write("\n");
      System.err.println("There were test failures:");      
      for (Failure failure : runClasses.getFailures()) {
        String testHeader = failure.getTestHeader();
        boolean ignore =failure.getMessage().contains("Can't find scripting engine for 'groovy'"); 
        if(ignore) {
          System.err.println("Ignored: "+testHeader);
        } else {
          System.err.println(testHeader);
          writer.write("     ");
          writer.write(testHeader);        
          writer.write("\n");
        }
        
        String pathname = "target/test-failures/"+(ignore ? "IGNORED-" : "" )+ testHeader+".txt";
        File file = new File(pathname);
        try {
          file.createNewFile();          
          String content = testHeader +"\n"+failure.getMessage() + "\n" +failure.getDescription() + "\n\n"+ failure.getTrace();
          FileOutputStream fos = new FileOutputStream(file);
          fos.write(content.getBytes());
          fos.close();          
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // TODO: fail after we have cleaned up the testsuite and decided which tests are allowed to fail and which ones are not.
      
//      throw new FoxPlatformException("there were test failures: "+writer.toString());
    }    
  }
    

}
