ALTER TABLE `cancellation_code_reason`
ADD COLUMN  IF NOT EXISTS `app_type` VARCHAR(32) NULL AFTER `code`,
ADD COLUMN  IF NOT EXISTS `client_reason` VARCHAR(500) NULL AFTER `reason`,
ADD COLUMN  IF NOT EXISTS `enabled` TINYINT(1) NULL;

ALTER TABLE `order_process_status`
ADD COLUMN IF NOT EXISTS  `cancellation_app_type` VARCHAR(32) NULL AFTER `cancellation_code`;

ALTER TABLE `cancellation_code_reason`
CHANGE COLUMN `app_type` `app_type` VARCHAR(32) NOT NULL DEFAULT 'DIGITAL' ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`code`, `app_type`);
