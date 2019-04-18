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

drop index ACT_IDX_HI_DEC_INST_ID on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_KEY on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_PI on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_CI on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_ACT on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_ACT_INST on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_TIME on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_TENANT_ID on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_ROOT_ID on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_REQ_ID on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_REQ_KEY on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_ROOT_PI on ACT_HI_DECINST;
drop index ACT_IDX_HI_DEC_INST_RM_TIME on ACT_HI_DECINST;

drop index ACT_IDX_HI_DEC_IN_INST on ACT_HI_DEC_IN;
drop index ACT_IDX_HI_DEC_IN_CLAUSE on ACT_HI_DEC_IN;
drop index ACT_IDX_HI_DEC_IN_ROOT_PI on ACT_HI_DEC_IN;
drop index ACT_IDX_HI_DEC_IN_RM_TIME on ACT_HI_DEC_IN;

drop index ACT_IDX_HI_DEC_OUT_INST on ACT_HI_DEC_OUT;
drop index ACT_IDX_HI_DEC_OUT_RULE on ACT_HI_DEC_OUT;
drop index ACT_IDX_HI_DEC_OUT_ROOT_PI on ACT_HI_DEC_OUT;
drop index ACT_IDX_HI_DEC_OUT_RM_TIME on ACT_HI_DEC_OUT;

drop table if exists ACT_HI_DECINST;

drop table if exists ACT_HI_DEC_IN;

drop table if exists ACT_HI_DEC_OUT;
