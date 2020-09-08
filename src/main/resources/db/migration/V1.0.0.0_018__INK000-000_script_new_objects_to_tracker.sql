ALTER TABLE `order_process_status`
    DROP FOREIGN KEY `service_local_order_ibfk_2`;
ALTER TABLE `order_process_status`
    DROP INDEX `service_local_order_ibfk_2` ;
;