package org.camunda.bpm.tasklist;


import org.camunda.bpm.AbstractWebappUiIntegrationTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class TasklistIT extends AbstractWebappUiIntegrationTest {

  public TasklistIT() {
    super("/camunda/app/tasklist");
  }

  @Test
  public void testLogin() {
    driver.get(appUrl + "/#/login");

    WebDriverWait wait = new WebDriverWait(driver, 10);

    WebElement user = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"text\"]")));
    user.sendKeys("demo");

    WebElement password= wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"password\"]")));
    password.sendKeys("demo");

    WebElement submit = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type=\"submit\"]")));
    submit.submit();
  }

}
