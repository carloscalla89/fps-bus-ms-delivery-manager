package com.inretailpharma.digital.deliverymanager.repository.custom;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrderCanonicalFulfitment;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrderCanonicalResponse;
import com.inretailpharma.digital.deliverymanager.dto.RequestFilterDTO;
import com.inretailpharma.digital.deliverymanager.util.Constant;
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
    int timeLimitFilter = 0;
    boolean timeUnlimited = false;
    boolean existsDateFilter = false;
    boolean existsStatusFilter = false;
    StringBuilder queryFilters = new StringBuilder();

    if (requestFilter.getFilter() == null) {
      timeLimitFilter = Constant.TimeLimitFilterDate.TIME_LIMIT_GRID;

      queryFilters.append(" where 1 = 1 ");

      if (requestFilter.getOrderStatusCodeAllowed() != null) {
        StringBuilder queryInOrderStatusCodeAllowed = new StringBuilder();
        queryInOrderStatusCodeAllowed
                .append(" and os.code in (")
                .append(getFiltersConcatenated(
                          requestFilter
                            .getOrderStatusCodeAllowed()
                            .toArray(new String[0])))
                .append(") ");

        queryFilters.append(queryInOrderStatusCodeAllowed.toString());
      }
    } else {
      timeLimitFilter = Constant.TimeLimitFilterDate.TIME_LIMIT_OTHER;
      queryFilters.append("where 1 = 1 ");

      //TODO: OMS
      if (requestFilter.getFilter().getFilterType() != null && requestFilter.getFilter().getValueFilterType() != null) {
        String queryFilter = "";
        if (requestFilter.getFilter().getFilterType().equalsIgnoreCase(Constant.FilterOption.FIND_ORDER_NUMBER)) { //N° pedido
          timeUnlimited = true;
          queryFilter = "and o.ecommerce_purchase_id = '?' ";
        } else {
          if (requestFilter.getFilter().getFilterType().equalsIgnoreCase(Constant.FilterOption.FIND_TELEPHONE_NUMBER)) {//telefono
            queryFilter = "and c.phone = '?' ";
          } else {//documento
            queryFilter = "and c.document_number = '?' ";
          }
        }
        queryFilters.append(queryFilter.replace("?",requestFilter.getFilter().getValueFilterType()));
      }

      if (requestFilter.getFilter().getCompanyCode() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getCompanyCode());
        queryFilters.append(" and s.company_code IN(")
            .append(filters).append(") ");
      }

      if (requestFilter.getFilter().getLocalId() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getLocalId());
        queryFilters.append(" and s.center_code IN(")
            .append(filters).append(") ");
      }

      if (requestFilter.getFilter().getEcommerceId() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getEcommerceId());
        queryFilters.append(" and o.ecommerce_purchase_id IN( ")
            .append(filters).append(") ");
      }
      if (requestFilter.getFilter().getOrderStatus() != null) {
        existsStatusFilter = true;
        String filters = getFiltersConcatenated(requestFilter.getFilter().getOrderStatus());
        queryFilters.append(" and os.code IN(")
            .append(filters).append(") ");
      }

      if (requestFilter.getFilter().getPromiseDate() != null) {
        existsDateFilter = true;
        LocalDate startDate = DateUtils
            .getLocalDateFromStringWithFormatV2(requestFilter.getFilter().getPromiseDate()[0]);
        LocalDate endDate = DateUtils
            .getLocalDateFromStringWithFormatV2(requestFilter.getFilter().getPromiseDate()[1]);

        queryFilters.append(" and Date(o.scheduled_time) BETWEEN ")
            .append("'").append(startDate.toString()).append("'")
            .append(" and ")
            .append("'").append(endDate.toString()).append("' ");
      }

      if (requestFilter.getFilter().getServiceChannel() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getServiceChannel());
        queryFilters.append(" and st.source_channel IN(")
            .append(filters).append(") ");
      }

      if (requestFilter.getFilter().getServiceTypeId() != null) {
        String filters = getFiltersConcatenated(requestFilter.getFilter().getServiceTypeId());
        queryFilters.append(" and st.short_code IN(")
            .append(filters).append(") ");
      }

      if (!existsStatusFilter && requestFilter.getOrderStatusCodeAllowed() != null) {
        StringBuilder queryInOrderStatusCodeAllowed = new StringBuilder();
        queryInOrderStatusCodeAllowed
                .append(" and os.code in (")
                .append(getFiltersConcatenated(
                        requestFilter
                                .getOrderStatusCodeAllowed()
                                .toArray(new String[0])))
                .append(") ");

        queryFilters.append(queryInOrderStatusCodeAllowed.toString());
      }
    }

    if (!timeUnlimited && !existsDateFilter) {
      LocalDate endDate = LocalDate.now();
      LocalDate startDate = endDate.minusMonths(timeLimitFilter);

      queryFilters.append(" and Date(o.scheduled_time) BETWEEN ")
              .append("'").append(startDate.toString()).append("'")
              .append(" and ")
              .append("'").append(endDate.toString()).append("' ");
    }

    return queryFilters.toString();
  }

  private String getQueryOrderCriteria(RequestFilterDTO requestFilter) {
    StringBuilder queryCriteria = new StringBuilder();

    if (requestFilter.getOrderCriteria() == null) {
      queryCriteria.append(CustomSqlQuery.BASIC_QUERY_ORDER_BY_DATE_AND_ORDER_NUMBER_DESC.toString());
    } else {
      queryCriteria.append(" order by ");
      switch (requestFilter.getOrderCriteria().getColumn()) {
        case Constant.OrderCriteriaColumn.ORDER_CRITERIA_ECOMMERCE_ID: {
          queryCriteria.append(" o.ecommerce_purchase_id ? ");
          break;
        }
        case Constant.OrderCriteriaColumn.ORDER_CRITERIA_STORE: {
          queryCriteria.append(" s.center_code ? ");
          break;
        }
        case Constant.OrderCriteriaColumn.ORDER_CRITERIA_CHANNEL: {
          queryCriteria.append(" st.source_channel ? ");
          break;
        }
        case Constant.OrderCriteriaColumn.ORDER_CRITERIA_SERVICE_TYPE: {
          queryCriteria.append(" st.short_code ? ");
          break;
        }
        case Constant.OrderCriteriaColumn.ORDER_CRITERIA_DATE: {
          queryCriteria.append(" o.scheduled_time ? ");
          break;
        }
        case Constant.OrderCriteriaColumn.ORDER_CRITERIA_CLIENT: {
          queryCriteria.append(" c.first_name ? ");
          break;
        }
        case Constant.OrderCriteriaColumn.ORDER_CRITERIA_DOCUMENT: {
          queryCriteria.append(" c.document_number ? ");
          break;
        }
        case Constant.OrderCriteriaColumn.ORDER_CRITERIA_STATUS: {
          queryCriteria.append(" os.code ? ");
          break;
        }
      }
      queryCriteria.replace(queryCriteria.toString().indexOf("?"),queryCriteria.toString().indexOf("?")+1,Constant.OrderCriteria.getByCode(requestFilter.getOrderCriteria().getOrder()).getOrder());
      //queryCriteria.toString().replace("?",Constant.OrderCriteria.getByCode(requestFilter.getCriteria().getOrder()).getOrder());
    }
    return queryCriteria.toString();
  }

  public String getFiltersConcatenated(String[] filters) {
    return Arrays.stream(filters)
            .map(a -> {
              if(a.split(",").length>1) {
                return getFiltersConcatenated(a.split(","));
              } else {
                return "'".concat(a).concat("'");
              }
            })
        .collect(Collectors.joining(","));
  }

  public OrderCanonicalResponse getOrderInfo(RequestFilterDTO filter) {
    String queryFilters = getQueryOrderInfo(filter);
    log.info("queryFilters:{}",queryFilters);

    String queryCriterias = getQueryOrderCriteria(filter);
    log.info("queryCriterias:{}",queryCriterias);

    String queryTotal = CustomSqlQuery.BASIC_QUERY_GET_ORDERINFO_COUNT.toString()
                          .concat(queryFilters);
    log.info("queryTotal:{}",queryTotal);

    String queryOrderInfo = CustomSqlQuery.BASIC_QUERY_GET_ORDERINFO.toString()
                              .concat(queryFilters)
                              .concat(queryCriterias);
    log.info("queryOrderInfo: {}",queryOrderInfo);

    Query totalRecordsQuery = entityManager.createNativeQuery(queryTotal);
    log.info("totalRecordsQuery:{}",totalRecordsQuery);

    BigInteger totalRecords = (BigInteger) totalRecordsQuery.getSingleResult();
    log.info("totalRecords:{}",totalRecords);

    Query query = entityManager.createNativeQuery(queryOrderInfo);
    Integer page = Optional.ofNullable(filter.getPage()).orElse(1);
    Integer totalRows = Optional.ofNullable(filter.getRecords()).orElse(9);
    log.info("totalRows:{}",totalRows);
    query.setFirstResult(page > 0 ? (page-1)*totalRows : page);
    query.setMaxResults(totalRows);
    List<Object[]> result = query.getResultList();

    List<OrderCanonicalFulfitment> orders = result.stream().parallel().map(data -> {
      OrderCanonicalFulfitment response = new OrderCanonicalFulfitment();
      BigInteger orderId = (BigInteger) data[0];
      response.setOrderId(orderId.longValue());

      BigInteger ecommerceId = (BigInteger) data[1];
      response.setEcommerceId(ecommerceId.longValue());
      Date promiseDate = (Date) data[3];

      DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy hh:mm a");
      String strDate = dateFormat.format(promiseDate);
      response.setPromiseDate(strDate.toUpperCase());
      response.setOrderStatus(String.valueOf(data[4]));
      response.setLocalId(String.valueOf(data[5]));
      response.setCompanyCode(String.valueOf(data[6]));
      response.setServiceChannel(String.valueOf(data[7]));
      response.setServiceTypeId(String.valueOf(data[8]));
      response.setClient(String.valueOf(data[9]).concat(" ").concat(String.valueOf(data[11])));
      response.setDocumentoId(String.valueOf(data[10]));
      response.setStatusCode(String.valueOf(data[12]));
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
