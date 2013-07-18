package org.camunda.bpm.cockpit.test;

import javax.ws.rs.core.MediaType;

import org.apache.http.impl.client.DefaultHttpClient;
import org.camunda.bpm.TestProperties;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

public class DashboardIT {

  protected WebDriver driver;
  protected String appUrl;
  
  protected ApacheHttpClient4 client;
  protected DefaultHttpClient defaultHttpClient;
  
  protected TestProperties testProperties;
  
  protected String getApplicationContextPath() {
    return "engine-rest/";
  }

  @Before
  public void before() throws Exception {
    testProperties = new TestProperties(48080);
    appUrl = testProperties.getApplicationPath("/camunda/app/cockpit");
    driver = new FirefoxDriver();
    
    // create admin user:    
    ClientConfig clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig);

    defaultHttpClient = (DefaultHttpClient) client.getClientHandler().getHttpClient();
    
    UserDto user = new UserDto();
    UserCredentialsDto credentials = new UserCredentialsDto();    
    credentials.setPassword("admin");
    user.setCredentials(credentials);
    UserProfileDto profile = new UserProfileDto();
    profile.setId("admin");
    profile.setFirstName("Mr.");
    profile.setLastName("Admin");
    user.setProfile(profile);
    
    WebResource webResource = client.resource(testProperties.getApplicationPath("/engine-rest/user/create"));
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, user);
    clientResponse.close();
  }

  @Test
  public void testLogin() {
    driver.get(appUrl+"/#/login");

    WebDriverWait wait = new WebDriverWait(driver, 10);

    WebElement user = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"text\"]")));
    user.sendKeys("admin");

    WebElement password= wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"password\"]")));
    password.sendKeys("admin");

    WebElement submit = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type=\"submit\"]")));
    submit.submit();

    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.tile")));
    element.click();
    Boolean found = wait.until(ExpectedConditions.textToBePresentInElement(By.tagName("h1"), "invoice receipt"));
  }

  @After
  public void after() {
    driver.close();
    
    // delete admin user
    WebResource webResource = client.resource(testProperties.getApplicationPath("/engine-rest/user/admin"));
    webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).delete();
    
    client.destroy();
  }

}
