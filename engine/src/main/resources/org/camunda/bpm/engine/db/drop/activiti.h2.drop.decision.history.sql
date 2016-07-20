drop index ACT_IDX_HI_DEC_INST_ID;
drop index ACT_IDX_HI_DEC_INST_KEY;
drop index ACT_IDX_HI_DEC_INST_PI;
drop index ACT_IDX_HI_DEC_INST_CI;
drop index ACT_IDX_HI_DEC_INST_ACT;
drop index ACT_IDX_HI_DEC_INST_ACT_INST;
drop index ACT_IDX_HI_DEC_INST_TIME;
drop index ACT_IDX_HI_DEC_INST_TENANT_ID;
drop index ACT_IDX_HI_DEC_INST_ROOT_ID;
drop index ACT_IDX_HI_DEC_INST_REQ_ID;
drop index ACT_IDX_HI_DEC_INST_REQ_KEY;

drop index ACT_IDX_HI_DEC_IN_INST;
drop index ACT_IDX_HI_DEC_IN_CLAUSE;

drop index ACT_IDX_HI_DEC_OUT_INST;
drop index ACT_IDX_HI_DEC_OUT_RULE;

drop table ACT_HI_DECINST if exists;

drop table ACT_HI_DEC_IN if exists;

drop table ACT_HI_DEC_OUT if exists;
