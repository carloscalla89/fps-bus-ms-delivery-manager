update service_type
set send_new_flow_enabled = 1
where code in ('INKATRACKER_LITE_AM_PM','INKATRACKER_LITE_CALL_AM_PM','INKATRACKER_LITE_CALL_EXP',
'INKATRACKER_LITE_CALL_PROG','INKATRACKER_LITE_CALL_RET','INKATRACKER_LITE_EXP','INKATRACKER_LITE_PROG',
'INKATRACKER_LITE_RAD','INKATRACKER_LITE_RET','INKATRACKER_LITE_CALL_RAD');
