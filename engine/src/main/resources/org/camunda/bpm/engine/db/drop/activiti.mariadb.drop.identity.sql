alter table ACT_ID_MEMBERSHIP
    drop FOREIGN KEY ACT_FK_MEMB_GROUP;

alter table ACT_ID_MEMBERSHIP
    drop FOREIGN KEY ACT_FK_MEMB_USER;

alter table ACT_ID_TENANT_MEMBER
    drop FOREIGN KEY ACT_FK_TENANT_MEMB;

alter table ACT_ID_TENANT_MEMBER
    drop FOREIGN KEY ACT_FK_TENANT_MEMB_USER;

alter table ACT_ID_TENANT_MEMBER
    drop FOREIGN KEY ACT_FK_TENANT_MEMB_GROUP;

drop table if exists ACT_ID_TENANT_MEMBER;
drop table if exists ACT_ID_TENANT;
drop table if exists ACT_ID_INFO;
drop table if exists ACT_ID_MEMBERSHIP;
drop table if exists ACT_ID_GROUP;
drop table if exists ACT_ID_USER;
