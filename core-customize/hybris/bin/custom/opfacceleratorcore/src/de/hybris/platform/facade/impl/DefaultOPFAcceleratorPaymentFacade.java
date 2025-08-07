/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.facade.impl;

import com.opf.dto.cta.CTARequestDTO;
import com.opf.dto.cta.CTAResponseDTO;
import com.opf.dto.cta.OPFActiveConfigDTO;
import com.opf.dto.request.OPFApplePayRequestDTO;
import com.opf.dto.submit.OPFPaymentSubmitRequestDTO;
import com.opf.dto.submit.OPFPaymentSubmitResponseDTO;
import com.opf.dto.verify.OPFPaymentVerifyRequestDTO;
import de.hybris.platform.constants.OpfacceleratorcoreConstants;
import de.hybris.platform.cta.request.OPFPaymentCTARequest;
import de.hybris.platform.cta.response.OPFPaymentCTAResponse;
import de.hybris.platform.data.response.OPFActiveConfigResponse;
import de.hybris.platform.facade.OPFAcceleratorPaymentFacade;
import de.hybris.platform.opf.data.OPFInitiatePaymentData;
import de.hybris.platform.opf.data.OPFInitiatePaymentSessionRequestData;

import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteRequestData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteResponseData;
import de.hybris.platform.opf.data.OPFPaymentBrowserInfoData;
import de.hybris.platform.opf.data.request.OPFApplePayRequest;
import de.hybris.platform.opf.data.request.OPFPaymentSubmitRequest;
import de.hybris.platform.opf.data.response.OPFApplePayResponse;
import de.hybris.platform.opf.data.response.OPFPaymentSubmitResponse;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionRequest;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionResponse;
import de.hybris.platform.opf.dto.OPFPaymentVerifyRequest;
import de.hybris.platform.opf.dto.OPFPaymentVerifyResponse;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteRequest;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteResponse;
import de.hybris.platform.service.OPFAcceleratorPaymentService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.util.OPFAcceleratorCoreUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open Payment Framework Accelerator SDK Facade Impl
 */
