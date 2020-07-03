ALTER TABLE `order_fulfillment`
ADD COLUMN `confirmed_order` DATETIME NULL DEFAULT NULL AFTER `scheduled_time`;
