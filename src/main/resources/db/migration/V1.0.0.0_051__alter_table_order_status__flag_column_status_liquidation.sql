ALTER TABLE `order_status`
CHANGE COLUMN `send_notification_enabled` `liquidationEnabled` TINYINT(1) NULL DEFAULT '0' ,
ADD COLUMN `liquidationStatus` VARCHAR(45) NULL AFTER `liquidationEnabled`;