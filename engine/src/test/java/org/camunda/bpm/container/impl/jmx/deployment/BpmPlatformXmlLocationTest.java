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
package org.camunda.bpm.container.impl.jmx.deployment;

import static org.camunda.bpm.container.impl.deployment.AbstractParseBpmPlatformXmlStep.BPM_PLATFORM_XML_FILE;
import static org.camunda.bpm.container.impl.deployment.AbstractParseBpmPlatformXmlStep.BPM_PLATFORM_XML_LOCATION;
import static org.camunda.bpm.container.impl.deployment.AbstractParseBpmPlatformXmlStep.BPM_PLATFORM_XML_SYSTEM_PROPERTY;
import static org.camunda.bpm.container.impl.tomcat.deployment.TomcatParseBpmPlatformXmlStep.CATALINA_HOME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.camunda.bpm.container.impl.tomcat.deployment.TomcatParseBpmPlatformXmlStep;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContext;

/**
 * Checks the correct retrieval of bpm-platform.xml file through JNDI,
 * environment variable, classpath and Tomcat's conf directory.
 *
 * @author Christian Lipphardt
 *
 */
public class BpmPlatformXmlLocationTest {

  private static final String BPM_PLATFORM_XML_LOCATION_PARENT_DIR = getBpmPlatformXmlLocationParentDir();
  private static final String BPM_PLATFORM_XML_LOCATION_ABSOLUTE_DIR = BPM_PLATFORM_XML_LOCATION_PARENT_DIR + File.separator + "conf";
  private static final String BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION = BPM_PLATFORM_XML_LOCATION_ABSOLUTE_DIR + File.separator + BPM_PLATFORM_XML_FILE;

  private static final String BPM_PLATFORM_XML_LOCATION_RELATIVE_PATH = "home/hawky4s/.camunda";

  private static final String BPM_PLATFORM_XML_LOCATION_VALID_PATH_UNIX = "/" + BPM_PLATFORM_XML_LOCATION_RELATIVE_PATH;
  private static final String BPM_PLATFORM_XML_LOCATION_VALID_PATH_WINDOWS = "C:\\users\\hawky4s\\.camunda";

  private static final String BPM_PLATFORM_XML_LOCATION_FILE_INVALID_PATH_UNIX = "C:" + File.separator + BPM_PLATFORM_XML_FILE;
  private static final String BPM_PLATFORM_XML_LOCATION_FILE_INVALID_PATH_WINDOWS = "C://users//hawky4s//.camunda//" + BPM_PLATFORM_XML_FILE;

  private static final String BPM_PLATFORM_XML_LOCATION_URL_HTTP_PROTOCOL = "http://localhost:8080/camunda/" + BPM_PLATFORM_XML_FILE;
  private static final String BPM_PLATFORM_XML_LOCATION_URL_HTTPS_PROTOCOL = "https://localhost:8080/camunda/" + BPM_PLATFORM_XML_FILE;

  @Rule
  public MockInitialContextRule initialContextRule = new MockInitialContextRule(new SimpleNamingContext());

  @Test
  public void checkValidBpmPlatformXmlResourceLocationForUrl() throws NamingException, MalformedURLException {
    TomcatParseBpmPlatformXmlStep tomcatParseBpmPlatformXmlStep = new TomcatParseBpmPlatformXmlStep();

    assertNull(tomcatParseBpmPlatformXmlStep.checkValidUrlLocation(BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION));
    assertNull(tomcatParseBpmPlatformXmlStep.checkValidUrlLocation(BPM_PLATFORM_XML_LOCATION_FILE_INVALID_PATH_WINDOWS));
    assertNull(tomcatParseBpmPlatformXmlStep.checkValidUrlLocation(BPM_PLATFORM_XML_LOCATION_FILE_INVALID_PATH_UNIX));
    assertNull(tomcatParseBpmPlatformXmlStep.checkValidUrlLocation(BPM_PLATFORM_XML_LOCATION_VALID_PATH_WINDOWS));
    assertNull(tomcatParseBpmPlatformXmlStep.checkValidUrlLocation(BPM_PLATFORM_XML_LOCATION_VALID_PATH_UNIX));

    URL httpUrl = tomcatParseBpmPlatformXmlStep.checkValidUrlLocation(BPM_PLATFORM_XML_LOCATION_URL_HTTP_PROTOCOL);
    assertEquals(BPM_PLATFORM_XML_LOCATION_URL_HTTP_PROTOCOL, httpUrl.toString());
    URL httpsUrl = tomcatParseBpmPlatformXmlStep.checkValidUrlLocation(BPM_PLATFORM_XML_LOCATION_URL_HTTPS_PROTOCOL);
    assertEquals(BPM_PLATFORM_XML_LOCATION_URL_HTTPS_PROTOCOL, httpsUrl.toString());
  }

  @Test
  public void checkValidBpmPlatformXmlResourceLocationForFile() throws NamingException, MalformedURLException {
    TomcatParseBpmPlatformXmlStep tomcatParseBpmPlatformXmlStep = new TomcatParseBpmPlatformXmlStep();

    URL url = tomcatParseBpmPlatformXmlStep.checkValidFileLocation(BPM_PLATFORM_XML_LOCATION_RELATIVE_PATH);
    assertNull("Relative path is invalid.", url);

    url = tomcatParseBpmPlatformXmlStep.checkValidFileLocation(BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION);
    assertEquals(new File(BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION).toURI().toURL(), url);

    url = tomcatParseBpmPlatformXmlStep.checkValidFileLocation(BPM_PLATFORM_XML_LOCATION_FILE_INVALID_PATH_WINDOWS);
    assertNull("Path is invalid.", url);

    assertNull(tomcatParseBpmPlatformXmlStep.checkValidFileLocation(BPM_PLATFORM_XML_LOCATION_URL_HTTP_PROTOCOL));
    assertNull(tomcatParseBpmPlatformXmlStep.checkValidFileLocation(BPM_PLATFORM_XML_LOCATION_URL_HTTPS_PROTOCOL));
  }

