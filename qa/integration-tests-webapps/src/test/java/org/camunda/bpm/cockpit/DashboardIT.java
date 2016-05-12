package org.camunda.bpm.cockpit;


import org.camunda.bpm.AbstractWebappUiIntegrationTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;


public class DashboardIT extends AbstractWebappUiIntegrationTest {

  public DashboardIT() {
    super("/camunda/app/cockpit");
  }

  @Test
  public void testLogin() throws URISyntaxException {
    driver.get(appUrl+"/#/login");

    WebDriverWait wait = new WebDriverWait(driver, 10);

    WebElement user = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"text\"]")));
    user.sendKeys("demo");

    WebElement password= wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"password\"]")));
    password.sendKeys("demo");

    WebElement submit = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type=\"submit\"]")));
    submit.submit();

    wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[ng-repeat=\"propName in procDefStatsKeys\"]:first-child > .value"), "1"));
    wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[ng-repeat=\"propName in procDefStatsKeys\"]:first-child > a"), "process definition"));

    wait.until(currentURIIs(new URI(appUrl + "/default/#/dashboard")));
  }

}
