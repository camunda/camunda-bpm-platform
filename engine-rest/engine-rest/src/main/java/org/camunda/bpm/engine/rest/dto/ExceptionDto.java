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
package org.camunda.bpm.engine.rest.dto;


/**
 *
 * @author nico.rehwaldt
 */
public class ExceptionDto {

  protected String type;
  protected String message;
  protected Integer code;

  public ExceptionDto() {

  }

  public String getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public static ExceptionDto fromException(Exception e) {

    ExceptionDto dto = new ExceptionDto();

    dto.type = e.getClass().getSimpleName();
    dto.message = e.getMessage();

    return dto;
  }

  public static ExceptionDto fromException(Throwable e) {

    ExceptionDto dto = new ExceptionDto();

    dto.type = e.getClass().getSimpleName();
    dto.message = e.getMessage();

    return dto;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

}
