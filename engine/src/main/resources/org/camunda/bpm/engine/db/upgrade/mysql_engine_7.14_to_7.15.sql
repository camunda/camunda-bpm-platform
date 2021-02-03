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
values ('400', CURRENT_TIMESTAMP, '7.15.0');

-- https://jira.camunda.com/browse/CAM-13013

create table ACT_RU_TASK_METER_LOG (
  ID_ varchar(64) not null,
  ASSIGNEE_HASH_ bigint,
  TIMESTAMP_ datetime,
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_TASK_METER_LOG_TIME on ACT_RU_TASK_METER_LOG(TIMESTAMP_);

-- https://jira.camunda.com/browse/CAM-13060
ALTER TABLE ACT_RU_INCIDENT
  ADD ANNOTATION_ varchar(4000);

ALTER TABLE ACT_HI_INCIDENT
  ADD ANNOTATION_ varchar(4000);
