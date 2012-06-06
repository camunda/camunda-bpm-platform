package com.camunda.fox.demo.twitter.test;

import java.io.File;
import java.net.URL;

import junit.framework.Assert;

import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
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

import com.camunda.fox.demo.twitter.jsf.CurrentUser;
import com.camunda.fox.demo.twitter.jsf.DontTweetService;
import com.camunda.fox.demo.twitter.jsf.ProcessList;
import com.camunda.fox.demo.twitter.jsf.TaskList;
import com.camunda.fox.demo.twitter.jsf.TweetFeed;
import com.camunda.fox.demo.twitter.jsf.TweetService;

@RunWith(Arquillian.class)
public class TestTwitterDemoProcess {
  
  public static final String WEB_INF_RESOURCES = "src/main/webapp/WEB-INF/";
  public static final String WEB_RESOURCES = "src/main/webapp";
  
  public static final String TWEET_CONTENT = "my first twitter text!";
  public static final String TWITTER_ACCOUNT = "kermit";
  
  @Deployment
  public static WebArchive createDeployment() {
    WebArchive war = ShrinkWrap.create(WebArchive.class, "twitterDemo.war")
              //.setWebXML(new File(WEB_INF_RESOURCES + "web.xml"))
              .addPackage("com.camunda.fox.demo.twitter.jsf")
              .addAsWebInfResource(new File(WEB_INF_RESOURCES + "faces-config.xml"), "faces-config.xml")
              .addAsWebInfResource(new File(WEB_INF_RESOURCES + "beans.xml"), "beans.xml")
              .addAsWebInfResource(new File(WEB_INF_RESOURCES + "jboss-web.xml"), "jboss-web.xml")
              .addAsWebInfResource(new File(WEB_INF_RESOURCES + "web.xml"), "web.xml")
              .addAsWebInfResource(new File(WEB_INF_RESOURCES + "templates/template.xhtml"),"templates/template.xhtml");
    
      war.addAsWebResource(new File(WEB_RESOURCES,"index.html"))
         .addAsWebResource(new File(WEB_RESOURCES,"processList.xhtml"))
         .addAsWebResource(new File(WEB_RESOURCES,"taskForm_changeTweet.xhtml"))
         .addAsWebResource(new File(WEB_RESOURCES,"taskForm_newTweet.xhtml"))
         .addAsWebResource(new File(WEB_RESOURCES,"taskForm_reviewTweet.xhtml"))
         .addAsWebResource(new File(WEB_RESOURCES,"taskList.xhtml"));
      
      war.addAsResource("diagrams/twitterProcess.bpmn20.xml");
      war.addAsResource("META-INF/processes.xml", "META-INF/processes.xml");
      
      war.addClasses(CurrentUser.class, DontTweetService.class, ProcessList.class, TaskList.class, TweetFeed.class, TweetService.class);
     
      war.addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class).goOffline()
              .loadMetadataFromPom("pom.xml")
              .artifact("com.camunda.fox.platform:fox-platform-client")
              .artifact("org.seleniumhq.selenium:selenium-api:2.20.0")
              .resolveAsFiles());
      
      return war;
  }
  
  @Test
  public void testDeployment() {
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    long count = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("twitterProcess")
      .count();
    
    Assert.assertEquals(1, count);
  }
  
  @Test
  @RunAsClient
  public void testApplication(@Drone WebDriver driver, @ArquillianResource URL contextPath) {
    driver.get(contextPath + "processList.jsf");
    Assert.assertEquals(contextPath + "processList.jsf", driver.getCurrentUrl());
    
    // start new process
    driver.findElement(By.linkText("Start")).click();
    
    Assert.assertEquals(contextPath + "taskForm_newTweet.jsf?processDefinitionKey=twitterProcess", driver.getCurrentUrl());
    
    // create new tweet
    driver.findElement(By.id("newTweetForm:twitter_account")).sendKeys(TWITTER_ACCOUNT);
    driver.findElement(By.id("newTweetForm:tweet_content")).sendKeys(TWEET_CONTENT);
    driver.findElement(By.id("newTweetForm:submit_button")).submit();
    
    Assert.assertEquals(contextPath + "taskList.jsf", driver.getCurrentUrl());
    
    // complete task
    driver.findElement(By.id("taskListForm:task_user")).sendKeys("kermit");
    driver.findElement(By.id("taskListForm:taskList:0:task_complete_link")).click();
    
    Assert.assertTrue(driver.getCurrentUrl().contains(contextPath + "taskForm_reviewTweet.jsf"));
    
    // review tweet
    Assert.assertEquals(TWITTER_ACCOUNT, driver.findElement(By.id("reviewTweetForm:twitter_account")).getText());
    Assert.assertEquals(TWEET_CONTENT, driver.findElement(By.id("reviewTweetForm:tweet_content")).getText());

    driver.findElement(By.id("reviewTweetForm:checkbox_approve_publish")).click();
    driver.findElement(By.id("reviewTweetForm:submit_button")).submit();
    
    Assert.assertEquals(contextPath + "taskList.jsf", driver.getCurrentUrl());
    Assert.assertTrue(driver.getPageSource().contains(TWEET_CONTENT));
  }
}
