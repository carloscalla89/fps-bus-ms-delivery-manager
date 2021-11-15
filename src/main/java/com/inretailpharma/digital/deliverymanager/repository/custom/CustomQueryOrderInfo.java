package com.inretailpharma.digital.deliverymanager.repository.custom;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrderCanonicalFulfitment;
import com.inretailpharma.digital.deliverymanager.dto.RequestFilterDTO;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import com.inretailpharma.digital.deliverymanager.util.sql.CustomSqlQuery;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CustomQueryOrderInfo {

  @Autowired
  EntityManager entityManager;

  private String getQueryOrderInfo(RequestFilterDTO requestFilter) {
    String basicQuery = CustomSqlQuery.BASIC_QUERY_GET_ORDERINFO.toString();
    if (requestFilter.getFilter() == null) {
      return basicQuery;
    } else {
      StringBuilder query = new StringBuilder();
      query.append(basicQuery);
      query.append("where 1 = 1 ");
      if (requestFilter.getFilter().getCompanyCode() != null) {
        query.append(" and s.company_code = '").append(requestFilter.getFilter().getCompanyCode())
            .append("'");
      }

      if (requestFilter.getFilter().getLocalId() != null) {
        query.append(" and s.center_code = '").append(requestFilter.getFilter().getLocalId())
            .append("'");
      }

      if (requestFilter.getFilter().getEcommerceId() != null) {
        query.append(" and  o.ecommerce_purchase_id = ")
            .append(requestFilter.getFilter().getEcommerceId());
      }
      if (requestFilter.getFilter().getOrderStatus() != null) {
        query.append(" and os.type = '").append(requestFilter.getFilter().getOrderStatus())
            .append("'");
      }

      if (requestFilter.getFilter().getPromiseDate() != null) {
        LocalDateTime date = DateUtils.getLocalDateTimeFromStringWithFormatV2(requestFilter.getFilter().getPromiseDate());
        query.append(" and Date(o.scheduled_time) = '").append(date.toLocalDate().toString())
            .append("'");
      }

      if (requestFilter.getFilter().getServiceChannel() != null) {
        query.append(" and st.source_channel = '")
            .append(requestFilter.getFilter().getServiceChannel()).append("'");
      }

      if (requestFilter.getFilter().getServiceTypeId() != null) {
        query.append(" and st.short_code = '").append(requestFilter.getFilter().getServiceTypeId())
            .append("'");
      }

      return query.toString();
    }

  }

  public List<OrderCanonicalFulfitment> getOrderInfo(RequestFilterDTO filter) {

    String queryOrderInfo = getQueryOrderInfo(filter);

    Query query = entityManager.createNativeQuery(queryOrderInfo);
    Integer page = Optional.ofNullable(filter.getPage()).orElse(1);
    Integer totalRows = Optional.ofNullable(filter.getRecords()).orElse(9);
    query.setFirstResult(page > 0 ? page - 1 : page);
    query.setMaxResults(totalRows);
    List<Object[]> result = query.getResultList();

    return result.stream().parallel().map(data -> {
      OrderCanonicalFulfitment response = new OrderCanonicalFulfitment();
      BigInteger orderId = (BigInteger) data[0];
      response.setOrderId(orderId.longValue());

      BigInteger ecommerceId = (BigInteger) data[1];
      response.setEcommerceId(ecommerceId.longValue());
      Date promiseDate = (Date) data[3];

      DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
      String strDate = dateFormat.format(promiseDate);
      response.setPromiseDate(strDate);
      response.setOrderStatus(String.valueOf(data[4]));
      response.setLocalId(String.valueOf(data[5]));
      response.setCompanyCode(String.valueOf(data[6]));
      response.setServiceChannel(String.valueOf(data[7]));
      response.setServiceTypeId(String.valueOf(data[8]));
      response.setBusinessName(String.valueOf(data[9]));
      response.setDocumentoId(String.valueOf(data[10]));
      return response;
    }).collect(Collectors.toList());


  }



}