  @Test
  public void checkUrlAutoCompletion() throws NamingException, MalformedURLException {
    TomcatParseBpmPlatformXmlStep tomcatParseBpmPlatformXmlStep = new TomcatParseBpmPlatformXmlStep();

    String correctedUrl = tomcatParseBpmPlatformXmlStep.autoCompleteUrl(BPM_PLATFORM_XML_LOCATION_VALID_PATH_UNIX);
    assertEquals(BPM_PLATFORM_XML_LOCATION_VALID_PATH_UNIX + "/" + BPM_PLATFORM_XML_FILE, correctedUrl);

    correctedUrl = tomcatParseBpmPlatformXmlStep.autoCompleteUrl(BPM_PLATFORM_XML_LOCATION_VALID_PATH_UNIX + "/");
    assertEquals(BPM_PLATFORM_XML_LOCATION_VALID_PATH_UNIX + "/" + BPM_PLATFORM_XML_FILE, correctedUrl);

    correctedUrl = tomcatParseBpmPlatformXmlStep.autoCompleteUrl(BPM_PLATFORM_XML_LOCATION_VALID_PATH_WINDOWS);
    assertEquals(BPM_PLATFORM_XML_LOCATION_VALID_PATH_WINDOWS + "\\" + BPM_PLATFORM_XML_FILE, correctedUrl);
  }

  @Test
  public void checkValidBpmPlatformXmlResourceLocation() throws NamingException, MalformedURLException {
    URL url = new TomcatParseBpmPlatformXmlStep().checkValidBpmPlatformXmlResourceLocation(BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION);
    assertEquals(new File(BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION).toURI().toURL(), url);
  }

  @Test
  public void getBpmPlatformXmlLocationFromJndi() throws NamingException, MalformedURLException {
    Context context = new InitialContext();
    context.bind("java:comp/env/" + BPM_PLATFORM_XML_LOCATION, BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION);

    URL url = new TomcatParseBpmPlatformXmlStep().lookupBpmPlatformXmlLocationFromJndi();

    assertEquals(new File(BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION).toURI().toURL(), url);
  }

  @Test
  public void bpmPlatformXmlLocationNotRegisteredInJndi() throws NamingException {
    URL url = new TomcatParseBpmPlatformXmlStep().lookupBpmPlatformXmlLocationFromJndi();
    assertNull(url);
  }

  @Test
  public void getBpmPlatformXmlFromEnvironmentVariableAsUrlLocation() {
    try {
      System.setProperty(BPM_PLATFORM_XML_SYSTEM_PROPERTY, BPM_PLATFORM_XML_LOCATION_URL_HTTP_PROTOCOL);

      URL url = new TomcatParseBpmPlatformXmlStep().lookupBpmPlatformXmlLocationFromEnvironmentVariable();

      assertEquals(BPM_PLATFORM_XML_LOCATION_URL_HTTP_PROTOCOL, url.toString());
    } finally {
      System.clearProperty(BPM_PLATFORM_XML_SYSTEM_PROPERTY);
    }
  }

  @Test
  public void getBpmPlatformXmlFromSystemPropertyAsFileLocation() throws MalformedURLException {
    try {
      System.setProperty(BPM_PLATFORM_XML_SYSTEM_PROPERTY, BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION);

      URL url = new TomcatParseBpmPlatformXmlStep().lookupBpmPlatformXmlLocationFromEnvironmentVariable();

      assertEquals(new File(BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION).toURI().toURL(), url);
    } finally {
      System.clearProperty(BPM_PLATFORM_XML_SYSTEM_PROPERTY);
    }
  }

  @Test
  public void getBpmPlatformXmlFromClasspath() {
    String classPathResourceLocation = BpmPlatformXmlLocationTest.class.getPackage().getName().replace(".", "/") + "/conf/" + BPM_PLATFORM_XML_FILE;

    URL url = new TomcatParseBpmPlatformXmlStep().lookupBpmPlatformXmlFromClassPath(classPathResourceLocation);
    assertNotNull("Url should point to a bpm-platform.xml file.", url);
  }

  @Test
  public void getBpmPlatformXmlFromCatalinaConfDirectory() throws MalformedURLException {
    System.setProperty(CATALINA_HOME, BPM_PLATFORM_XML_LOCATION_PARENT_DIR);

    try {
      URL url = new TomcatParseBpmPlatformXmlStep().lookupBpmPlatformXmlFromCatalinaConfDirectory();

      assertEquals(new File(BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION).toURI().toURL(), url);
    } finally {
      System.clearProperty(CATALINA_HOME);
    }
  }

  @Test
  public void lookupBpmPlatformXml() throws NamingException, MalformedURLException {
    Context context = new InitialContext();
    context.bind("java:comp/env/" + BPM_PLATFORM_XML_LOCATION, BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION);

    URL url = new TomcatParseBpmPlatformXmlStep().lookupBpmPlatformXml();

    assertEquals(new File(BPM_PLATFORM_XML_FILE_ABSOLUTE_LOCATION).toURI().toURL(), url);
  }
  
  private static String getBpmPlatformXmlLocationParentDir() {
    String baseDir = BpmPlatformXmlLocationTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    try {
      // replace escaped whitespaces in path
      baseDir = URLDecoder.decode(baseDir, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return baseDir
        + BpmPlatformXmlLocationTest.class.getPackage().getName().replace(".", File.separator);
  }

}
