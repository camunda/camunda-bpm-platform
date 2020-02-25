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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.text.StringSubstitutor;
import org.camunda.bpm.engine.ProcessEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

/**
 * Container that handles a managed Spring Boot application that is started by
 * the distro's startup scripts
 */
public class SpringBootManagedContainer {

  private static final String BASE_TEST_APPLICATION_YML = "base-test-application.yml";
  private static final String CONFIGURATION_APPLICATION_YML = "configuration/application.yml";

  protected static final Logger log = LoggerFactory.getLogger(SpringBootManagedContainer.class.getName());

  protected String baseDirectory;
  protected String baseUrl;
  protected Class testClass;
  protected List<String> commands = new ArrayList<>();

  protected Thread shutdownThread;
  protected Process startupProcess;

  public SpringBootManagedContainer(String baseDirectory, Class testClass, String... commands) {
    this.baseUrl = "http://localhost:8080";
    this.baseDirectory = baseDirectory;
    this.testClass = testClass;
    this.commands.add(getScriptPath());
    if (commands != null && commands.length > 0) {
      Arrays.stream(commands).forEach(e -> this.commands.add(e));
    }
    createTestYml();
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
      new Thread(new ConsoleConsumer()).start();
      final Process proc = startupProcess;

      shutdownThread = new Thread(() -> {
        if (proc != null) {
          killProcess(proc, true);
        }
      });
      Runtime.getRuntime().addShutdownHook(shutdownThread);

      final long startupTimeoutSeconds = 20;
      long timeout = startupTimeoutSeconds * 1000;
      boolean serverAvailable = false;
      while (timeout > 0 && serverAvailable == false) {
        serverAvailable = isRunning();
        if (!serverAvailable) {
          Thread.sleep(100);
          timeout -= 100;
        }
      }
      if (!serverAvailable) {
        killProcess(startupProcess, false);
        throw new TimeoutException(String.format("Managed Spring Boot application was not started within [%d] s", startupTimeoutSeconds));
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
        if (!gracefullyTerminateProcess(startupProcess) || isRunning()) {
          if (!killProcess(startupProcess, false)) {
            throw new RuntimeException("Could not kill the application.");
          }
        }
        startupProcess = null;
      }
    } catch (final Exception e) {
      throw new RuntimeException("Could not stop managed Spring Boot application", e);
    }

    cleanupTestYml();
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  protected String getScriptPath() {
    return baseDirectory + "/start." + (isUnixLike() ? "sh" : "bat");
  }

  // ---------------------------
  // determine server status
  // ---------------------------

