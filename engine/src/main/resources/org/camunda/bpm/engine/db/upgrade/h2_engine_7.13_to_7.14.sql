--
-- Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
-- under one or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information regarding copyright
-- ownership. Camunda licenses this file to you under the Apache License,
-- Version 2.0; you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

insert into ACT_GE_SCHEMA_LOG
values ('300', CURRENT_TIMESTAMP, '7.14.0');

-- https://jira.camunda.com/browse/CAM-12304
ALTER TABLE ACT_RU_VARIABLE
  ADD BATCH_ID_ varchar(64);
CREATE INDEX ACT_IDX_BATCH_ID ON ACT_RU_VARIABLE(BATCH_ID_);
ALTER TABLE ACT_RU_VARIABLE
    ADD CONSTRAINT ACT_FK_VAR_BATCH
    FOREIGN KEY (BATCH_ID_)
    REFERENCES ACT_RU_BATCH (ID_);
    
-- https://jira.camunda.com/browse/CAM-12411
create index ACT_IDX_VARIABLE_TASK_NAME_TYPE on ACT_RU_VARIABLE(TASK_ID_, NAME_, TYPE_);