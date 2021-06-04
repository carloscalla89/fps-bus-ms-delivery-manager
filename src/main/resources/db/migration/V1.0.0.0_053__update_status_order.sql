INSERT INTO application_parameter (code, description, value)
values ('ENABLED_SEND_TO_LIQUIDATION', 'Flag para activar envío al nuevo componente de liquidation', 0);


INSERT INTO order_status(code,type,description) VALUES
('45','PARTIAL_UPDATE_ORDER','Orden que ha sido actualizada parcialmente'),
('46','ERROR_PARTIAL_UPDATE','Error al actualizar una orden parcial'),
('22','CHECKOUT_ORDER','Orden que ha sido actualiza a checkout correctamente'),
('42','ERROR_CHECKOUT','Error al actualizar a checkout una orden');

update order_status
set liquidationEnabled=0, liquidationCode='00', liquidationStatus='PENDING'
where code in ('15','16');

update order_status
set liquidationEnabled=0, liquidationCode='01', liquidationStatus='ERROR'
where code in ('01');

update order_status
set liquidationEnabled=0, liquidationCode='02', liquidationStatus='ERROR'
where code in ('02');

update order_status
set liquidationEnabled=0, liquidationCode='03', liquidationStatus='AUTOMATIC_CANCELLED'
where code in ('31','33');

update order_status
set liquidationEnabled=0, liquidationCode='04', liquidationStatus='IN_PROCESS'
where code in ('22','18','13');

update order_status
set liquidationEnabled=0, liquidationCode='05', liquidationStatus='BILLED'
where code in ('19');

update order_status
set liquidationEnabled=0, liquidationCode='05', liquidationStatus='BILLED'
where code in ('19');

update order_status
set liquidationEnabled=0, liquidationCode='06', liquidationStatus='PARTIAL_BILLED'
where code in ('45');

update order_status
set liquidationEnabled=0
where code in ('11','12','32','34','35');