alter table ACT_RE_DECISION_DEF
    drop constraint ACT_FK_DEC_REQ;

drop index ACT_IDX_DEC_DEF_TENANT_ID;
drop index ACT_IDX_DEC_DEF_REQ_ID;
drop index ACT_IDX_DEC_REQ_DEF_TENANT_ID;

drop table ACT_RE_DECISION_DEF;
drop table ACT_RE_DECISION_REQ_DEF;

