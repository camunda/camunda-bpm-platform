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
package org.camunda.bpm.run.qa.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container that handles a managed Spring Boot application that is started by
 * the distro's startup scripts
 */
public class SpringBootManagedContainer {

  public static final String APPLICATION_YML_PATH = "configuration/default.yml";
  public static final String RESOURCES_PATH = "configuration/resources";
  public static final String USERLIB_PATH = "configuration/userlib";

  protected static final String BASE_TEST_APPLICATION_YML = "base-test-application.yml";
  protected static final String RUN_HOME_VARIABLE = "camunda.run.home";

  protected static final long RAMP_UP_SECONDS = 60;
  protected static final long RAMP_DOWN_SECONDS = 20;

  protected static final Logger log = LoggerFactory.getLogger(SpringBootManagedContainer.class.getName());

  protected String baseDirectory;
  protected String baseUrl;
  protected List<String> commands = new ArrayList<>();

  protected Thread shutdownThread;
  protected Process startupProcess;
  protected static long pid;

  protected List<File> configurationFiles = new ArrayList<>();

  public SpringBootManagedContainer(String... commands) {
    this.baseDirectory = getRunHome();
    this.baseUrl = "http://localhost:8080";
    this.commands.add(getScriptPath());
    this.commands.add("start");
    if (commands != null && commands.length > 0) {
      Arrays.stream(commands).forEach(e -> this.commands.add(e));
    }
    InputStream defaultYml = SpringBootManagedContainer.class.getClassLoader().getResourceAsStream(BASE_TEST_APPLICATION_YML);
    createConfigurationYml(APPLICATION_YML_PATH, defaultYml);
    Path resourcesPath = Paths.get(baseDirectory, RESOURCES_PATH);
    resourcesPath.toFile().mkdir();
  }

  /**
   * @return the home directory of Camunda Platform Run based on the
   *         "camunda.run.home" system property.
   */
  public static String getRunHome() {
    String runHomeDirectory = System.getProperty(RUN_HOME_VARIABLE);
    if (runHomeDirectory == null || runHomeDirectory.isEmpty()) {
      throw new RuntimeException("System property " + RUN_HOME_VARIABLE + " not set. This property must point "
          + "to the root directory of the run distribution to test.");
    }

    return Paths.get(runHomeDirectory).toAbsolutePath().toString();
  }

  public void start() {
    if (isRunning()) {
      throw new RuntimeException("The Spring Boot application is already running!");
    }

    try {
      // execute command
      final ProcessBuilder startupProcessBuilder = new ProcessBuilder(commands);
      startupProcessBuilder.redirectErrorStream(true);
      startupProcessBuilder.directory(new File(baseDirectory));
      log.info("Starting Spring Boot application with: " + startupProcessBuilder.command());
      startupProcess = startupProcessBuilder.start();
      pid = startupProcess.pid();
      new Thread(new ConsoleConsumer()).start();

      shutdownThread = new Thread(() -> {
        if (startupProcess != null) {
          killProcess( true);
        }
      });
      Runtime.getRuntime().addShutdownHook(shutdownThread);

      if (!isStarted(RAMP_UP_SECONDS * 1000)) {
        killProcess(false);
        throw new TimeoutException(String.format("Managed Spring Boot application was not started within [%d] s", RAMP_UP_SECONDS));
      }
    } catch (final Exception ex) {
      throw new RuntimeException("Could not start managed Spring Boot application!", ex);
    }
  }

