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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.runtime.ProcessElementInstance;

/**
 * @author Sebastian Menski
 */
public final class StringUtil {
  
  /**
   * Note: {@link String#length()} counts Unicode supplementary
   * characters twice, so for a String consisting only of those,
   * the limit is effectively MAX_LONG_STRING_LENGTH / 2
   */
  public static int DB_MAX_STRING_LENGTH = 666;

  /**
   * Checks whether a {@link String} seams to be an expression or not
   *
   * Note: In most cases you should check for composite expressions. See
   * {@link #isCompositeExpression(String, ExpressionManager)} for more information.
   *
   * @param text the text to check
   * @return true if the text seams to be an expression false otherwise
   */
  public static boolean isExpression(String text) {
    text = text.trim();
    return text.startsWith("${") || text.startsWith("#{");
  }

  /**
   * Checks whether a {@link String} seams to be a composite expression or not. In contrast to an eval expression
   * is the composite expression also allowed to consist of a combination of literal and eval expressions, e.g.,
   * "Welcome ${customer.name} to our site".
   *
   * Note: If you just want to allow eval expression, then the expression must always start with "#{" or "${".
   * Use {@link #isExpression(String)} to conduct these kind of checks.
   *
   */
  public static boolean isCompositeExpression(String text, ExpressionManager expressionManager) {
    return !expressionManager.createExpression(text).isLiteralText();
  }

  public static String[] split(String text, String regex) {
    if (text == null) {
      return null;
    }
    else if (regex == null) {
      return new String[] { text };
    }
    else {
      String[] result = text.split(regex);
      for (int i = 0; i < result.length; i++) {
        result[i] = result[i].trim();
      }
      return result;
    }
  }

  public static boolean hasAnySuffix(String text, String[] suffixes) {
    for (String suffix : suffixes) {
      if (text.endsWith(suffix)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Converts a byte array into a string using the current process engines default charset as
   * returned by {@link ProcessEngineConfigurationImpl#getDefaultCharset()}. The converted string
   * is empty if the provided byte array is empty or <code>null</code>.
   *
   * @param bytes the byte array
   * @return a string representing the bytes, empty String if byte array is <code>null</code>
   */
  public static String fromBytes(byte[] bytes) {
    EnsureUtil.ensureActiveCommandContext("StringUtil.fromBytes");
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    return fromBytes(bytes, processEngineConfiguration.getProcessEngine());
  }

  /**
   * Converts a byte array into a string using the current process engines default charset as
   * returned by {@link ProcessEngineConfigurationImpl#getDefaultCharset()}. The converted string
   * is empty if the provided byte array is empty or <code>null</code>.
   *
   * @param bytes the byte array
   * @param processEngine the process engine
   * @return a string representing the bytes, empty String if byte array is <code>null</code>
   */
  public static String fromBytes(byte[] bytes, ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    Charset charset = processEngineConfiguration.getDefaultCharset();
    return bytes != null ? new String(bytes, charset) : new String();
  }

  public static Reader readerFromBytes(byte[] bytes) {
    EnsureUtil.ensureActiveCommandContext("StringUtil.readerFromBytes");
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

    return new InputStreamReader(inputStream, processEngineConfiguration.getDefaultCharset());
  }

  public static Writer writerForStream(OutputStream outStream) {
    EnsureUtil.ensureActiveCommandContext("StringUtil.readerFromBytes");
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    return new OutputStreamWriter(outStream, processEngineConfiguration.getDefaultCharset());
  }


  /**
   * Gets the bytes from a string using the current process engine's default charset
   *
   * @param string the string to get the bytes form
   * @return the byte array
   */
  public static byte[] toByteArray(String string) {
    EnsureUtil.ensureActiveCommandContext("StringUtil.toByteArray");
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    return toByteArray(string, processEngineConfiguration.getProcessEngine());
  }

  /**
   * Gets the bytes from a string using the provided process engine's default charset
   *
   * @param string the string to get the bytes form
   * @param processEngine the process engine to use
   * @return the byte array
   */
  public static byte[] toByteArray(String string, ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    Charset charset = processEngineConfiguration.getDefaultCharset();
    return string.getBytes(charset);
  }
  
  /**
   * Trims the input to the {@link #DB_MAX_STRING_LENGTH maxium length allowed} for persistence with our default database schema 
   *
   * @param string the input that might be trimmed if maximum length is exceeded
   * @return the input, eventually trimmed to {@link #DB_MAX_STRING_LENGTH}
   */
  public static String trimToMaximumLengthAllowed(String string) {
    if (string != null && string.length() > DB_MAX_STRING_LENGTH) {
      return string.substring(0, DB_MAX_STRING_LENGTH);
    }
    return string;
  }

  public static String joinDbEntityIds(Collection<? extends DbEntity> dbEntities) {
    return join(new StringIterator<DbEntity>(dbEntities.iterator()) {
      public String next() {
        return iterator.next().getId();
      }
    });
  }

  public static String joinProcessElementInstanceIds(Collection<? extends ProcessElementInstance> processElementInstances) {
    final Iterator<? extends ProcessElementInstance> iterator = processElementInstances.iterator();
    return join(new StringIterator<ProcessElementInstance>(iterator) {
      public String next() {
        return iterator.next().getId();
      }
    });
  }

  /**
   * @param string the String to check.
   * @return a boolean <code>TRUE</code> if the String is not null and not empty. <code>FALSE</code> otherwise.
   */
  public static boolean hasText(String string) {
    return string != null && !string.isEmpty();
  }

  public static String join(Iterator<String> iterator) {
    StringBuilder builder = new StringBuilder();

    while (iterator.hasNext()) {
      builder.append(iterator.next());
      if (iterator.hasNext()) {
        builder.append(", ");
      }
    }

    return builder.toString();
  }

  public abstract static class StringIterator<T> implements Iterator<String> {

    protected Iterator<? extends T> iterator;

    public StringIterator(Iterator<? extends T> iterator) {
      this.iterator = iterator;
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public void remove() {
      iterator.remove();
    }
  }

}
