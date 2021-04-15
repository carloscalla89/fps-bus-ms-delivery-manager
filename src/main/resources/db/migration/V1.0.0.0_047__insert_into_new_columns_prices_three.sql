ALTER TABLE `order_fulfillment`
ADD COLUMN `subTotalWithNoSpecificPaymentMethod` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment`
ADD COLUMN `totalWithNoSpecificPaymentMethod` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment`
ADD COLUMN `totalWithPaymentMethod` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment`
ADD COLUMN `paymentMethodCardType` VARCHAR(16) DEFAULT NULL;



ALTER TABLE `order_fulfillment_item`
ADD COLUMN `priceList` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `totalPriceList` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `priceAllPaymentMethod` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `totalPriceAllPaymentMethod` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `priceWithpaymentMethod` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `totalPriceWithpaymentMethod` DECIMAL(10,2) DEFAULT 0.0;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `crossOutPL` tinyint(1) DEFAULT 0;

ALTER TABLE `order_fulfillment_item`
ADD COLUMN `paymentMethodCardType` VARCHAR(16) DEFAULT NULL;