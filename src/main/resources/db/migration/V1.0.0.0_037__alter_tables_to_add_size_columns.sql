ALTER TABLE `address_fulfillment`
CHANGE COLUMN `number` `number` VARCHAR(256) NULL;

ALTER TABLE `payment_method`
CHANGE COLUMN `bin` `bin` VARCHAR(256) NULL;

ALTER TABLE `order_fulfillment`
ADD COLUMN external_channel_id VARCHAR(256) NULL AFTER `external_purchase_id`;