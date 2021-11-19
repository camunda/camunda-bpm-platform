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
package org.camunda.bpm.engine.rest.mapper;

import java.text.SimpleDateFormat;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.engine.rest.hal.Hal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Provider
@Produces({MediaType.APPLICATION_JSON, Hal.APPLICATION_HAL_JSON})
public class JacksonConfigurator implements ContextResolver<ObjectMapper> {

  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  public static String dateFormatString = DEFAULT_DATE_FORMAT;

  public static ObjectMapper configureObjectMapper(ObjectMapper mapper) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    mapper.setDateFormat(dateFormat);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    
    mapper.registerModule(new JavaTimeModule());

    return mapper;
  }

  @Override
  public ObjectMapper getContext(Class<?> clazz) {
    return configureObjectMapper(new ObjectMapper());
  }

  public static void setDateFormatString(String dateFormatString) {
    JacksonConfigurator.dateFormatString = dateFormatString;
  }

}
