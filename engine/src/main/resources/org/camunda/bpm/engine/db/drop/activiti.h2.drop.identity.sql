alter table ACT_ID_MEMBERSHIP
    drop constraint ACT_FK_MEMB_GROUP;

alter table ACT_ID_MEMBERSHIP
    drop constraint ACT_FK_MEMB_USER;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_FK_TENANT_MEMB;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_FK_TENANT_MEMB_USER;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_FK_TENANT_MEMB_GROUP;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_UNIQ_TENANT_MEMB_USER;

alter table ACT_ID_TENANT_MEMBER
    drop constraint ACT_UNIQ_TENANT_MEMB_GROUP;

drop table ACT_ID_TENANT_MEMBER if exists;
drop table ACT_ID_TENANT if exists;
drop table ACT_ID_INFO if exists;
drop table ACT_ID_GROUP if exists;
drop table ACT_ID_MEMBERSHIP if exists;
drop table ACT_ID_USER if exists;
