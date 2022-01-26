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
package org.camunda.bpm.engine.impl.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.camunda.bpm.engine.ClassLoadingException;
import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.Problem;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.util.io.StreamSource;

/**
 * @author Daniel Meyer
 *
 */
public class EngineUtilLogger extends ProcessEngineLogger {


  public ProcessEngineException malformedUrlException(String url, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
        "001",
        "The URL '{}' is malformed", url), cause);
  }

  public ProcessEngineException multipleSourcesException(StreamSource source1, StreamSource source2) {
    return new ProcessEngineException(exceptionMessage(
        "002",
        "Multiple sources detected, which is invalid. Source 1: '{}', Source 2: {}", source1, source2));
  }

  public ProcessEngineException parsingFailureException(String name, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
        "003",
        "Could not parse '{}'. {}", name, cause.getMessage()), cause);
  }

  public void logParseWarnings(String formattedMessage) {
    logWarn(
        "004",
        "Warnings during parsing: {}", formattedMessage);
  }

  public ProcessEngineException exceptionDuringParsing(String string, String resourceName, List<Problem> errors, List<Problem> warnings) {
    return new ParseException(exceptionMessage(
        "005",
        "Could not parse BPMN process. Errors: {}",
        string),
        resourceName,
        errors,
        warnings);
  }

  public void unableToSetSchemaResource(Throwable cause) {
    logWarn(
        "006",
        "Setting schema resource failed because of: '{}'", cause.getMessage(), cause);
  }

  public ProcessEngineException invalidBitNumber(int bitNumber) {
    return new ProcessEngineException(exceptionMessage(
        "007",
        "Invalid bit {}. Only 8 bits are supported.", bitNumber));
  }

  public ProcessEngineException exceptionWhileInstantiatingClass(String className, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "008",
        "Exception while instantiating class '{}': {}", className, e.getMessage()), e);
  }

  public ProcessEngineException exceptionWhileApplyingFieldDeclatation(String declName, String className, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "009", "Exception while applying field declaration '{}' on class '{}': {}", declName, className, e.getMessage()), e);
  }

  public ProcessEngineException incompatibleTypeForFieldDeclaration(FieldDeclaration declaration, Object target, Field field) {
    return new ProcessEngineException(exceptionMessage(
        "010",
        "Incompatible type set on field declaration '{}' for class '{}'. Declared value has type '{}', while expecting '{}'" ,
        declaration.getName(),
        target.getClass().getName(),
        declaration.getValue().getClass().getName(),
        field.getType().getName()));
  }

  public ProcessEngineException exceptionWhileReadingStream(String inputStreamName, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "011",
        "Exception while reading {} as input stream: {}", inputStreamName, e.getMessage()), e);
  }

  public ProcessEngineException exceptionWhileReadingFile(String filePath, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "012",
        "Exception while reading file {}: {}", filePath, e.getMessage()), e);
  }

  public ProcessEngineException exceptionWhileGettingFile(String filePath, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "013",
        "Exception while getting file {}: {}", filePath, e.getMessage()), e);
  }

  public ProcessEngineException exceptionWhileWritingToFile(String filePath, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "014",
        "Exception while writing to file {}: {}", filePath, e.getMessage()), e);
  }

  public void debugCloseException(IOException ignore) {
    logDebug(
        "015",
        "Ignored exception on resource close", ignore);
  }

  public void debugClassLoading(String className, String classLoaderDescription, ClassLoader classLoader) {
    logDebug(
        "016",
        "Attempting to load class '{}' with {}: {}", className, classLoaderDescription, classLoader);
  }

  public ClassLoadingException classLoadingException(String className, Throwable throwable) {
    return new ClassLoadingException(exceptionMessage(
        "017",
        "Cannot load class '{}': {}", className, throwable.getMessage()), className, throwable);
  }

  public ProcessEngineException cannotConvertUrlToUri(URL url, URISyntaxException e) {
    return new ProcessEngineException(exceptionMessage(
        "018", "Cannot convert URL[{}] to URI: {}", url, e.getMessage()), e);
  }

  public ProcessEngineException exceptionWhileInvokingMethod(String methodName, Object target, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "019",
        "Exception while invoking method '{}' on object of type '{}': {}'", methodName, target, e.getMessage()), e);
  }

  public ProcessEngineException unableToAccessField(Field field, String name) {
    return new ProcessEngineException(exceptionMessage(
        "020",
        "Unable to access field {} on class {}, access protected", field, name));
  }

  public ProcessEngineException unableToAccessMethod(String methodName, String name) {
    return new ProcessEngineException(exceptionMessage(
        "021",
        "Unable to access method {} on class {}, access protected", methodName, name));
  }

  public ProcessEngineException exceptionWhileSettingField(Field field, Object object, Object value, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "022",
        "Exception while setting value '{}' to field '{}' on object of type '{}': {}",
        value, field, object.getClass().getName(), e.getMessage()), e);

  }

  public ProcessEngineException ambiguousSetterMethod(String setterName, String name) {
    return new ProcessEngineException(exceptionMessage(
        "023",
        "Ambiguous setter: more than one method named {} on class {}, with different parameter types.", setterName, name));
  }

  public NotFoundException cannotFindResource(String resourcePath) {
    return new NotFoundException(exceptionMessage(
        "024",
        "Unable to find resource at path {}", resourcePath));
  }

  public IllegalStateException notInsideCommandContext(String operation) {
    return new IllegalStateException(exceptionMessage(
        "025",
        "Operation {} requires active command context. No command context active on thread {}.", operation, Thread.currentThread()));
  }

  public ProcessEngineException exceptionWhileParsingCronExpresison(String duedateDescription, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "026",
        "Exception while parsing cron expression '{}': {}", duedateDescription, e.getMessage()), e);
  }

  public ProcessEngineException exceptionWhileResolvingDuedate(String duedate, Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "027",
        "Exception while resolving duedate '{}': {}", duedate, e.getMessage()), e);
  }

  public Exception cannotParseDuration(String expressions) {
    return new ProcessEngineException(exceptionMessage(
        "028",
        "Cannot parse duration '{}'.", expressions));
  }

  public void logParsingRetryIntervals(String intervals, Exception e) {
    logWarn(
        "029",
        "Exception while parsing retry intervals '{}'", intervals, e.getMessage(), e);
  }

  public void logJsonException(Exception e) {
    logDebug(
      "030",
      "Exception while parsing JSON: {}", e.getMessage(), e);
  }

  public void logAccessExternalSchemaNotSupported(Exception e) {
    logDebug(
        "031",
        "Could not restrict external schema access. "
        + "This indicates that this is not supported by your JAXP implementation: {}",
        e.getMessage());
  }

  public void logMissingPropertiesFile(String file) {
    logWarn("032", "Could not find the '{}' file on the classpath. " +
      "If you have removed it, please restore it.", file);
  }

  public ProcessEngineException exceptionDuringFormParsing(String cause, String resourceName) {
    return new ProcessEngineException(
        exceptionMessage("033", "Could not parse Camunda Form resource {}. Cause: {}", resourceName, cause));
  }

  public void debugCouldNotResolveCallableElement(
      String callingProcessDefinitionId,
      String activityId,
      Throwable cause) {
    logDebug("046", "Could not resolve a callable element for activity {} in process {}. Reason: {}",
        activityId,
        callingProcessDefinitionId,
        cause.getMessage());
  }

  public ProcessEngineException exceptionWhileSettingXxeProcessing(Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
        "047",
        "Exception while configuring XXE processing: {}", cause.getMessage()), cause);
  }

}
