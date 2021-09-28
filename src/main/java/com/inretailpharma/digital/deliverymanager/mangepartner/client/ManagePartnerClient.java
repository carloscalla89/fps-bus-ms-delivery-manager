package com.inretailpharma.digital.deliverymanager.mangepartner.client;

import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.mangepartner.model.ApiModelResponse;
import com.inretailpharma.digital.deliverymanager.mangepartner.util.RestTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class ManagePartnerClient {

	@Value("${external-service.manage-partner.update-status}")
	private String managePartnerUrl;

	private final RestTemplateUtil restTemplateUtil;
	
	public ApiModelResponse notifyEvent(String proformaNumber, ActionDto action) {
		String url = managePartnerUrl + proformaNumber;
		log.info("[START] calling MP service: ",url);

		ApiModelResponse response = restTemplateUtil.create(ApiModelResponse.class, url, HttpMethod.POST, action).getGeneric();
		log.info("[END] calling MP service: ",url);
		return response;
	}

}
