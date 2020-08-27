ALTER TABLE `order_fulfillment`
ADD COLUMN IF NOT EXISTS `partial_change` DECIMAL(10,2) NULL AFTER `bridge_purchase_id`,
ADD COLUMN IF NOT EXISTS `partial` TINYINT(1) NULL AFTER `notes`;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN  IF NOT EXISTS `fractional_discount` DECIMAL(10,2) NULL AFTER `fractionated`,
ADD COLUMN  IF NOT EXISTS `edition_code_type` VARCHAR(12) NULL AFTER `fractional_discount`,
ADD COLUMN  IF NOT EXISTS `partial` TINYINT(1) NULL AFTER `fractional_discount`,
ADD COLUMN  IF NOT EXISTS `unit_price_w_discount` DECIMAL(10,2) NULL AFTER `partial`,
ADD COLUMN  IF NOT EXISTS `old_quantity` INT(11) NULL AFTER `unit_price_w_discount`,
ADD COLUMN  IF NOT EXISTS `modified_quantity` INT(11) NULL AFTER `old_quantity`,
ADD COLUMN  IF NOT EXISTS `modified_total_price` DECIMAL(10,2) NULL AFTER `modified_quantity`,
ADD COLUMN  IF NOT EXISTS `old_fractional_discount` DECIMAL(10,2) NULL AFTER `modified_total_price`;





