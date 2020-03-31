ALTER TABLE `service_type`
ADD COLUMN `source_channel` VARCHAR(32) NULL AFTER `type`;

ALTER TABLE `service_type`
ADD COLUMN `enabled` tinyint(1) DEFAULT NULL;