public class DefaultOPFAcceleratorPaymentFacade implements OPFAcceleratorPaymentFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOPFAcceleratorPaymentFacade.class);
    private OPFAcceleratorPaymentService opfAcceleratorPaymentService;
    private Converter<CTARequestDTO, OPFPaymentCTARequest> opfAcceleratorCTARequestConverter;
    private Converter<OPFPaymentCTAResponse, CTAResponseDTO> opfAcceleratorCTAResponseConverter;
    private Converter<OPFActiveConfigResponse, OPFActiveConfigDTO> opfAcceleratorActiveConfigResponseConverter;
    private Converter<OPFInitiatePaymentSessionRequest, OPFInitiatePaymentSessionRequestData> opfPaymentSessionRequestConverter;

    private Converter<OPFInitiatePaymentSessionResponse, OPFInitiatePaymentData> opfPaymentSessionResponseConverter;
    private Converter<OPFPaymentSubmitRequestDTO, OPFPaymentSubmitRequest> opfAcceleratorSubmitRequestConverter;
    private Converter<OPFPaymentSubmitResponse, OPFPaymentSubmitResponseDTO> opfAcceleratorSubmitResponseConverter;
    private Converter<OPFPaymentVerifyRequestDTO, OPFPaymentVerifyRequest> opfAcceleratorVerifyRequestConverter;
    private Converter<OPFPaymentSubmitCompleteRequest, OPFPaymentSubmitCompleteRequestData> opfPaymentSubmitCompleteRequestConverter;
    private Converter<OPFPaymentSubmitCompleteResponse, OPFPaymentSubmitCompleteResponseData> opfPaymentSubmitCompleteResponseConverter;
    private Converter<OPFApplePayRequestDTO, OPFApplePayRequest> opfApplePayRequestConverter;

    /**
     * Constructor for DefaultOPFAcceleratorPaymentFacade
     *
     * @param opfAcceleratorPaymentService
     *         payment service
     * @param opfAcceleratorCTARequestConverter
     *         cta request converter
     * @param opfAcceleratorCTAResponseConverter
     *         cta response converter
     * @param opfAcceleratorActiveConfigResponseConverter
     *         active config response converter
     * @param opfAcceleratorPaymentSessionRequestConverter
     *         - the request converter
     * @param opfAcceleratorPaymentSessionResponseConverter
     *         - the response converter
     * @param opfAcceleratorSubmitRequestConverter
     *        submit request converter
     * @param opfAcceleratorSubmitResponseConverter
     *        submit response converter
     * @param opfAcceleratorVerifyRequestConverter
     *        verify request converter
     * @param opfPaymentSubmitCompleteRequestConverter
     *       submit complete request converter
     * @param opfPaymentSubmitCompleteResponseConverter
     *       submit complete response converter
     */
    public DefaultOPFAcceleratorPaymentFacade(OPFAcceleratorPaymentService opfAcceleratorPaymentService,
            Converter<CTARequestDTO, OPFPaymentCTARequest> opfAcceleratorCTARequestConverter,
            Converter<OPFPaymentCTAResponse, CTAResponseDTO> opfAcceleratorCTAResponseConverter,
            Converter<OPFActiveConfigResponse, OPFActiveConfigDTO> opfAcceleratorActiveConfigResponseConverter,
            Converter<OPFInitiatePaymentSessionRequest, OPFInitiatePaymentSessionRequestData> opfAcceleratorPaymentSessionRequestConverter,
            Converter<OPFInitiatePaymentSessionResponse, OPFInitiatePaymentData> opfAcceleratorPaymentSessionResponseConverter,
            Converter<OPFPaymentSubmitRequestDTO, OPFPaymentSubmitRequest> opfAcceleratorSubmitRequestConverter,
            Converter<OPFPaymentSubmitResponse, OPFPaymentSubmitResponseDTO> opfAcceleratorSubmitResponseConverter,
            Converter<OPFPaymentVerifyRequestDTO, OPFPaymentVerifyRequest> opfAcceleratorVerifyRequestConverter,
            Converter<OPFPaymentSubmitCompleteRequest, OPFPaymentSubmitCompleteRequestData> opfPaymentSubmitCompleteRequestConverter,
            Converter<OPFPaymentSubmitCompleteResponse, OPFPaymentSubmitCompleteResponseData> opfPaymentSubmitCompleteResponseConverter,
            Converter<OPFApplePayRequestDTO, OPFApplePayRequest> opfApplePayRequestConverter) {
        this.opfAcceleratorPaymentService = opfAcceleratorPaymentService;
        this.opfAcceleratorCTARequestConverter = opfAcceleratorCTARequestConverter;
        this.opfAcceleratorCTAResponseConverter = opfAcceleratorCTAResponseConverter;
        this.opfAcceleratorActiveConfigResponseConverter = opfAcceleratorActiveConfigResponseConverter;
        this.opfPaymentSessionRequestConverter = opfAcceleratorPaymentSessionRequestConverter;
        this.opfPaymentSessionResponseConverter = opfAcceleratorPaymentSessionResponseConverter;
        this.opfAcceleratorSubmitRequestConverter = opfAcceleratorSubmitRequestConverter;
        this.opfAcceleratorSubmitResponseConverter = opfAcceleratorSubmitResponseConverter;
        this.opfAcceleratorVerifyRequestConverter = opfAcceleratorVerifyRequestConverter;
        this.opfPaymentSubmitCompleteRequestConverter = opfPaymentSubmitCompleteRequestConverter;
        this.opfPaymentSubmitCompleteResponseConverter = opfPaymentSubmitCompleteResponseConverter;
        this.opfApplePayRequestConverter=opfApplePayRequestConverter;
    }

    /**
     * @param ctaRequestWsDTO
     *         ctaRequestWsDTO
     * @return
     * {@link CTAResponseDTO}
     */
    @Override
    public CTAResponseDTO getCTAResponse(CTARequestDTO ctaRequestWsDTO) {
        CTAResponseDTO ctaResponseWsDTO = new CTAResponseDTO();
        if (ctaRequestWsDTO != null) {
            OPFPaymentCTARequest request = new OPFPaymentCTARequest();
            opfAcceleratorCTARequestConverter.convert(ctaRequestWsDTO, request);
            OPFPaymentCTAResponse ctaResponse = opfAcceleratorPaymentService.getCTAResponse(request);
            if (ctaResponse != null) {
                opfAcceleratorCTAResponseConverter.convert(ctaResponse, ctaResponseWsDTO);
            }
        }
        return ctaResponseWsDTO;
    }

    /**
     * @return OPFActiveConfigDTO
     */
    @Override
    public OPFActiveConfigDTO getActiveConfigurations() {
        OPFActiveConfigDTO activeConfigWsDTO = new OPFActiveConfigDTO();
        OPFActiveConfigResponse activeConfigResponse = opfAcceleratorPaymentService.getActiveConfigurations();
        opfAcceleratorActiveConfigResponseConverter.convert(activeConfigResponse, activeConfigWsDTO);
        return activeConfigWsDTO;
    }

    /**
     * Initiate OPF Payment Session
     *
     * @param paymentRequest
     *         Payment request data
     * @return OPFInitiatePaymentData as response
     */
    @Override
    public OPFInitiatePaymentData getInitiatePaymentResponse(OPFInitiatePaymentSessionRequest paymentRequest) {
        if (paymentRequest == null) {
            LOGGER.warn("Initiate payment request is null.");
            return new OPFInitiatePaymentData();
        }
        OPFInitiatePaymentSessionRequestData requestData = new OPFInitiatePaymentSessionRequestData();
        opfPaymentSessionRequestConverter.convert(paymentRequest, requestData);
        OPFInitiatePaymentSessionResponse response = opfAcceleratorPaymentService.getInitiatePaymentResponse(requestData);
        OPFInitiatePaymentData paymentResponse = new OPFInitiatePaymentData();
        if (response != null) {
            opfPaymentSessionResponseConverter.convert(response, paymentResponse);
        } else {
            LOGGER.warn("Received null response for initiate payment request.");
        }
        return paymentResponse;
    }

    /**
     * submit payment
     *
     * @param opfPaymentSubmitRequestDTO
     *         opfPaymentSubmitRequestDTO
     * @param paymentSessionId
     *         paymentSessionId
     * @param ipAddress
     *         ipAddress
     * @return {@link OPFPaymentSubmitResponseDTO}
     * @see OPFPaymentSubmitResponseDTO
     */
    @Override
    public OPFPaymentSubmitResponseDTO submitPayment(OPFPaymentSubmitRequestDTO opfPaymentSubmitRequestDTO, String paymentSessionId,
            String ipAddress) {
        OPFPaymentSubmitResponseDTO opfPaymentSubmitResponseDTO = new OPFPaymentSubmitResponseDTO();
        if (opfPaymentSubmitRequestDTO != null) {
            OPFPaymentSubmitRequest opfPaymentSubmitRequest = new OPFPaymentSubmitRequest();
            opfAcceleratorSubmitRequestConverter.convert(opfPaymentSubmitRequestDTO, opfPaymentSubmitRequest);
            //PaymentSessionId is not required for Quick Buy with GPay or Apple Pay
            boolean isQuickBuy = OPFAcceleratorCoreUtil.isQuickBuy(opfPaymentSubmitRequestDTO.getPaymentMethod());
            if(!isQuickBuy){
                opfPaymentSubmitRequest.setPaymentSessionId(paymentSessionId);
            }
            OPFPaymentBrowserInfoData browserInfoData = opfPaymentSubmitRequest.getBrowserInfo();
            browserInfoData.setIpAddress(ipAddress);
            opfPaymentSubmitRequest.setBrowserInfo(browserInfoData);
            OPFPaymentSubmitResponse opfPaymentSubmitResponse = opfAcceleratorPaymentService.submitPayment(opfPaymentSubmitRequest, isQuickBuy);
            if (opfPaymentSubmitResponse != null) {
                opfAcceleratorSubmitResponseConverter.convert(opfPaymentSubmitResponse, opfPaymentSubmitResponseDTO);
            }
        }
        return opfPaymentSubmitResponseDTO;
    }

    /* OPF Submit Complete payment
     *
     * @param paymentRequest
     *         Payment request data
     * @return OPFPaymentSubmitCompleteResponse as response
     */
    @Override
    public OPFPaymentSubmitCompleteResponseData getCompletedPaymentResponse(OPFPaymentSubmitCompleteRequest paymentRequest) {
        OPFPaymentSubmitCompleteRequestData requestData = convertToRequestData(paymentRequest);
        OPFPaymentSubmitCompleteResponse response = opfAcceleratorPaymentService.getCompletedPaymentResponse(requestData);
        return validateAndConvertResponse(response);
    }

    @Override
    public void setPaymentInfoOnCart() {
        opfAcceleratorPaymentService.setPaymentInfo();
    }

    @Override
    public OPFApplePayResponse getApplePayWebSession(OPFApplePayRequestDTO opfApplePayRequestDTO) {
      OPFApplePayResponse opfApplePayResponse = new OPFApplePayResponse();
        if (opfApplePayRequestDTO != null) {
            OPFApplePayRequest opfApplePayRequest = new OPFApplePayRequest();
            opfApplePayRequestConverter.convert(opfApplePayRequestDTO, opfApplePayRequest);
            opfApplePayResponse = opfAcceleratorPaymentService.getApplePayWebSession(opfApplePayRequest);
        }
        return opfApplePayResponse;
    }

    @Override
    public OPFPaymentVerifyResponse verifyPayment(OPFPaymentVerifyRequestDTO opfPaymentVerifyRequestDTO) {
        OPFPaymentVerifyRequest opfPaymentVerifyRequest = new OPFPaymentVerifyRequest();
        opfAcceleratorVerifyRequestConverter.convert(opfPaymentVerifyRequestDTO, opfPaymentVerifyRequest);
        return opfAcceleratorPaymentService.verifyPayment(opfPaymentVerifyRequest);
    }

    /**
     * Convert the request to data
     *
     * @param paymentRequest
     *         Payment request data
     * @return requestData
     */
    private OPFPaymentSubmitCompleteRequestData convertToRequestData(OPFPaymentSubmitCompleteRequest paymentRequest) {
        OPFPaymentSubmitCompleteRequestData requestData = new OPFPaymentSubmitCompleteRequestData();
        opfPaymentSubmitCompleteRequestConverter.convert(paymentRequest, requestData);
        return requestData;
    }

    /**
     * Method to validate the response and convert it
     *
     * @param response
     *         OPFPaymentSubmitCompleteResponseData
     * @return OPFPaymentSubmitCompleteResponseData
     */
    private OPFPaymentSubmitCompleteResponseData validateAndConvertResponse(OPFPaymentSubmitCompleteResponse response) {
        OPFPaymentSubmitCompleteResponseData paymentResponse = new OPFPaymentSubmitCompleteResponseData();
        if (null != response && response.getPaymentSessionId() != null && StringUtils.isNotEmpty(response.getStatus())) {
            opfPaymentSubmitCompleteResponseConverter.convert(response, paymentResponse);
        }
        return paymentResponse;
    }

}
