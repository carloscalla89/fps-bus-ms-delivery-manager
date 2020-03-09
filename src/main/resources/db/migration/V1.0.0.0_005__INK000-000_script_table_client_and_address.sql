--
-- Table structure for table `client_fulfillment`
--
DROP TABLE IF EXISTS `client_fulfillment`;
CREATE TABLE `client_fulfillment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(64) NULL,
  `last_name` VARCHAR(64) NULL,
  `email` VARCHAR(45) NULL,
  `document_number` VARCHAR(16) NULL,
  `phone` VARCHAR(16) NULL,
  `birth_date` DATE NULL,
  `inkaclub` TINYINT(1) NULL,
  `anonimous` TINYINT(1) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `address_fulfillment`
--
DROP TABLE IF EXISTS `address_fulfillment`;
CREATE TABLE `address_fulfillment` (
  `order_fulfillment_id` bigint(20) NOT NULL,
  `name` VARCHAR(45) NULL,
  `street` VARCHAR(45) NULL,
  `number` VARCHAR(45) NULL,
  `apartment` VARCHAR(45) NULL,
  `country` VARCHAR(45) NULL,
  `city` VARCHAR(45) NULL,
  `district` VARCHAR(45) NULL,
  `province` VARCHAR(45) NULL,
  `department` VARCHAR(45) NULL,
  `notes` VARCHAR(45) NULL,
  `latitude` DECIMAL(11,8) NULL,
  `longitude` DECIMAL(11,8) NULL,
  `postal_code` VARCHAR(16) NULL,
  PRIMARY KEY (`order_fulfillment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;;


ALTER TABLE `order_fulfillment`
DROP COLUMN `document_number`,
ADD COLUMN `client_id` BIGINT(20) NULL AFTER `delivery_cost`;



ALTER TABLE `order_fulfillment`
ADD INDEX `fk_order_fulfillment_1_idx` (`client_id` ASC);


ALTER TABLE `order_fulfillment`
ADD CONSTRAINT `fk_order_fulfillment_1`
  FOREIGN KEY (`client_id`)
  REFERENCES `client_fulfillment` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;



ALTER TABLE `address_fulfillment`
ADD CONSTRAINT `fk_address_fulfillment_1`
  FOREIGN KEY (`order_fulfillment_id`)
  REFERENCES `order_fulfillment` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
