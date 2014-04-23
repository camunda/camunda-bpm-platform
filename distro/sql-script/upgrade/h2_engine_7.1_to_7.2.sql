-- add deployment.lock row to property table --
INSERT INTO ACT_GE_PROPERTY
  VALUES ('deployment.lock', '0', 1);

-- add revision column to incident table --
ALTER TABLE ACT_RU_INCIDENT
  ADD REV_ INTEGER;

-- set initial incident revision to 1 --
UPDATE
  ACT_RU_INCIDENT
SET
  REV_ = 1;

-- set incident revision column to not null --
ALTER TABLE ACT_RU_INCIDENT
  ALTER COLUMN REV_ SET NOT NULL;
