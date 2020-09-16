ALTER TABLE `order_fulfillment`
ADD COLUMN  `partial` TINYINT(1) NULL AFTER `notes`;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN  `fractional_discount` DECIMAL(10,2) NULL AFTER `fractionated`,
ADD COLUMN  `unit_price_w_discount` DECIMAL(10,2) NULL AFTER `fractional_discount`;







