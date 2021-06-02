ALTER TABLE `order_fulfillment`
ADD COLUMN `discountAppliedNoDP` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `promotionalDiscount` DECIMAL(10,2) DEFAULT 0.0;
