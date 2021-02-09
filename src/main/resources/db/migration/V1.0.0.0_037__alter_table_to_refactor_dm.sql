ALTER TABLE `address_fulfillment`
CHANGE COLUMN `number` `number` VARCHAR(256) NULL;

ALTER TABLE `payment_method`
CHANGE COLUMN `bin` `bin` VARCHAR(256) NULL;

ALTER TABLE `service_type`
ADD COLUMN `send_new_flow_enabled` tinyint(1) DEFAULT 0 AFTER `send_new_code_enabled`;

ALTER TABLE `order_process_status`
ADD COLUMN `date_created` DATETIME NULL DEFAULT NULL;

ALTER TABLE `order_process_status`
ADD COLUMN `date_last_updated` DATETIME NULL DEFAULT NULL;

ALTER TABLE `order_process_status`
ADD COLUMN `date_cancelled` DATETIME NULL DEFAULT NULL;

UPDATE  service_type
SET     send_new_flow_enabled = 1
WHERE  code in
('INKATRACKER_LITE_AM_PM', 'INKATRACKER_LITE_CALL_AM_PM', 'INKATRACKER_LITE_CALL_EXP','INKATRACKER_LITE_CALL_PROG',
'INKATRACKER_LITE_CALL_RAD','INKATRACKER_LITE_CALL_RET','INKATRACKER_LITE_EXP','INKATRACKER_LITE_PROG',
'INKATRACKER_LITE_RAD','INKATRACKER_LITE_RET');


UPDATE order_status
set type = 'ERROR_PICKED'
where code='04';

UPDATE order_status
set type = 'ERROR_PREPARED'
where code='05';

UPDATE order_status
set type = 'ERROR_READY_FOR_PICKUP'
where code='05';

UPDATE order_status
set type = 'ERROR_ASSIGNED'
where code='06';

UPDATE order_status
set type = 'ERROR_ON_ROUTED'
where code='07';

UPDATE order_status
set type = 'ERROR_ARRIVED'
where code='08';

UPDATE order_status
set type = 'ERROR_DELIVERED'
where code='09';

UPDATE order_status
set type = 'ERROR_CANCELLED'
where code='10';

