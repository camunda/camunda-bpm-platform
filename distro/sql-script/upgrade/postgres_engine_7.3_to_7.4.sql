-- metrics --

ALTER TABLE ACT_RU_METER_LOG
  ADD REPORTER_ varchar(255);
  
-- job prioritization --
  
ALTER TABLE ACT_RU_JOB
  ADD PRIORITY_ integer NOT NULL
  DEFAULT 0;
