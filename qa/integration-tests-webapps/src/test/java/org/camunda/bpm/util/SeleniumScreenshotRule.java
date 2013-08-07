package org.camunda.bpm.util;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Allows to take screenshots in case of an selenium2 test error.
 *
 * @author Christian Lipphardt
 */
public class SeleniumScreenshotRule extends TestWatcher {

  private static Logger log = Logger.getAnonymousLogger();

  private WebDriver webDriver;

  public SeleniumScreenshotRule(WebDriver webDriver) {
    this.webDriver = webDriver;
  }

  @Override
  protected void failed(Throwable e, Description description) {
    File scrFile = ((TakesScreenshot) webDriver).getScreenshotAs(
        OutputType.FILE);
    String now = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    String scrFilename = "screenshot-" + description.getClassName() + "-" + description.getMethodName() + "-" + now + ".png";
    File outputFile = new File(computeScreenshotsRoot(description.getTestClass()), scrFilename);
    log.info(scrFilename + " screenshot created.");
    try {
      FileUtils.copyFile(scrFile, outputFile);
    } catch (IOException ioe) {
      log.severe("Error copying screenshot after exception.");
    }
  }

  public static File computeScreenshotsRoot(Class anyTestClass) {
       final String clsUri = anyTestClass.getName().replace('.','/') + ".class";
       final URL url = anyTestClass.getClassLoader().getResource(clsUri);
       final String clsPath = url.getPath();
       final File root = new File(clsPath.substring(0, clsPath.length() - clsUri.length()));
       final File clsFile = new File(root, clsUri);
       return new File(root.getParentFile(), "screenshots");
  }
}
