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
package org.camunda.bpm.engine.impl.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;

/**
 * @author Roman Smirnov
 *
 */
public class JobExceptionUtil {

  public static String getJobExceptionStacktrace(Throwable exception) {
    StringWriter stringWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  public static String getJobExceptionStacktrace(ByteArrayEntity byteArray) {
    String result = null;
    if(byteArray != null) {
      result = StringUtil.fromBytes(byteArray.getBytes());
    }
    return result;
  }

  public static ByteArrayEntity createJobExceptionByteArray(byte[] byteArray) {
    ByteArrayEntity result = null;

    if (byteArray != null) {
      result = new ByteArrayEntity("job.exceptionByteArray", byteArray);
      Context
        .getCommandContext()
        .getDbEntityManager()
        .insert(result);
    }

    return result;
  }

}
