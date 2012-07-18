package com.camunda.fox.tasklist;

import java.io.File;
import java.net.URL;

import junit.framework.Assert;

import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.identity.User;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
public class TestTasklistGui {

  public static final String TEST_PROCESS_APPLICATION_ROOT = "../test-process-application/";

  public static final String WEB_INF_RESOURCES = TEST_PROCESS_APPLICATION_ROOT + "src/main/webapp/WEB-INF/";
  public static final String WEB_RESOURCES = TEST_PROCESS_APPLICATION_ROOT + "src/main/webapp/";

  @Deployment
  public static WebArchive createDeployment() {
    WebArchive war = ShrinkWrap.create(WebArchive.class, "test-process-application.war")
            .addAsWebInfResource(new File(WEB_INF_RESOURCES + "faces-config.xml"), "faces-config.xml")
            .addAsWebInfResource(new File(WEB_INF_RESOURCES + "beans.xml"), "beans.xml")
            .addAsWebInfResource(new File(WEB_INF_RESOURCES + "templates/template.xhtml"), "templates/template.xhtml")
            
            .addAsWebResource(new File(WEB_RESOURCES + "app/start.xhtml"), "app/start.xhtml")
            .addAsWebResource(new File(WEB_RESOURCES + "app/userTask.xhtml"), "app/userTask.xhtml")
            
            .addAsResource(new File(TEST_PROCESS_APPLICATION_ROOT + "src/main/resources/test-process.bpmn"), "test-process.bpmn")
            .addAsResource(new File(TEST_PROCESS_APPLICATION_ROOT + "src/main/resources/META-INF/processes.xml"), "META-INF/processes.xml")
            
            .addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
            .goOffline().loadMetadataFromPom(TEST_PROCESS_APPLICATION_ROOT + "pom.xml")
            .artifact("com.camunda.fox.platform:fox-platform-client")
            .artifact("org.seleniumhq.selenium:selenium-api:2.20.0")
            .resolveAsFiles());

    return war;
  }

  @Test
  public void testDeploymentAndPrepareUsers() {
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    long count = repositoryService.createProcessDefinitionQuery().processDefinitionKey("test_process").count();

    Assert.assertEquals(1, count);

    IdentityService identityService = processEngine.getIdentityService();
    User kermit = identityService.newUser("kermit");
    kermit.setPassword("kermit");
    identityService.saveUser(kermit);
  }

  @Test
  @RunAsClient
  public void testApplication(@Drone WebDriver driver, @ArquillianResource URL contextPath) {
    driver.get(contextPath + "../tasklist");
    
    Assert.assertTrue(driver.getCurrentUrl().contains("tasklist/signin.jsf"));
    
    // sign in
    driver.findElement(By.id("signin:username")).sendKeys("kermit");
    driver.findElement(By.id("signin:password")).sendKeys("kermit");
    driver.findElement(By.id("signin:submit_button")).submit();
    
    Assert.assertTrue(driver.getCurrentUrl().endsWith("tasklist/app/taskList.jsf"));
    
    // go to start form of "Test Process"
    driver.findElement(By.linkText("Start Process...")).click();
    driver.findElement(By.xpath("//*[@id=\"j_idt21:0:j_idt23\"]/a")).click();
    
    Assert.assertTrue(driver.getCurrentUrl().contains("test-process-application/app/start.jsf"));
    
    // fill start form and submit it  
    driver.findElement(By.id("startForm:someVariable")).sendKeys("some text");
    driver.findElement(By.id("startForm:submit_button")).submit();
    
    // make sure redirection to task-list works
    Assert.assertTrue(driver.getCurrentUrl().endsWith("tasklist/app/taskList.jsf"));
    
    // make sure the task shows up in the task-list
    Assert.assertTrue(driver.findElement(By.xpath("//*[@id=\"taskListView\"]/table/tbody/tr/td[1]")).getText().equals("User Task"));
    
    // click edit task link and go to task form
    driver.findElement(By.xpath("//*[@id=\"taskListView\"]/table/tbody/tr/td[4]/a")).click();
    Assert.assertTrue(driver.getCurrentUrl().contains("test-process-application/app/userTask.jsf"));

    // submit task form and make sure the redirection to task-list works
    driver.findElement(By.id("userTaskForm:submit_button")).submit();
    Assert.assertTrue(driver.getCurrentUrl().endsWith("tasklist/app/taskList.jsf"));

    // make sure the task-list is empty
    Assert.assertTrue(driver.findElement(By.xpath("//*[@id=\"taskListView\"]/table/tbody/tr/td[1]")).getText().equals("-"));
  }
}
