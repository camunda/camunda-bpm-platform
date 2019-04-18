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

drop index ACT_UNIQ_TENANT_MEMB_USER;
drop index ACT_UNIQ_TENANT_MEMB_GROUP;

alter table ACT_ID_MEMBERSHIP
    drop foreign key ACT_FK_MEMB_GROUP;

alter table ACT_ID_MEMBERSHIP
    drop foreign key ACT_FK_MEMB_USER;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_FK_TENANT_MEMB;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_FK_TENANT_MEMB_USER;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_FK_TENANT_MEMB_GROUP;

drop table ACT_ID_TENANT_MEMBER;
drop table ACT_ID_TENANT;
drop table ACT_ID_INFO;
drop table ACT_ID_MEMBERSHIP;
drop table ACT_ID_GROUP;
drop table ACT_ID_USER;