  protected boolean isRunning() {
    try {
      URLConnection conn = new URL(this.baseUrl).openConnection();
      HttpURLConnection hconn = (HttpURLConnection) conn;
      hconn.setAllowUserInteraction(false);
      hconn.setDoInput(true);
      hconn.setUseCaches(false);
      hconn.setDoOutput(false);
      hconn.setRequestMethod("OPTIONS");
      hconn.setRequestProperty("User-Agent", "Camunda-Managed-SpringBoot-Container/1.0");
      hconn.setRequestProperty("Accept", "text/plain");
      hconn.connect();
      processResponse(hconn);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  protected void processResponse(HttpURLConnection hconn) throws IOException {
    int httpResponseCode = hconn.getResponseCode();
    if (httpResponseCode >= 400 && httpResponseCode < 500) {
      throw new RuntimeException(String.format("Unable to connect to server, it failed with responseCode (%s) and responseMessage (%s).", httpResponseCode,
          hconn.getResponseMessage()));
    } else if (httpResponseCode >= 300) {
      throw new IllegalStateException(
          String.format("The server request failed with responseCode (%s) and responseMessage (%s).", httpResponseCode, hconn.getResponseMessage()));
    }
  }

  // ---------------------------
  // kill processes
  // ---------------------------

  protected static boolean gracefullyTerminateProcess(Process process) {
    try {
      if (isUnixLike()) {
        return new ProcessBuilder("kill", "-15", String.valueOf(unixLikeProcessId(process))).start().waitFor() == 0;
      } else {
        // neither of the following "graceful" options seem to work reliably so far

        // return Kernel32.INSTANCE.GenerateConsoleCtrlEvent(Wincon.CTRL_C_EVENT, windowsProcessId(process));
        // return Kernel32.INSTANCE.GenerateConsoleCtrlEvent(Wincon.CTRL_BREAK_EVENT, windowsProcessId(process));
        // return new ProcessBuilder("taskkill", "/T", "/pid", String.valueOf(windowsProcessId(process))).start().waitFor() == 0;

        // therefore we are going to use "force"
        return new ProcessBuilder("taskkill", "/F", "/T", "/pid", String.valueOf(windowsProcessId(process))).start().waitFor() == 0;
      }
    } catch (Exception e) {
      log.error(String.format("Couldn't gracefully terminate process %s", process), e);
      return false;
    }
  }

  protected static boolean killProcess(Process process, boolean failOnException) {
    try {
      Process p = null;
      Integer pid = null;
      if (isUnixLike()) {
        pid = unixLikeProcessId(process);
        p = new ProcessBuilder("kill", "-9", String.valueOf(pid)).start();
      } else {
        pid = windowsProcessId(process);
        p = new ProcessBuilder("taskkill", "/F", "/T", "/pid", String.valueOf(pid)).start();
      }
      int exitCode = p.waitFor();
      if (exitCode != 0) {
        log.warn("Attempt to terminate process with pid {} returned with exit code {}", pid, exitCode);
      }
      return exitCode == 0;
    } catch (Exception e) {
      String message = String.format("Couldn't kill process %s", process);
      if (failOnException) {
        throw new RuntimeException(message, e);
      } else {
        log.error(message, e);
      }
      return false;
    }
  }

  protected static boolean isUnixLike() {
    return !System.getProperty("os.name").startsWith("Windows", 0);
  }

  protected static Integer unixLikeProcessId(Process process) {
    Class<?> clazz = process.getClass();
    try {
      if (clazz.getName().equals("java.lang.UNIXProcess")) {
        Field pidField = clazz.getDeclaredField("pid");
        pidField.setAccessible(true);
        Object value = pidField.get(process);
        if (value instanceof Integer) {
          log.debug("Detected pid: {}", value);
          return (Integer) value;
        }
      }
    } catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException ex) {
      throw new RuntimeException("Cannot fetch unix pid!", ex);
    }
    return null;
  }

  protected static Integer windowsProcessId(Process process) {
    if (process.getClass().getName().equals("java.lang.Win32Process") || process.getClass().getName().equals("java.lang.ProcessImpl")) {
      /* determine the pid on windows plattforms */
      try {
        Field f = process.getClass().getDeclaredField("handle");
        f.setAccessible(true);
        long handl = f.getLong(process);

        Kernel32 kernel = Kernel32.INSTANCE;
        WinNT.HANDLE handle = new WinNT.HANDLE();
        handle.setPointer(Pointer.createConstant(handl));
        int ret = kernel.GetProcessId(handle);
        log.debug("Detected pid: {}", ret);
        return ret;
      } catch (Throwable ex) {
        throw new RuntimeException("Cannot fetch windows pid!", ex);
      }
    }
    return null;
  }

  private void createTestYml() {
    File baseDir = new File(baseDirectory);
    while (!baseDir.getName().equals("test-classes")) {
      baseDir = baseDir.getParentFile();
    }
    String dbDirPath = new File(baseDir, "db" + File.separator + testClass.getSimpleName()).getAbsolutePath();
    dbDirPath = dbDirPath.replace(File.separatorChar, '/');

    String baseYml = SpringBootManagedContainer.class.getClassLoader().getResource(BASE_TEST_APPLICATION_YML).getFile();
    File testYml = new File(new File(baseDirectory), CONFIGURATION_APPLICATION_YML);

    try (PrintWriter printWriter = new PrintWriter(new FileWriter(testYml, false))) {
      try (BufferedReader file = new BufferedReader(new FileReader(baseYml))) {
        StringBuffer inputBuffer = new StringBuffer();
        String line;
        while ((line = file.readLine()) != null) {
          if (line.contains("url: jdbc:h2:")) {
            Map<String, String> values = new HashMap<>();
            values.put("testclass", dbDirPath);
            StringSubstitutor sub = new StringSubstitutor(values, "%(", ")");
            line = sub.replace(line);
          }
          inputBuffer.append(line);
          inputBuffer.append("\r\n");
        }
        printWriter.write(inputBuffer.toString());
      } catch (FileNotFoundException e) {
        throw new ProcessEngineException("Could not locate test application.yml for test " + testClass, e);
      }
    } catch (IOException e) {
      throw new ProcessEngineException("Could not write to " + testYml.getAbsolutePath(), e);
    }
  }

  private void cleanupTestYml() {
    File testYml = new File(new File(baseDirectory), CONFIGURATION_APPLICATION_YML);
    testYml.delete();
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