  public void stop() {

    if (shutdownThread != null) {
      Runtime.getRuntime().removeShutdownHook(shutdownThread);
      shutdownThread = null;
    }
    try {
      if (startupProcess != null) {
        if (isRunning()) {
          killProcess(false);
          if (!isShutDown(RAMP_DOWN_SECONDS * 1000)) {
            throw new RuntimeException("Could not kill the application.");
          }
        }
        startupProcess = null;
      }
    } catch (final Exception e) {
      throw new RuntimeException("Could not stop managed Spring Boot application", e);
    }

    cleanup();
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  protected String getScriptPath() {
    return baseDirectory + "/internal/run." + (isUnixLike() ? "sh" : "bat");
  }

  // ---------------------------
  // determine server status
  // ---------------------------

  protected boolean isStarted(long millisToWait) throws InterruptedException {
    return waitForServerStatus(millisToWait, true);
  }

  protected boolean isShutDown(long millisToWait) throws InterruptedException {
    return waitForServerStatus(millisToWait, false);
  }

  protected boolean waitForServerStatus(long millisToWait, boolean shouldBeRunning) throws InterruptedException {
    boolean serverAvailable = !shouldBeRunning;
    long targetTime = System.currentTimeMillis() + millisToWait;
    while (System.currentTimeMillis() < targetTime && serverAvailable == !shouldBeRunning) {
      serverAvailable = isRunning();
      if (shouldBeRunning ^ serverAvailable) {
        Thread.sleep(100);
      }
    }
    return serverAvailable == shouldBeRunning;
  }

  protected boolean isRunning() {
    try {
      //There might not be a resource at the base url, but at this point we just want to know that the server is up.
      processOptionsRequests(this.baseUrl);
      return true;
    } catch (Exception e) {
        return false;
    }
  }

  protected void processOptionsRequests(String urlToCall) throws IOException {
    URLConnection conn = new URL(urlToCall).openConnection();
    HttpURLConnection hconn = (HttpURLConnection) conn;
    hconn.setAllowUserInteraction(false);
    hconn.setDoInput(true);
    hconn.setUseCaches(false);
    hconn.setDoOutput(false);
    hconn.setRequestMethod("OPTIONS");
    hconn.setRequestProperty("User-Agent", "Camunda-Managed-SpringBoot-Container/1.0");
    hconn.setRequestProperty("Accept", "text/plain");
    hconn.connect();
    hconn.disconnect();
  }

  // ---------------------------
  // kill processes
  // ---------------------------

  protected static void killProcess(boolean failOnException) {
    try {
      Process p = null;

      // must kill a hierachy of processes: the script process (which corresponds to the pid value)
      // and the Java process it has spawned
      if (isUnixLike()) {
        p = new ProcessBuilder("pkill", "-TERM", "-P", String.valueOf(pid)).start();
      } else {
        p = new ProcessBuilder("taskkill", "/F", "/T", "/pid", String.valueOf(pid)).start();
      }
      int exitCode = p.waitFor();
      if (exitCode != 0) {
        log.warn("Attempt to terminate process with pid {} returned with exit code {}", pid, exitCode);
      }
    } catch (Exception e) {
      String message = String.format("Couldn't kill process %s", pid);
      if (failOnException) {
        throw new RuntimeException(message, e);
      } else {
        log.error(message, e);
      }
    }
  }

  protected static boolean isUnixLike() {
    return !System.getProperty("os.name").startsWith("Windows", 0);
  }

  public void replaceConfigurationYml(String filePath, InputStream source) {
    try {
      Files.deleteIfExists(Paths.get(baseDirectory, filePath));
      createConfigurationYml(filePath, source);
    } catch (IOException e) {
      log.error("Could not replace " + filePath, e);
    }
  }

  public void createConfigurationYml(String filePath, InputStream source) {
    try {

      Path testYmlPath = Paths.get(baseDirectory, filePath);

      Files.copy(source, testYmlPath);
      configurationFiles.add(testYmlPath.toFile());
    } catch (IOException e) {
      log.error("Could not create " + filePath, e);
    }
  }

  private void cleanup() {
    // cleanup test YAML
    for (File configFile : configurationFiles) {
      configFile.delete();
    }

    // cleanup resources
    File resourcesDir = new File(new File(baseDirectory), RESOURCES_PATH);
    deleteDirectory(resourcesDir);
  }

  private void deleteDirectory(File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File f : files) {
        if (f.isDirectory()) {
          deleteDirectory(f);
        } else {
          f.delete();
        }
      }
    }
    directory.delete();
  }

  // ---------------------------
  // fetch console
  // ---------------------------
  /**
   * Runnable that consumes the output of the startupProcess. If nothing
   * consumes the output the AS will hang on some platforms
   */
  protected class ConsoleConsumer implements Runnable {

    @Override
    public void run() {

      final InputStream stream = startupProcess.getInputStream();
      final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      String line = null;
      try {
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      } catch (final IOException e) {
        // ignore
      }
    }
  }
}
