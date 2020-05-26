ALTER TABLE `address_fulfillment`
MODIFY `name` varchar(256) DEFAULT NULL

ALTER TABLE `address_fulfillment`
MODIFY `notes` varchar(256) DEFAULT NULL;

ALTER TABLE `order_process_status`
MODIFY `status_detail` varchar(1024) DEFAULT NULL;