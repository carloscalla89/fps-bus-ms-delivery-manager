package com.inretailpharma.digital.deliverymanager.util.sql;

public class CustomSqlQuery {

  public static final StringBuilder BASIC_QUERY_GET_ORDERINFO = new StringBuilder()
          .append("select o.id as orderId, " +
                  " o.ecommerce_purchase_id as ecommerceId, " +
                  " o.source, " +
                  " o.scheduled_time as scheduledTime, " +
                  " os.type as statusName, " +
                  " os.code as statusCode, " +
                  " s.center_code as centerCode, " +
                  " s.company_code as companyCode, " +
                  " (select st.source_channel from service_type st where st.code=s.service_type_code) as serviceChannel, " +
                  " (select st.short_code from service_type st where st.code=s.service_type_code) as serviceTypeShortCode, " +
                  " c.first_name as firstName, " +
                  " c.document_number as documentNumber, " +
                  " c.last_name as lastName " +
                  " from order_fulfillment o " +
                  " inner join client_fulfillment c on c.id = o.client_id " +
                  " inner join order_process_status s on o.id = s.order_fulfillment_id " +
                  " inner join order_status os on os.code = s.order_status_code ");

  public static final StringBuilder BASIC_QUERY_ORDER_BY_DATE_AND_ORDER_NUMBER_DESC = new StringBuilder()
          .append(" order by o.scheduled_time DESC, o.ecommerce_purchase_id DESC");

  public static final StringBuilder BASIC_QUERY_GET_ORDERINFO_COUNT = new StringBuilder()
          .append(" select max(o.id) " +
                  " from order_fulfillment o " +
                  " inner join client_fulfillment c on c.id = o.client_id " +
                  " inner join order_process_status s on o.id = s.order_fulfillment_id " +
                  " inner join order_status os on os.code = s.order_status_code ");
}
