INSERT INTO application_parameter (code, description, value)
values ('ENABLED_SEND_TO_LIQUIDATION', 'Flag para activar envío al nuevo componente de liquidation', 0);

ALTER TABLE `order_process_status`
ADD COLUMN `liquidationStatus` VARCHAR(32) DEFAULT NULL AFTER `order_status_code`;

ALTER TABLE `order_process_status`
ADD COLUMN `liquidationStatusDetail` VARCHAR(1024) DEFAULT NULL AFTER `liquidationStatus`;
