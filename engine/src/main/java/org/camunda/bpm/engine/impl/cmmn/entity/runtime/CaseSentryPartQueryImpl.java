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

/**
 * This query is currently not public API on purpose.
 *
 * @author Roman Smirnov
 */
public class CaseSentryPartQueryImpl extends AbstractQuery<CaseSentryPartQueryImpl, CaseSentryPartEntity> {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String sentryId;
  protected String type;
  protected String sourceCaseExecutionId;
  protected String standardEvent;
  protected String variableEvent;
  protected String variableName;
  protected boolean satisfied;

  public CaseSentryPartQueryImpl() {
  }

  public CaseSentryPartQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public CaseSentryPartQueryImpl caseSentryPartId(String caseSentryPartId) {
    ensureNotNull(NotValidException.class, "caseSentryPartId", caseSentryPartId);
    this.id = caseSentryPartId;
    return this;
  }

  public CaseSentryPartQueryImpl caseInstanceId(String caseInstanceId) {
    ensureNotNull(NotValidException.class, "caseInstanceId", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public CaseSentryPartQueryImpl caseExecutionId(String caseExecutionId) {
    ensureNotNull(NotValidException.class, "caseExecutionId", caseExecutionId);
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public CaseSentryPartQueryImpl sentryId(String sentryId) {
    ensureNotNull(NotValidException.class, "sentryId", sentryId);
    this.sentryId = sentryId;
    return this;
  }

  public CaseSentryPartQueryImpl type(String type) {
    ensureNotNull(NotValidException.class, "type", type);
    this.type = type;
    return this;
  }

  public CaseSentryPartQueryImpl sourceCaseExecutionId(String sourceCaseExecutionId) {
    ensureNotNull(NotValidException.class, "sourceCaseExecutionId", sourceCaseExecutionId);
    this.sourceCaseExecutionId = sourceCaseExecutionId;
    return this;
  }

  public CaseSentryPartQueryImpl standardEvent(String standardEvent) {
    ensureNotNull(NotValidException.class, "standardEvent", standardEvent);
    this.standardEvent = standardEvent;
    return this;
  }

  public CaseSentryPartQueryImpl variableEvent(String variableEvent) {
    ensureNotNull(NotValidException.class, "variableEvent", variableEvent);
    this.variableEvent = variableEvent;
    return this;
  }

  public CaseSentryPartQueryImpl variableName(String variableName) {
    ensureNotNull(NotValidException.class, "variableName", variableName);
    this.variableName = variableName;
    return this;
  }

  public CaseSentryPartQueryImpl satisfied() {
    this.satisfied = true;
    return this;
  }

  // order by ///////////////////////////////////////////

  public CaseSentryPartQueryImpl orderByCaseSentryId() {
    orderBy(CaseSentryPartQueryProperty.CASE_SENTRY_PART_ID);
    return this;
  }

  public CaseSentryPartQueryImpl orderByCaseInstanceId() {
    orderBy(CaseSentryPartQueryProperty.CASE_INSTANCE_ID);
    return this;
  }

  public CaseSentryPartQueryImpl orderByCaseExecutionId() {
    orderBy(CaseSentryPartQueryProperty.CASE_EXECUTION_ID);
    return this;
  }

  public CaseSentryPartQueryImpl orderBySentryId() {
    orderBy(CaseSentryPartQueryProperty.SENTRY_ID);
    return this;
  }

  public CaseSentryPartQueryImpl orderBySource() {
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

  public List<CaseSentryPartEntity> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    List<CaseSentryPartEntity> result = commandContext
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

  public String getVariableEvent() {
    return variableEvent;
  }

  public String getVariableName() {
    return variableName;
  }

  public boolean isSatisfied() {
    return satisfied;
  }

}
