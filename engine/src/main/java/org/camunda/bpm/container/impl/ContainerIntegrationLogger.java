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
package org.camunda.bpm.container.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;
import javax.naming.NamingException;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.jboss.vfs.VirtualFile;

/**
 * @author Daniel Meyer
 *
 */
public class ContainerIntegrationLogger extends ProcessEngineLogger {

  public ProcessEngineException couldNotInstantiateJobExecutorClass(Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "001",
        "Could not instantiate job executor class"),e);
  }

  public ProcessEngineException couldNotLoadJobExecutorClass(Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "002",
        "Could not load job executor class"),e);
  }

  public void exceptionWhileStopping(String serviceType, String serviceName, Throwable t) {
    logWarn(
        "003",
        "Exception while stopping {} '{}': {}", serviceType, serviceName, t.getMessage(), t);
  }

  public void debugRootPath(String urlPath) {
    logDebug(
        "004",
        "Rootpath is {}",
        urlPath);
  }

  public ProcessEngineException cannotDecodePathName(UnsupportedEncodingException e) {
    return new ProcessEngineException(exceptionMessage(
        "005",
        "Could not decode pathname using utf-8 decoder."), e);
  }

  public ProcessEngineException exceptionWhileScanning(String file, IOException e) {
    return new ProcessEngineException(exceptionMessage(
        "006",
        "IOException while scanning archive '{}'.", file), e);
  }

  public void debugDiscoveredResource(String resourcePath) {
    logDebug(
        "007", "Discovered resource {}", resourcePath);
  }

  public ProcessEngineException cannotOpenFileInputStream(String absolutePath, IOException e) {
    return new ProcessEngineException(exceptionMessage(
        "008",
        "Cannot not open file for reading: {}.", e.getMessage()), e);
  }

  public ProcessEngineException couldNotGetResource(String strippedPaResourceRootPath, ClassLoader cl, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "009",
        "Could not load resources at '{}' using classloaded '{}'", strippedPaResourceRootPath, cl), e);
  }

  public void cannotFindResourcesForPath(String resourceRootPath, ClassLoader classLoader) {
    logWarn(
        "010",
        "Could not find any resources for process archive resource root path '{}' using classloader '{}'.",
        resourceRootPath,
        classLoader);
  }

  public ProcessEngineException exceptionWhileGettingVirtualFolder(URL url, URISyntaxException e) {
    return new ProcessEngineException(exceptionMessage(
        "011",
        "Could not load virtual file for url '{}'", url), e);
  }

  public void cannotScanVfsRoot(VirtualFile processArchiveRoot, IOException e) {
    logWarn(
        "012",
        "Cannot scan process archive root {}", processArchiveRoot, e);
  }

  public void cannotReadInputStreamForFile(String resourceName, VirtualFile processArchiveRoot, IOException e) {
    logWarn(
        "013",
        "Could not read input stream of file '{}' from process archive '{}'.", resourceName, processArchiveRoot, e);
  }

  public ProcessEngineException exceptionWhileLoadingCpRoots(String strippedPaResourceRootPath, ClassLoader classLoader, IOException e) {
    return new ProcessEngineException(exceptionMessage(
        "014",
        "Could not load resources at '{}' using classloaded '{}'", strippedPaResourceRootPath, classLoader), e);
  }

  public ProcessEngineException unsuppoertedParameterType(Type parameterType) {
    return new ProcessEngineException(exceptionMessage(
        "015",
        "Unsupported parametertype {}", parameterType));
  }

  public void debugAutoCompleteUrl(String url) {
    logDebug(
        "016",
        "Autocompleting url : [{}]", url);
  }

  public void debugAutoCompletedUrl(String url) {
    logDebug(
        "017",
        "Autocompleted url : [{}]", url);
  }

  public void foundConfigJndi(String jndi, String string) {
    logInfo(
        "018",
        "Found Camunda Platform configuration in JNDI [{}] at {}", jndi, string);
  }

  public void debugExceptionWhileGettingConfigFromJndi(String jndi, NamingException e) {
    logDebug(
        "019",
        "Failed to look up Camunda Platform configuration in JNDI [{}]: {}", jndi, e);
  }

  public void foundConfigAtLocation(String logStatement, String string) {
    logInfo(
        "020",
        "Found Camunda Platform configuration through {}  at {} " , logStatement, string);
  }

  public void notCreatingPaDeployment(String name) {
    logInfo(
        "021",
        "Not creating a deployment for process archive '{}': no resources provided.", name);
  }

  public IllegalArgumentException illegalValueForResumePreviousByProperty(String string) {
    return new IllegalArgumentException(exceptionMessage(
        "022",
        string));
  }

  public void deploymentSummary(Collection<String> deploymentResourceNames, String deploymentName) {

    StringBuilder builder = new StringBuilder();
    builder.append("Deployment summary for process archive '" + deploymentName + "': \n");
    builder.append("\n");
    for (String resourceName : deploymentResourceNames) {
      builder.append("        " + resourceName);
      builder.append("\n");
    }

    logInfo(
        "023",
        builder.toString());

  }

  public void foundProcessesXmlFile(String string) {

    logInfo(
        "024", "Found processes.xml file at {}", string);

  }

  public void emptyProcessesXml() {
    logInfo(
        "025",
        "Detected empty processes.xml file, using default values");
  }

  public void noProcessesXmlForPa(String paName) {
    logInfo(
        "026",
        "No processes.xml file found in process application '{}'", paName);
  }

  public ProcessEngineException exceptionWhileReadingProcessesXml(String deploymentDescriptor, IOException e) {
    return new ProcessEngineException(exceptionMessage(
        "027",
        "Exception while reading {}", deploymentDescriptor), e);
  }

  public ProcessEngineException exceptionWhileInvokingPaLifecycleCallback(String methodName, String paName, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "028",
        "Exception while invoking {} on Process Application {}: {}", methodName, paName, e.getMessage()), e);
  }

  public void debugFoundPaLifecycleCallbackMethod(String methodName, String paName) {
    logDebug(
        "029",
        "Found Process Application lifecycle callback method {} in application {}", methodName, paName);
  }

  public void debugPaLifecycleMethodNotFound(String methodName, String paName) {
    logDebug(
        "030",
        "Process Application lifecycle callback method {} not found in application {}", methodName, paName);
  }

  public ProcessEngineException cannotLoadConfigurationClass(String className, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "031",
        "Failed to load configuration class '{}': {}", className, e.getMessage()), e);
  }

  public ProcessEngineException configurationClassHasWrongType(String className, Class<?> expectedType, ClassCastException e) {
    return new ProcessEngineException(exceptionMessage(
        "032",
        "Class '{}' has wrong type. Must extend {}", expectedType.getName()), e);
  }

  public void timeoutDuringShutdownOfThreadPool(int i, TimeUnit seconds) {
    logError(
        "033",
        "Timeout during shutdown of managed thread pool. The current running tasks could not end within {} {} after shutdown operation.",
        i, seconds);
  }

  public void interruptedWhileShuttingDownThreadPool(InterruptedException e) {
    logError(
        "034",
        "Interrupted while shutting down thread pool", e);
  }

  public ProcessEngineException cannotRegisterService(ObjectName serviceName, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "035",
        "Cannot register service {} with MBean Server: {}", serviceName, e.getMessage()), e);
  }

  public ProcessEngineException cannotComposeNameFor(String serviceName, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "036",
        "Cannot compose name for service {}: {}", serviceName, e.getMessage()), e);
  }

  public ProcessEngineException exceptionWhileUnregisteringService(String canonicalName, Throwable t) {
    return new ProcessEngineException(exceptionMessage(
        "037",
        "Exception while unregistering service {} with the MBeanServer: {}", canonicalName, t), t);
  }

  public ProcessEngineException unknownExceptionWhileParsingDeploymentDescriptor(Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "038",
        "Unknown exception while parsing deployment camunda descriptor: {}", e.getMessage()), e);
  }

  public ProcessEngineException cannotSetValueForProperty(String key, String canonicalName, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "039",
        "Cannot set property '{}' on instance of class '{}'", key, canonicalName), e);
  }

  public ProcessEngineException cannotFindSetterForProperty(String key, String canonicalName) {
    return new ProcessEngineException(exceptionMessage(
        "040",
        "Cannot find setter for property '{}' on class '{}'", key, canonicalName));
  }

  public void debugPerformOperationStep(String stepName) {
    logDebug(
        "041",
        "Performing deployment operation step '{}'", stepName);
  }

  public void debugSuccessfullyPerformedOperationStep(String stepName) {
    logDebug(
        "041",
        "Successfully performed deployment operation step '{}'", stepName);
  }

  public void exceptionWhileRollingBackOperation(Exception e) {
    logError(
        "042",
        "Exception while rolling back operation",
        e);
  }

  public ProcessEngineException exceptionWhilePerformingOperationStep(String opName, String stepName, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "043",
        "Exception while performing '{}' => '{}': {}", opName, stepName, e.getMessage()), e);
  }

  public void exceptionWhilePerformingOperationStep(String name, Exception e) {
    logError(
        "044",
        "Exception while performing '{}': {}", name, e.getMessage(), e);
  }

  public void debugRejectedExecutionException(RejectedExecutionException e) {
    logDebug(
        "045",
        "RejectedExecutionException while scheduling work", e);
  }

  public void foundTomcatDeploymentDescriptor(String bpmPlatformFileLocation, String fileLocation) {
    logInfo(
        "046",
        "Found Camunda Platform configuration in CATALINA_BASE/CATALINA_HOME conf directory [{}] at '{}'", bpmPlatformFileLocation, fileLocation);

  }

  public ProcessEngineException invalidDeploymentDescriptorLocation(String bpmPlatformFileLocation, MalformedURLException e) {
    throw new ProcessEngineException(exceptionMessage(
        "047",
        "'{} is not a valid Camunda Platform configuration resource location.", bpmPlatformFileLocation), e);
  }

  public void camundaBpmPlatformSuccessfullyStarted(String serverInfo) {
    logInfo(
        "048",
        "Camunda Platform sucessfully started at '{}'.", serverInfo);
  }

  public void camundaBpmPlatformStopped(String serverInfo) {
    logInfo(
        "049",
        "Camunda Platform stopped at '{}'", serverInfo);
  }

  public void paDeployed(String name) {
    logInfo(
        "050",
        "Process application {} successfully deployed", name);
  }

  public void paUndeployed(String name) {
    logInfo(
        "051",
        "Process application {} undeployed", name);
  }

}
