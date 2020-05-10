ALTER TABLE `order_process_status`
ADD COLUMN `cancellation_code` VARCHAR(40) NULL DEFAULT NULL AFTER `reprogrammed`;
