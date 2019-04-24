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
package org.camunda.bpm.engine.impl.db.entitymanager.cache;

/**
 *
 * @author Daniel Meyer
 *
 */
public enum DbEntityState {

  /** A transient object does not exist in the database and has been created by the current session.
   * It will be INSERTed to the database and marked {@link #PERSISTENT} with the next flush.
   */
  TRANSIENT,

  /** A persistent object has been loaded from the database by the current session.
   * At the next flush, the session will perform dirty checking and flush an update if the object's persistent state changed.
   * It will remain persistent after the next flush.
   */
  PERSISTENT,

  /** A persistent object which may exist in the database but which has not been loaded into the current session
   * form the database. A detached copy of the object has been modified offline and merged back into the session.
   * At the next flush an update with optimistic locking check will be performed and after that, the object will be marked
   * {@link #PERSISTENT}.
   */
  MERGED,

  /** A transient object which does not exist in the database but has been created and deleted in the current session.
   * It will not be flushed to the database and will be removed from the cache at the next flush. */
  DELETED_TRANSIENT,

  /** A persistent object which has been loaded into this session and will be deleted with the next flush.
   * After the flush it will be removed from the cache. */
  DELETED_PERSISTENT,

  /** A {@link #MERGED} object which may exists in the database and is set to be deleted by the current session.
   * It will be removed from the cache at the next flush. */
  DELETED_MERGED
}
