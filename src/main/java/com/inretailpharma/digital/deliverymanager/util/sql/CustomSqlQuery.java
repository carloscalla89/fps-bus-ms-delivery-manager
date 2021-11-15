package com.inretailpharma.digital.deliverymanager.util.sql;

public class CustomSqlQuery {

  public static final StringBuilder BASIC_QUERY_GET_ORDERINFO = new StringBuilder()
      .append("select o.id as orderId, o.ecommerce_purchase_id as ecommerceId, o.source,  "
              + "o.scheduled_time as scheduledTime, os.type as statusName, s.center_code as centerCode,  "
              + "s.company_code as companyCode, st.source_channel as serviceChannel, st.short_code as serviceTypeShortCode,  "
              + "c.first_name as firstName,  c.document_number as documentNumber,  c.last_name as lastName  "
              + "from order_fulfillment o  "
              + "inner join client_fulfillment c on c.id = o.client_id "
              + "inner join order_process_status s on o.id = s.order_fulfillment_id  "
              + "inner join order_status os on os.code = s.order_status_code  "
              + "inner join service_type st on st.code = s.service_type_code ");

}
