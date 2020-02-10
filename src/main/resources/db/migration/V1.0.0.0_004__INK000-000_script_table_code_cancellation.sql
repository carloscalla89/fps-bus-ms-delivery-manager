--
-- Table structure for table `order_fulfillment_cancel_reason`
--
DROP TABLE IF EXISTS `cancellation_code_reason`;
CREATE TABLE `cancellation_code_reason` (
  `code` varchar(40) NOT NULL,
  `type` varchar(50) NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `order_fulfillment_cancel_reason`
--
DROP TABLE IF EXISTS `order_fulfillment_cancelled`;
CREATE TABLE `order_fulfillment_cancelled` (
  `order_fulfillment_id` bigint(20) NOT NULL,
  `code_cancellation` varchar(40) NOT NULL,
  `observation` varchar(512) NOT NULL,
  KEY `order_fulfillment_id` (`order_fulfillment_id`),
  CONSTRAINT `order_fulfillment_id_ibfk_1` FOREIGN KEY (`order_fulfillment_id`) REFERENCES `order_fulfillment` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `code_cancellation_ibfk_1` FOREIGN KEY (`code_cancellation`) REFERENCES `cancellation_code_reason` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

