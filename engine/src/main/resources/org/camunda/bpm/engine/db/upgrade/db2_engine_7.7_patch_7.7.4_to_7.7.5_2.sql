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

-- increase the field length https://app.camunda.com/jira/browse/CAM-8177 --
DROP INDEX ACT_UNIQ_AUTH_USER;
DROP INDEX ACT_UNIQ_AUTH_GROUP;

ALTER TABLE ACT_RU_AUTHORIZATION
DROP COLUMN UNI_RESOURCE_ID_;

ALTER TABLE ACT_RU_AUTHORIZATION 
  ALTER COLUMN RESOURCE_ID_ 
  SET DATA TYPE varchar(255);

CALL Sysproc.admin_cmd ('REORG TABLE ACT_RU_AUTHORIZATION');
set integrity for ACT_RU_AUTHORIZATION off;

ALTER TABLE ACT_RU_AUTHORIZATION
 ADD COLUMN UNI_RESOURCE_ID_ varchar (255) not null generated always as (case when "RESOURCE_ID_" is null then "ID_" else "RESOURCE_ID_" end);

set integrity for ACT_RU_AUTHORIZATION immediate checked force generated;

create unique index ACT_UNIQ_AUTH_USER on ACT_RU_AUTHORIZATION(TYPE_,UNI_USER_ID_,RESOURCE_TYPE_,UNI_RESOURCE_ID_);
create unique index ACT_UNIQ_AUTH_GROUP on ACT_RU_AUTHORIZATION(TYPE_,UNI_GROUP_ID_,RESOURCE_TYPE_,UNI_RESOURCE_ID_);
