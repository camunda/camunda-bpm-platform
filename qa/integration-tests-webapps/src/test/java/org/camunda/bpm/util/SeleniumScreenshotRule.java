/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.util;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Allows to take screenshots in case of an selenium test error.
 */
public class SeleniumScreenshotRule implements TestRule {

  private static Logger log = Logger.getAnonymousLogger();

  protected WebDriver webDriver;

  public SeleniumScreenshotRule(WebDriver driver) {
    if (driver instanceof RemoteWebDriver) {
      webDriver = new Augmenter().augment(driver);
    } else {
      webDriver = driver;
    }
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        try {
          base.evaluate();
        } catch (Throwable t) {
          captureScreenShot(description);
          throw t;
        }
      }

      public void captureScreenShot(Description describe) {
        File scrFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
        String now = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String scrFilename = describe.getClassName() + "-" + describe.getMethodName() + "-" + now + ".png";
        File outputFile = new File(computeScreenshotsRoot(describe.getTestClass()), scrFilename);
        log.info(scrFilename + " screenshot created.");
        try {
          FileUtils.copyFile(scrFile, outputFile);
        } catch (IOException ioe) {
          log.severe("Error copying screenshot after exception.");
        }
      }

      public File computeScreenshotsRoot(Class anyTestClass) {
        final String clsUri = anyTestClass.getName().replace('.','/') + ".class";
        final URL url = anyTestClass.getClassLoader().getResource(clsUri);
        final String clsPath = url.getPath();
        final File root = new File(clsPath.substring(0, clsPath.length() - clsUri.length()));
        final File clsFile = new File(root, clsUri);
        return new File(root.getParentFile(), "screenshots");
      }

    };
  }
}
