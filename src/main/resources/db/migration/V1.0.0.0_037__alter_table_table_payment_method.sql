ALTER TABLE `address_fulfillment`
CHANGE COLUMN `number` `number` VARCHAR(256) NULL;

ALTER TABLE `payment_method`
CHANGE COLUMN `bin` `bin` VARCHAR(256) NULL;

ALTER TABLE `service_type`
ADD COLUMN `send_new_flow_enabled` tinyint(1) DEFAULT 0 AFTER `send_new_code_enabled`;