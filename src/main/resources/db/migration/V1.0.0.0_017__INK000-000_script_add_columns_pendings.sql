ALTER TABLE `client_fulfillment`
ADD COLUMN `notification_token` VARCHAR(300) NULL;

ALTER TABLE `order_fulfillment`
ADD COLUMN `discount_applied` DECIMAL(10,2) NULL,
ADD COLUMN `sub_total_cost` DECIMAL(10,2) NULL;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `eanCode` VARCHAR(15) NULL,
ADD COLUMN `presentation_id` INT(11) NULL,
ADD COLUMN `presentation_description` VARCHAR(90) NULL,
ADD COLUMN `quantity_units` INT(11) NULL,
ADD COLUMN `quantity_presentation` VARCHAR(90) NULL;