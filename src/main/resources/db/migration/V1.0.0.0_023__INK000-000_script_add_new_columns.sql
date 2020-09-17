ALTER TABLE client_fulfillment
ADD COLUMN user_id VARCHAR(128) NULL,
ADD COLUMN new_user_id VARCHAR(128) NULL;

ALTER TABLE order_fulfillment
ADD COLUMN confirmed_insink_order datetime NULL;

ALTER TABLE `order_fulfillment`
CHANGE COLUMN `source` `source` VARCHAR(16) NULL;

ALTER TABLE `order_fulfillment_item`
CHANGE COLUMN `product_code` `product_code` VARCHAR(16) NULL;

ALTER TABLE order_fulfillment_item
ADD COLUMN fractionated_price DECIMAL(10,2) NULL,
ADD COLUMN quantity_unit_minimium bigint(20) NULL,
ADD COLUMN family_type VARCHAR(50) NULL;



INSERT INTO application_parameter VALUES
('ACTIVATED_DD_IKF','Flag para activar o desactivar si se adapta a la nueva arquitectura para inkafarma',0),
('ACTIVATED_DD_MF','Flag para activar o desactivar si se adapta a la nueva arquitectura para mifarma',0);

INSERT INTO service_type
values
('INKATRACKER_EXP','Inkatracker con delivery express','DELIVERY','DIGITAL','órdenes DC con delivery express',0),
('INKATRACKER_AM_PM','Inkatracker con delivery AM y PM','DELIVERY','DIGITAL','órdenes DC con delivery AM y PM',0),
('INKATRACKER_PROG','Inkatracker con delivery programado','DELIVERY','DIGITAL','órdenes DC con delivery programado',0),
('INKATRACKER_RET','Inkatracker RECOGO EN TIENDA','PICKUP','DIGITAL','órdenes DC con retiro en tienda',0),
('INKATRACKER_LITE_EXP','Inkatrackerlite con delivery express','DELIVERY','DIGITAL','órdenes de botica con delivery express',0),
('INKATRACKER_LITE_AM_PM','Inkatrackerlite con delivery AM y PM','DELIVERY','DIGITAL','órdenes botica con delivery AM y PM',0),
('INKATRACKER_LITE_PROG','Inkatrackerlite con delivery programado','DELIVERY','DIGITAL','órdenes botica con delivery programado',0),
('INKATRACKER_LITE_CALL_RAD','Inkatrackerlite con delivery de call center','DELIVERY','CALL','órdenes botica CALL center con delivery',0),
('INKATRACKER_LITE_CALL_EXP','Inkatrackerlite con delivery de call center','DELIVERY','CALL','órdenes botica CALL center con delivery',0),
('INKATRACKER_LITE_CALL_AM_PM','Inkatrackerlite con delivery de call center','DELIVERY','CALL','órdenes botica CALL center con delivery',0),
('INKATRACKER_LITE_CALL_PROG','Inkatrackerlite con delivery de call center','DELIVERY','CALL','órdenes botica CALL center con delivery',0),
('INKATRACKER_LITE_CALL_RET','Inkatrackerlite con delivery de call center','PICKUP','CALL','órdenes botica CALL center con delivery',0),
('TEMPORARY_EXP','Inkatracker TEMPORARY con delivery express','DELIVERY','DIGITAL','órdenes TEMPORARY con delivery express',0),
('TEMPORARY_AM_PM','Inkatracker TEMPORARY con delivery AM y PM','DELIVERY','DIGITAL','órdenes TEMPORARY con delivery AM y PM',0),
('TEMPORARY_PROG','Inkatracker TEMPORARY con delivery programado','DELIVERY','DIGITAL','órdenes DC con TEMPORARY programado',0),
('TEMPORARY_RET','Inkatracker TEMPORARY con recogo en tienda','PICKUP','DIGITAL','órdenes temporary retiro en tienda',0);