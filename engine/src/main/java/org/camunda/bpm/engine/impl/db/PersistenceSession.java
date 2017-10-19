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
package org.camunda.bpm.engine.impl.db;

import java.util.List;

import org.apache.ibatis.executor.BatchResult;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.Session;


/**
 * @author Daniel Meyer
 *
 */
public interface PersistenceSession extends Session {

  // Entity Operations /////////////////////////////////

  void executeDbOperation(DbOperation operation);

  List<?> selectList(String statement, Object parameter);

  <T extends DbEntity> T selectById(Class<T> type, String id);

  Object selectOne(String statement, Object parameter);

  void lock(String statement, Object parameter);

  int executeUpdate(String updateStatement, Object parameter);

  int executeNonEmptyUpdateStmt(String updateStmt, Object parameter);

  List<BatchResult> flushOperations();

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
