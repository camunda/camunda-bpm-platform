package com.camunda.fox.demo.twitter.test;

import java.io.File;

import javax.faces.component.UIComponent;

import junit.framework.Assert;

import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jsfunit.api.InitialPage;
import org.jboss.jsfunit.jsfsession.JSFClientSession;
import org.jboss.jsfunit.jsfsession.JSFServerSession;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;

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
              .artifact("com.camunda.fox.platform:fox-platform-client").resolveAsFiles());
      
      war.addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
              .loadMetadataFromPom("pom.xml")
              .artifact("org.jboss.jsfunit:jboss-jsfunit-core:2.0.0.Beta2").resolveAsFiles());
      
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
  @InitialPage("/processList.jsf")
  public void testStartProcessAndCreateNewTweet(JSFServerSession server, JSFClientSession client) throws Exception {
    Assert.assertNotNull(server);

    Assert.assertEquals("/processList.xhtml", server.getCurrentViewID());
      
    // start process
    client.click("0:start_process_link");
    
    Assert.assertEquals("/taskForm_newTweet.xhtml", server.getCurrentViewID());
    
    // create new tweet
    client.setValue("twitter_account", TWITTER_ACCOUNT);
    client.setValue("tweet_content", TWEET_CONTENT);
    client.click("submit_button");
    
    Assert.assertEquals("/taskList.xhtml", server.getCurrentViewID());
  }
  
  @Test
  @InitialPage("/taskList.jsf")
  public void testCompleteTaskForKermit(JSFServerSession server, JSFClientSession client) throws Exception {
    Assert.assertNotNull(server);
    
    Assert.assertEquals("/taskList.xhtml", server.getCurrentViewID());
    
    // complete task
    client.setValue("task_user", "kermit");
    client.click("0:task_complete_link");
    
    Assert.assertEquals("/taskForm_reviewTweet.xhtml", server.getCurrentViewID());
    
    // review tweet
    Object componentValue = server.findComponent("twitter_account");
    Assert.assertNotNull(componentValue);
    String pageAsText = client.getPageAsText();
    Assert.assertTrue(pageAsText.contains(TWITTER_ACCOUNT));
    Assert.assertTrue(pageAsText.contains(TWEET_CONTENT));
    
    client.click("checkbox_approve_publish");
    client.click("submit_button");
    
    Assert.assertEquals("/taskList.xhtml", server.getCurrentViewID());
    
    UIComponent component = server.findComponent("last_tweets");
    Assert.assertNotNull(component);
    
    pageAsText = client.getPageAsText();
    Assert.assertTrue(pageAsText.contains(TWEET_CONTENT));
  }
  
}
