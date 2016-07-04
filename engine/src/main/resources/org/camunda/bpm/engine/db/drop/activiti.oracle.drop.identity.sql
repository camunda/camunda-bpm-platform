alter table ACT_ID_MEMBERSHIP
    drop CONSTRAINT ACT_FK_MEMB_GROUP;

alter table ACT_ID_MEMBERSHIP
    drop CONSTRAINT ACT_FK_MEMB_USER;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_FK_TENANT_MEMB;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_FK_TENANT_MEMB_USER;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_FK_TENANT_MEMB_GROUP;

drop index ACT_IDX_MEMB_GROUP;
drop index ACT_IDX_MEMB_USER;
drop index ACT_IDX_TENANT_MEMB;
drop index ACT_IDX_TENANT_MEMB_USER;
drop index ACT_IDX_TENANT_MEMB_GROUP;
drop index ACT_UNIQ_TENANT_MEMB_USER;
drop index ACT_UNIQ_TENANT_MEMB_GROUP;

drop table ACT_ID_TENANT_MEMBER;
drop table  ACT_ID_TENANT;
drop table  ACT_ID_INFO;
drop table  ACT_ID_MEMBERSHIP;
drop table  ACT_ID_GROUP;
drop table  ACT_ID_USER;
