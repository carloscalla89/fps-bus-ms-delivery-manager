ALTER TABLE `address_fulfillment`
CHANGE COLUMN `number` `number` VARCHAR(256) NULL;

ALTER TABLE `payment_method`
CHANGE COLUMN `bin` `bin` VARCHAR(256) NULL;

ALTER TABLE `service_type`
ADD COLUMN `send_new_flow_enabled` tinyint(1) DEFAULT 0 AFTER `send_new_code_enabled`;

ALTER TABLE `order_proce    ss_status`
ADD COLUMN `date_created` DATETIME NULL DEFAULT NULL;

ALTER TABLE `order_process_status`
ADD COLUMN `date_last_updated` DATETIME NULL DEFAULT NULL;

ALTER TABLE `order_process_status`
ADD COLUMN `date_cancelled` DATETIME NULL DEFAULT NULL;

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

UPDATE order_status
set type = 'CANCELLED_ORDER'
where code='11';

UPDATE order_status
set type = 'DELIVERED_ORDER'
where code='12';

UPDATE order_status
set type = 'READY_PICKUP_ORDER'
where code='13';

UPDATE order_status
set type = 'CONFIRMED'
where code='15';

UPDATE order_status
set type = 'CONFIRMED_TRACKER'
where code='16';

UPDATE order_status
set type = 'ASSIGNED'
where code='17';

UPDATE order_status
set type = 'PICKED_ORDER'
where code='18';

UPDATE order_status
set type = 'PREPARED_ORDER'
where code='19';

UPDATE order_status
set type = 'ON_ROUTED_ORDER'
where code='20';

UPDATE order_status
set type = 'ARRIVED_ORDER'
where code='21';

UPDATE order_status
set type = 'CANCELLED_ORDER_NOT_ENOUGH_STOCK'
where code='31';

UPDATE order_status
set type = 'CANCELLED_ORDER_ONLINE_PAYMENT'
where code='32';

UPDATE order_status
set type = 'CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK'
where code='33';

UPDATE order_status
set type = 'REJECTED_ORDER'
where code='34';

UPDATE order_status
set type = 'REJECTED_ORDER_ONLINE_PAYMENT'
where code='35';

UPDATE order_status
set type = 'ORDER_FAILED'
where code='38';

UPDATE order_status
set type = 'INVOICED'
where code='40';

UPDATE order_status
set type = 'ERROR_INVOICED'
where code='41';