ALTER TABLE `client_fulfillment`
ADD COLUMN IF NOT EXISTS `notification_token` VARCHAR(300) NULL;

ALTER TABLE `order_fulfillment`
ADD COLUMN IF NOT EXISTS `discount_applied` DECIMAL(10,2) NULL,
ADD COLUMN IF NOT EXISTS `sub_total_cost` DECIMAL(10,2) NULL;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN IF NOT EXISTS `ean_code` VARCHAR(15) NULL,
ADD COLUMN IF NOT EXISTS `presentation_id` INT(11) NULL,
ADD COLUMN IF NOT EXISTS `presentation_description` VARCHAR(90) NULL,
ADD COLUMN IF NOT EXISTS `quantity_units` INT(11) NULL,
ADD COLUMN IF NOT EXISTS `quantity_presentation` VARCHAR(90) NULL;