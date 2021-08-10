update application_parameter set value=1 where code='ENABLED_SEND_TO_LIQUIDATION';

UPDATE `cancellation_code_reason` SET `reason` = 'Producto no disponible en el local',
`client_reason` = 'Producto no disponible en el local' WHERE (`code` = 'C05');


update order_status
set liquidationEnabled=1
where code in ('01');

update order_status
set liquidationEnabled=1
where code in ('02');

update order_status
set liquidationEnabled=1
where code in ('15','16');

update order_status
set liquidationEnabled=1
where code in ('31','33');

update order_status
set liquidationEnabled=1
where code in ('22','18','13');

update order_status
set liquidationEnabled=1
where code in ('19');

update order_status
set liquidationEnabled=1
where code in ('45');

update order_status
set liquidationEnabled=1
where code in ('11','12','32','34','35');


