ALTER TABLE `order_fulfillment`
ADD COLUMN  `partial` TINYINT(1) NULL AFTER `notes`;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `edition_code_type` VARCHAR(12) NULL AFTER `fractional_discount`,
ADD COLUMN `unit_price_w_discount` DECIMAL(10,2);






