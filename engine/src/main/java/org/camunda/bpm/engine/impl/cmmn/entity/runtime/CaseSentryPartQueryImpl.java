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
package org.camunda.bpm.engine.impl.cmmn.entity.runtime;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.CaseSentryPart;
import org.camunda.bpm.engine.runtime.CaseSentryPartQuery;

/**
 * @author Roman Smirnov
 *
 */
public class CaseSentryPartQueryImpl extends AbstractQuery<CaseSentryPartQuery, CaseSentryPart> implements CaseSentryPartQuery {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String sentryId;
  protected String type;
  protected String sourceCaseExecutionId;
  protected String standardEvent;
  protected boolean satisfied;

  public CaseSentryPartQueryImpl() {
  }

  public CaseSentryPartQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public CaseSentryPartQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public CaseSentryPartQuery caseSentryPartId(String caseSentryPartId) {
    ensureNotNull(NotValidException.class, "caseSentryPartId", caseSentryPartId);
    this.id = caseSentryPartId;
    return this;
  }

  public CaseSentryPartQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull(NotValidException.class, "caseInstanceId", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public CaseSentryPartQuery caseExecutionId(String caseExecutionId) {
    ensureNotNull(NotValidException.class, "caseExecutionId", caseExecutionId);
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public CaseSentryPartQuery sentryId(String sentryId) {
    ensureNotNull(NotValidException.class, "sentryId", sentryId);
    this.sentryId = sentryId;
    return this;
  }

  public CaseSentryPartQuery type(String type) {
    ensureNotNull(NotValidException.class, "type", type);
    this.type = type;
    return this;
  }

  public CaseSentryPartQuery sourceCaseExecutionId(String sourceCaseExecutionId) {
    ensureNotNull(NotValidException.class, "sourceCaseExecutionId", sourceCaseExecutionId);
    this.sourceCaseExecutionId = sourceCaseExecutionId;
    return this;
  }

  public CaseSentryPartQuery standardEvent(String standardEvent) {
    ensureNotNull(NotValidException.class, "standardEvent", standardEvent);
    this.standardEvent = standardEvent;
    return this;
  }

  public CaseSentryPartQuery satisfied() {
    this.satisfied = true;
    return this;
  }

  // order by ///////////////////////////////////////////

  public CaseSentryPartQuery orderByCaseSentryId() {
    orderBy(CaseSentryPartQueryProperty.CASE_SENTRY_PART_ID);
    return this;
  }

  public CaseSentryPartQuery orderByCaseInstanceId() {
    orderBy(CaseSentryPartQueryProperty.CASE_INSTANCE_ID);
    return this;
  }

  public CaseSentryPartQuery orderByCaseExecutionId() {
    orderBy(CaseSentryPartQueryProperty.CASE_EXECUTION_ID);
    return this;
  }

  public CaseSentryPartQuery orderBySentryId() {
    orderBy(CaseSentryPartQueryProperty.SENTRY_ID);
    return this;
  }

  public CaseSentryPartQuery orderBySource() {
    orderBy(CaseSentryPartQueryProperty.SOURCE);
    return this;
  }

  // results ////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getCaseSentryPartManager()
      .findCaseSentryPartCountByQueryCriteria(this);
  }

  public List<CaseSentryPart> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    List<CaseSentryPart> result = commandContext
      .getCaseSentryPartManager()
      .findCaseSentryPartByQueryCriteria(this, page);

    return result;
  }

  // getters /////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getSentryId() {
    return sentryId;
  }

  public String getType() {
    return type;
  }

  public String getSourceCaseExecutionId() {
    return sourceCaseExecutionId;
  }

  public String getStandardEvent() {
    return standardEvent;
  }

  public boolean isSatisfied() {
    return satisfied;
  }

}
