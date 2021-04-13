ALTER TABLE `service_type`
ADD COLUMN `class_implement` VARCHAR(64) NULL AFTER `code`;

UPDATE service_type
set class_implement = 'inkatracker'
where code='INKATRACKER_AM_PM';

UPDATE service_type
set class_implement = 'inkatracker'
where code='INKATRACKER_CALL';

UPDATE service_type
set class_implement = 'inkatracker'
where code='INKATRACKER_EXP';

UPDATE service_type
set class_implement = 'inkatracker'
where code='INKATRACKER_PROG';

UPDATE service_type
set class_implement = 'inkatracker'
where code='INKATRACKER_RAD';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_AM_PM';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_CALL_AM_PM';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_CALL_EXP';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_CALL_PROG';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_CALL_RAD';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_CALL_RET';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_RET';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_EXP';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_PROG';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_RAD';

UPDATE service_type
set class_implement = 'inkatrackerlite'
where code='INKATRACKER_LITE_RAD';
