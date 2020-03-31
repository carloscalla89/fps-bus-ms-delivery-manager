DROP TABLE `order_fulfillment_cancelled`;

ALTER TABLE `order_process_status`
ADD COLUMN `cancellation_observation` VARCHAR(512) NULL AFTER `reprogrammed`;


