--
-- Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
-- under one or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information regarding copyright
-- ownership. Camunda licenses this file to you under the Apache License,
-- Version 2.0; you may not use this file except in compliance with the
-- License. You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- https://app.camunda.com/jira/browse/CAM-9498
ALTER TABLE ACT_RE_DEPLOYMENT
  MODIFY COLUMN DEPLOY_TIME_ datetime;

ALTER TABLE ACT_RU_JOB
  MODIFY COLUMN LOCK_EXP_TIME_ datetime NULL,
  MODIFY COLUMN DUEDATE_ datetime NULL;

ALTER TABLE ACT_RU_TASK
  MODIFY COLUMN CREATE_TIME_ datetime;

ALTER TABLE ACT_RU_EVENT_SUBSCR
  MODIFY COLUMN CREATED_ datetime NOT NULL;

ALTER TABLE ACT_RU_INCIDENT
  MODIFY COLUMN INCIDENT_TIMESTAMP_ datetime NOT NULL;

ALTER TABLE ACT_RU_METER_LOG
  MODIFY COLUMN TIMESTAMP_ datetime;

ALTER TABLE ACT_RU_EXT_TASK
  MODIFY COLUMN LOCK_EXP_TIME_ datetime NULL;

ALTER TABLE ACT_HI_JOB_LOG
  MODIFY COLUMN TIMESTAMP_ datetime NOT NULL,
  MODIFY COLUMN JOB_DUEDATE_ datetime NULL;

ALTER TABLE ACT_ID_USER
  MODIFY COLUMN LOCK_EXP_TIME_ datetime NULL;

-- https://app.camunda.com/jira/browse/CAM-9920
ALTER TABLE ACT_HI_OP_LOG
  ADD COLUMN CATEGORY_ varchar(64);
  
ALTER TABLE ACT_HI_OP_LOG
  ADD COLUMN EXTERNAL_TASK_ID_ varchar(64);
