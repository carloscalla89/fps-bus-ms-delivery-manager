--
-- Table structure for table `order_fulfillment_cancel_reason`
--
DROP TABLE IF EXISTS `order_fulfillment_cancel_reason`;
CREATE TABLE `order_fulfillment_cancel_reason` (
  `code` varchar(40) NOT NULL,
  `type` varchar(50) NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;