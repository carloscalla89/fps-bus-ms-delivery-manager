ALTER TABLE `payment_method`
ADD COLUMN `provider_card_commercial_code` VARCHAR(64) NULL DEFAULT NULL;

ALTER TABLE `payment_method`
ADD COLUMN `num_pan_visanet` VARCHAR(64) NULL DEFAULT NULL;

ALTER TABLE `order_process_status`
ADD COLUMN `transaction_date_visanet` DATETIME NULL DEFAULT NULL;
