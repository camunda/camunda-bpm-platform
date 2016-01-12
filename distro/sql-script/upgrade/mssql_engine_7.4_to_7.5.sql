-- create missing foreign key contraint on ACT_RU_EXECUTION
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION (ID_);
    
-- tenant id --

ALTER TABLE ACT_RE_DEPLOYMENT 
  ADD TENANT_ID_ nvarchar(255);
 
create index ACT_IDX_DEPLOYMENT_TENANT_ID on ACT_RE_DEPLOYMENT(TENANT_ID_);
