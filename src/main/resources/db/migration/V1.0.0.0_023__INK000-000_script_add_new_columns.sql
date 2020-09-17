ALTER TABLE client_fulfillment
ADD COLUMN user_id VARCHAR(128) NULL,
ADD COLUMN new_user_id VARCHAR(128) NULL;

ALTER TABLE `order_fulfillment`
CHANGE COLUMN `source` `source` VARCHAR(16) NULL;

ALTER TABLE `order_fulfillment_item`
CHANGE COLUMN `product_code` `product_code` VARCHAR(16) NULL;

ALTER TABLE order_fulfillment_item
ADD COLUMN fractionated_price DECIMAL(10,2) NULL,
ADD COLUMN quantity_unit_minimium bigint(20) NULL,
ADD COLUMN family_type VARCHAR(50) NULL;