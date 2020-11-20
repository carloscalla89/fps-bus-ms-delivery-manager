ALTER TABLE `order_process_status`
ADD COLUMN pickup_user_id VARCHAR(64) NULL,
ADD COLUMN pickup_full_name VARCHAR(128) NULL,
ADD COLUMN pickup_email VARCHAR(32) NULL,
ADD COLUMN pickup_document_type char(1) NULL,
ADD COLUMN pickup_document_number VARCHAR(24) NULL,
ADD COLUMN pickup_phone VARCHAR(16) NULL;