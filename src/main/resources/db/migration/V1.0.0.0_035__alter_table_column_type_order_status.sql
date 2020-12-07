ALTER TABLE `order_process_status`
CHANGE COLUMN `cancellation_app_type` `cancellation_app_type` VARCHAR(64) NULL;

ALTER TABLE `order_status`
CHANGE COLUMN `type` `type` VARCHAR(64) NULL;



