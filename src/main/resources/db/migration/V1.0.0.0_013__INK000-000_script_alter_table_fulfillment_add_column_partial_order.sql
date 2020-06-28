ALTER TABLE `order_fulfillment`
ADD COLUMN `partial_change` DECIMAL(10,2) NULL AFTER `bridge_purchase_id`,
ADD COLUMN `partial` TINYINT(1) NULL AFTER `notes`;



ALTER TABLE `order_fulfillment_item`
ADD COLUMN `fractional_discount` DECIMAL(10,2) NULL AFTER `fractionated`,
ADD COLUMN `edition_code_type` VARCHAR(12) NULL AFTER `partial`;
ADD COLUMN `partial` TINYINT(1) NULL AFTER `fractional_discount`,
ADD COLUMN `unit_price_w_discount` DECIMAL(10,2) NULL AFTER `partial`,
ADD COLUMN `old_quantity` INT(11) NULL AFTER `unit_price_w_discount`,
ADD COLUMN `modified_quantity` INT(11) NULL AFTER `old_quantity`,
ADD COLUMN `modified_total_price` DECIMAL(10,2) NULL AFTER `modified_quantity`,
ADD COLUMN `old_fractional_discount` DECIMAL(10,2) NULL AFTER `modified_total_price`;





