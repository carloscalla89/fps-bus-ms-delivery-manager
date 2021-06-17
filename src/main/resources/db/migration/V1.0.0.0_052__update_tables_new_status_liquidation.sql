ALTER TABLE `order_process_status`
ADD COLUMN `liquidationStatus` VARCHAR(32) DEFAULT NULL AFTER `order_status_code`;

ALTER TABLE `order_process_status`
ADD COLUMN `liquidationStatusDetail` VARCHAR(1024) DEFAULT NULL AFTER `liquidationStatus`;

ALTER TABLE `order_status`
CHANGE COLUMN `send_notification_enabled` `liquidationEnabled` TINYINT(1) NULL DEFAULT '0' ,
ADD COLUMN `liquidationCode` VARCHAR(8) NULL AFTER `liquidationEnabled`,
ADD COLUMN `liquidationStatus` VARCHAR(45) NULL AFTER `liquidationCode`;
