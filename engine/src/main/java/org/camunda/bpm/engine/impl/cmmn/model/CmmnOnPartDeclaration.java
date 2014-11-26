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
package org.camunda.bpm.engine.impl.cmmn.model;

import java.io.Serializable;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnOnPartDeclaration implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String standardEvent;
  protected CmmnActivity source;
  protected CmmnSentryDeclaration sentry;

  public String getStandardEvent() {
    return standardEvent;
  }

  public void setStandardEvent(String standardEvent) {
    this.standardEvent = standardEvent;
  }

  public CmmnActivity getSource() {
    return source;
  }

  public void setSource(CmmnActivity source) {
    this.source = source;
  }

  public CmmnSentryDeclaration getSentry() {
    return sentry;
  }

  public void setSentry(CmmnSentryDeclaration sentry) {
    this.sentry = sentry;
  }

}
