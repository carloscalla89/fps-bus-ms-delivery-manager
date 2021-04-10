ALTER TABLE `payment_method`
ADD COLUMN `provider_card_commercial_code` VARCHAR(64) NULL DEFAULT NULL;

ALTER TABLE `payment_method`
ADD COLUMN `num_pan_visanet` VARCHAR(64) NULL DEFAULT NULL;

ALTER TABLE `payment_method`
ADD COLUMN `transaction_date_visanet` DATETIME NULL DEFAULT NULL;

ALTER TABLE `service_type`
ADD COLUMN `send_notification_enabled` tinyint(1) DEFAULT 0 AFTER `send_new_flow_enabled`;

ALTER TABLE `order_status`
ADD COLUMN `send_notification_enabled` tinyint(1) DEFAULT 0;



