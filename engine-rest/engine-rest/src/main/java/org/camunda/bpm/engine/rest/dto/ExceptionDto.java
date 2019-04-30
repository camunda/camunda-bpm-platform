/*
 * Copyright © 2012 - 2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.rest.dto;

import java.util.Objects;
import java.util.function.Function;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;

/**
 *
 * @author nico.rehwaldt
 */
public class ExceptionDto {

  protected String type;
  protected String message;
  
  private static ProcessEngineConfiguration processEngineConfiguration;

  private static Function<Throwable, String> getExceptionMessage = new Function<Throwable, String> () {
     public String apply(Throwable throwable) {
       return getProcessEngineConfiguration().getThrowableMessageHook().apply(throwable);
    }
  };

  public static void setProcessEngineConfiguration(ProcessEngineConfiguration config) {
    processEngineConfiguration = config;
  }

  private static ProcessEngineConfiguration getProcessEngineConfiguration() {
    if (processEngineConfiguration == null)
    {
      processEngineConfiguration = ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration();
    }
    return processEngineConfiguration;
  }



  public ExceptionDto() {

  }

  public String getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public static void setGetMessageHook(Function<Throwable, String> getExceptionMessage) {
    getExceptionMessage = Objects.requireNonNull(getExceptionMessage);
  }

  public static ExceptionDto fromException(Exception e) {
    ExceptionDto dto = new ExceptionDto();

    dto.type = e.getClass().getSimpleName();
    dto.message = getExceptionMessage.apply(e);

    return dto;
  }

  public static ExceptionDto fromException(Throwable e) {

    ExceptionDto dto = new ExceptionDto();

    dto.type = e.getClass().getSimpleName();
    dto.message = getExceptionMessage.apply(e);

    return dto;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setMessage(String message) {
    this.message = message;
  }
  
  
}
