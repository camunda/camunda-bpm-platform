/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.dto;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author nico.rehwaldt
 */
public class ExceptionDto {

  private String type;
  private String message;

  private String stacktrace;

  public ExceptionDto() {

  }

  public String getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public static ExceptionDto fromException(Exception e, boolean includeStacktrace) {

    ExceptionDto dto = new ExceptionDto();

    dto.type = e.getClass().getSimpleName();
    dto.message = e.getMessage();

    if (includeStacktrace) {
      StringWriter writer = new StringWriter();

      e.printStackTrace(new PrintWriter(writer));

      dto.stacktrace = writer.toString();
    }

    return dto;
  }
}
