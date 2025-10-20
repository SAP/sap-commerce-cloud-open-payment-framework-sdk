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
import com.opf.order.data.OPFB2BPaymentTypeData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;

import de.hybris.platform.cta.request.OPFPaymentCTARequest;
import de.hybris.platform.cta.response.OPFPaymentCTAResponse;
import de.hybris.platform.data.response.OPFActiveConfigResponse;
import de.hybris.platform.data.response.OPFActiveConfigValue;
import de.hybris.platform.facade.OPFAcceleratorFacade;

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

import de.hybris.platform.opf.dto.user.AddressWsDTO;
import de.hybris.platform.opf.dto.user.CountryWsDTO;
import de.hybris.platform.service.OPFAcceleratorService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.util.OPFAcceleratorCoreUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Open Payment Framework Accelerator SDK Facade Impl
 */
public class DefaultOPFAcceleratorFacade implements OPFAcceleratorFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOPFAcceleratorFacade.class);
    private OPFAcceleratorService opfAcceleratorService;
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
    private Converter<OPFActiveConfigValue, OPFB2BPaymentTypeData> opfB2BAcceleratorActiveConfigResponseConverter;
    /**
     * Constructor for DefaultOPFAcceleratorFacade
     *
     * @param opfAcceleratorService
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
     * @param opfApplePayRequestConverter
     *       apple pay request converter
     * @param opfB2BAcceleratorActiveConfigResponseConverter
     *      B2B active config response converter
     */
    public DefaultOPFAcceleratorFacade(OPFAcceleratorService opfAcceleratorService,
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
            Converter<OPFApplePayRequestDTO, OPFApplePayRequest> opfApplePayRequestConverter,
            Converter<OPFActiveConfigValue, OPFB2BPaymentTypeData> opfB2BAcceleratorActiveConfigResponseConverter) {
        this.opfAcceleratorService = opfAcceleratorService;
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
        this.opfB2BAcceleratorActiveConfigResponseConverter=opfB2BAcceleratorActiveConfigResponseConverter;
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
            OPFPaymentCTAResponse ctaResponse = opfAcceleratorService.getCTAResponse(request);
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
        OPFActiveConfigResponse activeConfigResponse = opfAcceleratorService.getActiveConfigurations();
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
        OPFInitiatePaymentSessionResponse response = opfAcceleratorService.getInitiatePaymentResponse(requestData);
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
            if(browserInfoData == null) {
                browserInfoData = new OPFPaymentBrowserInfoData();
            }
            browserInfoData.setIpAddress(ipAddress);
            opfPaymentSubmitRequest.setBrowserInfo(browserInfoData);
            OPFPaymentSubmitResponse opfPaymentSubmitResponse = opfAcceleratorService.submitPayment(opfPaymentSubmitRequest, isQuickBuy);
            if (opfPaymentSubmitResponse != null) {
                opfAcceleratorSubmitResponseConverter.convert(opfPaymentSubmitResponse, opfPaymentSubmitResponseDTO);
            }
        }
        return opfPaymentSubmitResponseDTO;
    }

    /** OPF Submit Complete payment
     *
     * @param paymentRequest
     *         Payment request data
     * @return OPFPaymentSubmitCompleteResponse as response
     */
    @Override
    public OPFPaymentSubmitCompleteResponseData getCompletedPaymentResponse(OPFPaymentSubmitCompleteRequest paymentRequest) {
        OPFPaymentSubmitCompleteRequestData requestData = convertToRequestData(paymentRequest);
        OPFPaymentSubmitCompleteResponse response = opfAcceleratorService.getCompletedPaymentResponse(requestData);
        return validateAndConvertResponse(response);
    }

    /**
     * set payment info on cart
     *
     */
    @Override
    public void setPaymentInfoOnCart() {
        opfAcceleratorService.setPaymentInfo();
    }

    /**
     * get apple pay web session
     *
     * @param opfApplePayRequestDTO opfApplePayRequestDTO
     * @return {@link OPFApplePayResponse}
     * @see OPFApplePayResponse
     */
    @Override
    public OPFApplePayResponse getApplePayWebSession(OPFApplePayRequestDTO opfApplePayRequestDTO) {
        OPFApplePayResponse opfApplePayResponse = new OPFApplePayResponse();
        if (opfApplePayRequestDTO != null) {
            OPFApplePayRequest opfApplePayRequest = new OPFApplePayRequest();
            opfApplePayRequestConverter.convert(opfApplePayRequestDTO, opfApplePayRequest);
            opfApplePayResponse = opfAcceleratorService.getApplePayWebSession(opfApplePayRequest);
            if (opfApplePayResponse == null) {
                opfApplePayResponse = new OPFApplePayResponse();
            }
        }

        return opfApplePayResponse;
    }

    /**
     * verify payment
     *
     * @param opfPaymentVerifyRequestDTO opfPaymentVerifyRequestDTO
     * @return {@link OPFPaymentVerifyResponse}
     * @see OPFPaymentVerifyResponse
     */
    @Override
    public OPFPaymentVerifyResponse verifyPayment(OPFPaymentVerifyRequestDTO opfPaymentVerifyRequestDTO) {
        OPFPaymentVerifyRequest opfPaymentVerifyRequest = new OPFPaymentVerifyRequest();
        opfAcceleratorVerifyRequestConverter.convert(opfPaymentVerifyRequestDTO, opfPaymentVerifyRequest);
        return opfAcceleratorService.verifyPayment(opfPaymentVerifyRequest);
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

    /**
     * Maps an {@link AddressWsDTO} object to an {@link AddressData} object. This method performs a shallow copy of simple properties using
     * {@link org.springframework.beans.BeanUtils} and explicitly maps nested objects such as {@code CountryWsDTO} to {@code CountryData}.
     * It is useful in controller or facade layers where incoming web service DTOs need to be transformed into internal data objects used in
     * the service layer.
     *
     * @param source
     *         the {@link AddressWsDTO} object containing data from the web service request. Must not be {@code null}.
     * @return an {@link AddressData} object populated with values from the given {@code AddressWsDTO}.
     */
    @Override
    public AddressData mapAddressWsDTOToAddressData(AddressWsDTO source) {
        AddressData target = new AddressData();
        BeanUtils.copyProperties(source, target);

        Optional.ofNullable(source.getCountry()).ifPresent(country -> {
            CountryData countryData = new CountryData();
            BeanUtils.copyProperties(country, countryData);
            target.setCountry(countryData);
        });

        Optional.ofNullable(source.getRegion()).ifPresent(region -> {
            RegionData regionData = new RegionData();
            String regionIso = region.getIsocode();
            String countryIso = Optional.ofNullable(source.getCountry()).map(CountryWsDTO::getIsocode).orElse(null);
            if (regionIso != null && countryIso != null) {
                regionData.setIsocodeShort(regionIso);
                regionData.setCountryIso(countryIso);
                regionData.setIsocode(regionIso.contains("-") ? regionIso : (countryIso + "-" + regionIso));
                target.setRegion(regionData);
            }
        });
        return target;
    }

    @Override
    public List<OPFB2BPaymentTypeData> getB2BActiveConfigurations() {
        List<OPFB2BPaymentTypeData> b2BPaymentTypeDataList = new ArrayList<>();
        OPFActiveConfigResponse activeConfigResponse = opfAcceleratorService.getActiveConfigurations();
        List<OPFActiveConfigValue> activeConfigList=activeConfigResponse.getValue();
        if(!CollectionUtils.isEmpty(activeConfigList)){
            b2BPaymentTypeDataList=  opfB2BAcceleratorActiveConfigResponseConverter.convertAll(activeConfigList);
        }
        return b2BPaymentTypeDataList;
    }

    @Override
    public void setPaymentInfoOnCartForAccount() {
        opfAcceleratorService.setPaymentInfoForAccount();
    }

    /**
     * Clears the SAP Payment Option ID from the current session or cart.
     * This method is useful for resetting or removing any previously set payment option identifiers.
     */
    @Override
    public void  clearSapPaymentOptionId() {
        opfAcceleratorService.clearSapPaymentOptionId();
    }

}
