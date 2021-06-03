ALTER TABLE `order_status`
CHANGE COLUMN `send_notification_enabled` `liquidationEnabled` TINYINT(1) NULL DEFAULT '0' ,
ADD COLUMN `liquidationStatus` VARCHAR(45) NULL AFTER `liquidationEnabled`;

INSERT INTO order_status(code,type,description) VALUES
('45','PARTIAL_UPDATE_ORDER','Orden que ha sido actualizada parcialmente'),
('46','ERROR_PARTIAL_UPDATE','Error al actualizar una orden parcial');