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
package org.camunda.bpm.engine.query;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.DurationReportResult;

/**
 * Describes basic methods for creating a report.
 *
 * @author Roman Smirnov
 *
 * @since 7.5
 */
public interface Report {

  /**
   * <p>Executes the duration report query and returns a list of
   * {@link DurationReportResult}s.</p>
   *
   * <p>Be aware that the resulting report must be interpreted by the
   * caller itself.</p>
   *
   * @param periodUnit A {@link PeriodUnit period unit} to define
   *          the granularity of the report.
   *
   * @return a list of {@link DurationReportResult}s
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ_HISTORY} permission
   *          on any {@link Resources#PROCESS_DEFINITION}.
   * @throws NotValidException
   *          When the given period unit is null.
   */
  List<DurationReportResult> duration(PeriodUnit periodUnit);

}


