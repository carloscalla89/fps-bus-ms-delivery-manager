package com.inretailpharma.digital.deliverymanager.repository.custom;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrderCanonicalFulfitment;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrderCanonicalResponse;
import com.inretailpharma.digital.deliverymanager.dto.RequestFilterDTO;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import com.inretailpharma.digital.deliverymanager.util.sql.CustomSqlQuery;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class CustomQueryOrderInfo {

  @Autowired
  EntityManager entityManager;

  private String getQueryOrderInfo(RequestFilterDTO requestFilter) {

    String basicQuery = CustomSqlQuery.BASIC_QUERY_GET_ORDERINFO.toString();
    if (requestFilter.getFilter() == null) {
      return basicQuery;
    } else {
      StringBuilder queryFilters = new StringBuilder();
      //queryFilters.append(basicQuery);
      queryFilters.append("where 1 = 1 ");

      if(requestFilter.getFilter().getMultipleField() != null){
        String queryFilter = "and ( o.ecommerce_purchase_id like '?%' or"
            + " c.phone like '?%' or"
            + " c.document_number like '?%')";


        queryFilters.append(queryFilter.replace("?",requestFilter.getFilter().getMultipleField()));
      }

      if (requestFilter.getFilter().getCompanyCode() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getCompanyCode());
        queryFilters.append(" and s.company_code IN(")
            .append(filters)
            .append(")");
      }

      if (requestFilter.getFilter().getLocalId() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getLocalId());
        queryFilters.append(" and s.center_code IN(")
            .append(filters)
            .append(")");
      }

      if (requestFilter.getFilter().getEcommerceId() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getEcommerceId());
        queryFilters.append(" and  o.ecommerce_purchase_id IN( ")
            .append(filters).append(") ");
      }
      if (requestFilter.getFilter().getOrderStatus() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getOrderStatus());
        queryFilters.append(" and os.type IN(").append(filters)
            .append(")");
      }

      if (requestFilter.getFilter().getPromiseDate() != null) {
        LocalDate startDate = DateUtils
            .getLocalDateFromStringWithFormatV2(requestFilter.getFilter().getPromiseDate()[0]);
        LocalDate endDate = DateUtils
            .getLocalDateFromStringWithFormatV2(requestFilter.getFilter().getPromiseDate()[1]);

        queryFilters.append(" and Date(o.scheduled_time) BETWEEN ")
            .append("'")
            .append(startDate.toString())
            .append("'")
            .append(" and ")
            .append("'")
            .append(endDate.toString())
            .append("'");

      }

      if (requestFilter.getFilter().getServiceChannel() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getServiceChannel());
        queryFilters.append(" and st.source_channel IN(")
            .append(filters)
            .append(")");
      }

      if (requestFilter.getFilter().getServiceTypeId() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getServiceTypeId());
        queryFilters.append(" and st.short_code IN(")
            .append(filters)
            .append(")");
      }

      return queryFilters.toString();
    }

  }

  public String getFiltersConcatenated(String[] filters) {
    return Arrays.stream(filters).map(a -> "'".concat(a).concat("'"))
        .collect(Collectors.joining(","));
  }

  public OrderCanonicalResponse getOrderInfo(RequestFilterDTO filter) {

    String queryFilters = getQueryOrderInfo(filter);
    //String queryTotal = CustomSqlQuery.BASIC_QUERY_GET_ORDERINFO_COUNT.toString().concat (queryFilters);
    String queryTotal = CustomSqlQuery.BASIC_QUERY_GET_ORDERINFO_COUNT.toString();
    log.info("queryFilters:{}",queryFilters);
    //String queryOrderInfo = CustomSqlQuery.BASIC_QUERY_GET_ORDERINFO.toString().concat(queryFilters);
    String queryOrderInfo = CustomSqlQuery.BASIC_QUERY_GET_ORDERINFO.toString();

    log.info("queryTotal:{}",queryTotal);
    log.info("queryOrderInfo:{}",queryOrderInfo);

    Query totalRecordsQuery = entityManager.createNativeQuery(queryTotal);
    log.info("totalRecordsQuery:{}",totalRecordsQuery);
    BigInteger totalRecords = (BigInteger) totalRecordsQuery.getSingleResult();
    log.info("totalRecords:{}",totalRecords);

    Query query = entityManager.createNativeQuery(queryOrderInfo);
    Integer page = Optional.ofNullable(filter.getPage()).orElse(1);
    Integer totalRows = Optional.ofNullable(filter.getRecords()).orElse(9);
    log.info("totalRows:{}",totalRows);
    query.setFirstResult(page > 0 ? page - 1 : page);
    query.setMaxResults(totalRows);
    List<Object[]> result = query.getResultList();

    List<OrderCanonicalFulfitment> orders = result.stream().parallel().map(data -> {
      OrderCanonicalFulfitment response = new OrderCanonicalFulfitment();
      BigInteger orderId = (BigInteger) data[0];
      response.setOrderId(orderId.longValue());

      BigInteger ecommerceId = (BigInteger) data[1];
      response.setEcommerceId(ecommerceId.longValue());
      Date promiseDate = (Date) data[3];

      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a");
      String strDate = dateFormat.format(promiseDate);
      response.setPromiseDate(strDate.toLowerCase());
      response.setOrderStatus(String.valueOf(data[4]));
      response.setLocalId(String.valueOf(data[5]));
      response.setCompanyCode(String.valueOf(data[6]));
      response.setServiceChannel(String.valueOf(data[7]));
      response.setServiceTypeId(String.valueOf(data[8]));
      response.setClient(String.valueOf(data[9]));
      response.setDocumentoId(String.valueOf(data[10]));
      return response;
    }).collect(Collectors.toList());

    OrderCanonicalResponse response = new OrderCanonicalResponse();
    response.setTotalRecords(totalRecords);
    response.setPage(BigInteger.valueOf(page));
    response.setCurrentRecords(BigInteger.valueOf(orders.size()));
    response.setOrders(orders);

    return response;


  }


}
