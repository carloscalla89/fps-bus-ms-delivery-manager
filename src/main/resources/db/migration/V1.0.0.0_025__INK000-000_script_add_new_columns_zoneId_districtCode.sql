ALTER TABLE `order_process_status`
ADD COLUMN district_code_billing VARCHAR(32) NULL,
ADD COLUMN zone_id_billing bigint(20) NULL;