INSERT INTO `cancellation_code_reason` (`app_type`,`type`,`code`,`enabled`,`reason`,`client_reason`) VALUES
('DIGITAL','CANCELLED','CU01',1,'Cambié de opinión','Cambié de opinión')
,('DIGITAL','CANCELLED','CU02',1,'Conseguí el producto a un mejor precio','Conseguí el producto a un mejor precio')
,('DIGITAL','CANCELLED','CU03',1,'Me equivoqué de producto(s)','Me equivoqué de producto(s)')
,('DIGITAL','CANCELLED','CU04',1,'Se está demorando demasiado','Se está demorando demasiado')
,('DIGITAL','CANCELLED','CU05',1,'He tenido un inconveniente y no puedo esperar','He tenido un inconveniente y no puedo esperar')
,('DIGITAL','CANCELLED','CU06',1,'Otro motivo','Otro motivo');

INSERT INTO `cancellation_code_reason` (`app_type`,`type`,`code`,`enabled`,`reason`,`client_reason`) VALUES
('FARMADASHBOARD','CANCELLED','C30',1,'Cliente desiste o ausente','Cliente desiste o ausente')
,('FARMADASHBOARD','CANCELLED','C31',1,'Cliente sin receta o no paga','Cliente sin receta o no paga')
,('FARMADASHBOARD','CANCELLED','C32',1,'Cambios en el pedido (fecha, productos)','Cambios en el pedido (fecha, productos)')
,('FARMADASHBOARD','CANCELLED','C33',1,'Stock no disponible','Stock no disponible')
,('FARMADASHBOARD','CANCELLED','C34',1,'Problema con dirección o zona de reparto','Problema con dirección o zona de reparto')
,('FARMADASHBOARD','CANCELLED','C35',1,'Problemas con el producto o producto dañado','Problemas con el producto o producto dañado')
,('FARMADASHBOARD','CANCELLED','C36',1,'Problema con el precio o facturación','Problema con el precio o facturación')
,('FARMADASHBOARD','CANCELLED','C37',1,'Incidente en operación o delivery','Incidente en operación o delivery')
,('FARMADASHBOARD','CANCELLED','C38',1,'Fraude o mayorista','Fraude o mayorista')
,('FARMADASHBOARD','CANCELLED','C39',1,'Error en la toma de pedido','Error en la toma de pedido')
,('FARMADASHBOARD','CANCELLED','C40',1,'Incumplimiento del tiempo de entrega','Incumplimiento del tiempo de entrega')
,('FARMADASHBOARD','CANCELLED','C41',1,'Error en el ruteo de pedido','Error en el ruteo de pedido')
,('FARMADASHBOARD','CANCELLED','C42',1,'Proforma','Proforma')
,('FARMADASHBOARD','CANCELLED','C43',1,'Error o problemas en los sistemas','Error o problemas en los sistemas')
,('FARMADASHBOARD','CANCELLED','C44',1,'Prueba de sistemas','Prueba de sistemas')
,('FARMADASHBOARD','CANCELLED','EXP',1,'Cancelación desde el POS Unificado','Cancelación desde el POS Unificado')
,('FARMADASHBOARD','CANCELLED','EXD',1,'Cancelación por expiración de una orden RET','Cancelación por expiración de una orden RET')
,('FARMADASHBOARD','CANCELLED','DU',1,'Cancelación desde el el Delivery Unificado','Cancelación desde el Delivery Unificado');

insert into cancellation_code_reason values
('C38','UNIFIED_POS','CANCELLED','Fraude o mayorista','Fraude o mayorista',1),
('C30','UNIFIED_POS','CANCELLED','Cliente desiste o ausente','Cliente desiste o ausente',1),
('C31','UNIFIED_POS','CANCELLED','Cliente sin receta o no paga','Cliente sin receta o no paga',1),
('C39','UNIFIED_POS','CANCELLED','Error en la toma de pedido','Error en la toma de pedido',1),
('C33','UNIFIED_POS','CANCELLED','Stock no disponible','Stock no disponible',1),
('C36','UNIFIED_POS','CANCELLED','Problema con el precio o facturación','Problema con el precio o facturación',1),
('C34','UNIFIED_POS','CANCELLED','Problema con dirección o zona de reparto','Problema con dirección o zona de reparto',1),
('C44','UNIFIED_POS','CANCELLED','Prueba de sistemas','Prueba de sistemas',1),
('R05','UNIFIED_POS','REJECTED','Incumplimiento del tiempo de entrega','Incumplimiento del tiempo de entrega',1),
('R11','UNIFIED_POS','REJECTED','Cambio de Forma de Pago','Cambio de Forma de Pago',1),
('R14','UNIFIED_POS','REJECTED','Problemas con el producto o producto dañado','Problemas con el producto o producto dañado',1);
