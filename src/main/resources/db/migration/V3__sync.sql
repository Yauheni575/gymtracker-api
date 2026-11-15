CREATE SEQUENCE sync_version;

CREATE FUNCTION bump_version() RETURNS trigger AS $$
BEGIN
    NEW.version := nextval('sync_version');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

ALTER TABLE exercise ADD COLUMN uuid VARCHAR(36);
ALTER TABLE exercise ADD COLUMN version BIGINT;
ALTER TABLE exercise ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;
UPDATE exercise SET uuid = gen_random_uuid()::text, version = nextval('sync_version');
ALTER TABLE exercise ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE exercise ADD CONSTRAINT exercise_uuid UNIQUE (uuid);
ALTER TABLE exercise DROP CONSTRAINT exercise_user_name;
CREATE UNIQUE INDEX exercise_user_name_live ON exercise (user_id, name) WHERE NOT deleted;
CREATE INDEX exercise_user_version ON exercise (user_id, version);

ALTER TABLE entry ADD COLUMN uuid VARCHAR(36);
ALTER TABLE entry ADD COLUMN version BIGINT;
ALTER TABLE entry ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;
UPDATE entry SET uuid = gen_random_uuid()::text, version = nextval('sync_version');
ALTER TABLE entry ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE entry ADD CONSTRAINT entry_uuid UNIQUE (uuid);
CREATE INDEX entry_version ON entry (version);

ALTER TABLE food ADD COLUMN uuid VARCHAR(36);
ALTER TABLE food ADD COLUMN version BIGINT;
ALTER TABLE food ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;
UPDATE food SET uuid = gen_random_uuid()::text, version = nextval('sync_version');
ALTER TABLE food ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE food ADD CONSTRAINT food_uuid UNIQUE (uuid);
CREATE INDEX food_user_version ON food (user_id, version);

ALTER TABLE body_weight ADD COLUMN version BIGINT;
ALTER TABLE body_weight ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;
UPDATE body_weight SET version = nextval('sync_version');
ALTER TABLE body_weight DROP CONSTRAINT body_weight_user_date;
CREATE UNIQUE INDEX body_weight_user_date_live ON body_weight (user_id, date) WHERE NOT deleted;
CREATE INDEX body_weight_user_version ON body_weight (user_id, version);

ALTER TABLE profile ADD COLUMN version BIGINT;
UPDATE profile SET version = nextval('sync_version');

CREATE TRIGGER exercise_version BEFORE INSERT OR UPDATE ON exercise FOR EACH ROW EXECUTE FUNCTION bump_version();
CREATE TRIGGER entry_version BEFORE INSERT OR UPDATE ON entry FOR EACH ROW EXECUTE FUNCTION bump_version();
CREATE TRIGGER food_version BEFORE INSERT OR UPDATE ON food FOR EACH ROW EXECUTE FUNCTION bump_version();
CREATE TRIGGER body_weight_version BEFORE INSERT OR UPDATE ON body_weight FOR EACH ROW EXECUTE FUNCTION bump_version();
CREATE TRIGGER profile_version BEFORE INSERT OR UPDATE ON profile FOR EACH ROW EXECUTE FUNCTION bump_version();

CREATE FUNCTION delete_exercise_entries() RETURNS trigger AS $$
BEGIN
    UPDATE entry SET deleted = true WHERE exercise_id = NEW.id AND NOT deleted;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER exercise_deleted AFTER UPDATE OF deleted ON exercise
    FOR EACH ROW WHEN (NEW.deleted AND NOT OLD.deleted) EXECUTE FUNCTION delete_exercise_entries();
