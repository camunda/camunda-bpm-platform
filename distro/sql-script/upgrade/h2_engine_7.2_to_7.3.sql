-- case management --

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD SUPER_EXEC_ varchar(64);

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD REQUIRED_ bit;

-- history --

ALTER TABLE ACT_HI_ACTINST
  ADD CALL_CASE_INST_ID_ varchar(64);

ALTER TABLE ACT_HI_PROCINST
  ADD SUPER_CASE_INSTANCE_ID_ varchar(64);

ALTER TABLE ACT_HI_CASEINST
  ADD SUPER_PROCESS_INSTANCE_ID_ varchar(64);

ALTER TABLE ACT_HI_CASEACTINST
  ADD REQUIRED_ bit;
  
-- engine --

ALTER TABLE ACT_RU_EXECUTION
  ADD TRANSITION_ID_ varchar(255)
