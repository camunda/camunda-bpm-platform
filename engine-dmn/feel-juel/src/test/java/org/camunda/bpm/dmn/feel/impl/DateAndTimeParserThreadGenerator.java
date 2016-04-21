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
package org.camunda.bpm.dmn.feel.impl;

import org.camunda.bpm.dmn.feel.impl.juel.FeelEngineLogger;
import org.camunda.bpm.dmn.feel.impl.juel.FeelLogger;
import org.camunda.bpm.dmn.feel.impl.juel.el.FeelFunctionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.LogManager;

/**
 * @author Stefan Hentschel.
 */
public class DateAndTimeParserThreadGenerator implements Callable<Date> {

  private String dateString;

  public DateAndTimeParserThreadGenerator(String dateString) {
    this.dateString = dateString;
  }

  @Override
  public Date call() throws Exception {
    return FeelFunctionMapper.parseDateAndTime(this.dateString);
  }
}
