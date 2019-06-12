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
package org.camunda.bpm.engine.impl.db;

import java.util.List;

import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation.State;
import org.camunda.bpm.engine.impl.interceptor.Session;


/**
 * @author Daniel Meyer
 *
 */
public interface PersistenceSession extends Session {

  // Entity Operations /////////////////////////////////

  /**
   * <p>Attempts to perform the operations in order and returns a flush result.
   * The result indicates if there are operations that were not successful (via {@link FlushResult#getFailedOperations()}
   * and if some operations were not executed (via {@link FlushResult#getRemainingOperations()}.
   * The remaining operations must be a suffix of the parameter (e.g. for operations [a, b, c, d],
   * [c, d] is a valid list of remaining operations, [b, c] is not).
   *
   * <p>This method modifies the operation's state, i.e. {@link DbOperation#getState()} will
   * be updated by calling this method:
   *
   * <ul>
   * <li>Successful operations: {@link State#APPLIED}
   * <li>Failed operations: {@link State#FAILED_ERROR} or {@link State#FAILED_CONCURRENT_MODIFICATION}.
   * <li>Remaining operations: {@link State#NOT_APPLIED}
   * </ul>
   *
   * In addition, the number of affected rows and failure (if any) is updated in the operation.
   *
   * @throws Exception in case of an unexpected error that is unrelated to an operation result.
   *   The caller should rollback the transaction in this case
   */
  FlushResult executeDbOperations(List<DbOperation> operations);

  void flushOperations();

  List<?> selectList(String statement, Object parameter);

  <T extends DbEntity> T selectById(Class<T> type, String id);

  Object selectOne(String statement, Object parameter);

  void lock(String statement, Object parameter);

  int executeNonEmptyUpdateStmt(String updateStmt, Object parameter);

  void commit();

  void rollback();

  // Schema Operations /////////////////////////////////

  void dbSchemaCheckVersion();

  void dbSchemaCreate();

  void dbSchemaDrop();

  void dbSchemaPrune();

  void dbSchemaUpdate();

  List<String> getTableNamesPresent();

  // listeners //////////////////////////////////////////

  void addEntityLoadListener(EntityLoadListener listener);

}
