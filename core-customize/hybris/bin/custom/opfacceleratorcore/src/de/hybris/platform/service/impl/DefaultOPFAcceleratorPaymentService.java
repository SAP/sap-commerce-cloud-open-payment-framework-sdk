/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.service.impl;

import de.hybris.platform.client.OPFHttpClient;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.constants.OpfacceleratorcoreConstants;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.SAPGenericPaymentInfoModel;
import de.hybris.platform.cta.request.OPFPaymentCTARequest;
import de.hybris.platform.cta.response.OPFPaymentCTAResponse;
import de.hybris.platform.data.response.OPFActiveConfigResponse;
import de.hybris.platform.opf.data.OPFInitiatePaymentSessionRequestData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteRequestData;
import de.hybris.platform.opf.data.request.OPFApplePayRequest;
import de.hybris.platform.opf.data.request.OPFPaymentSubmitRequest;
import de.hybris.platform.opf.data.response.OPFApplePayResponse;
import de.hybris.platform.opf.data.response.OPFPaymentSubmitResponse;
import de.hybris.platform.opf.dto.*;
import de.hybris.platform.opfservices.client.CCAdapterClientException;
import de.hybris.platform.opfservices.dtos.http.HttpClientRequestDto;
import de.hybris.platform.order.CartService;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.service.OPFAcceleratorPaymentService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.OPFAcceleratorCoreUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Open Payment Framework Accelerator SDK Service Impl
 */
public class DefaultOPFAcceleratorPaymentService implements OPFAcceleratorPaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultOPFAcceleratorPaymentService.class);

    private static final String OPF_BASE_URL = "opf.base.url";
    private static final String OPF_CTA_URL = "opf.cta.url";
    private static final String OPF_ACTIVE_CONFIG_URL = "opf.active.config.url";
    private static final String OPF_ACTIVE_CONFIG_PAGE_SIZE = "opf.active.config.pageSize";
    private static final String OPF_ACTIVE_CONFIG_PAGE_NUMBER = "opf.active.config.pageNumber";
    private static final String OPF_ACTIVE_CONFIG_DIVISIONID = "opf.active.config.division.id";
    private static final String OPF_ACTIVE_CONFIG_CONFIGURATIONID = "opf.active.config.configuration.id";
    private static final String OPF_PAYMENT_INITIATE_URL = "opf.initiate.payment.session.url";
    private static final String OPF_SUBMIT_URL = "opf.submit.url";
    private static final String OPF_VERIFY_URL = "opf.verify.url";
    private static final String OPF_SUBMIT_COMPLETE_URL = "opf.complete.payment.url";
    private static final String OPF_PAYMENT_STATUSES = "opf.payment.statuses";
    private static final String OPF_APPLE_PAY_WEB_SESSION_URL = "opf.applepay.web.session.url";
    private ConfigurationService configurationService;
    @Resource(name = "opfAcceleratorRestTemplate")
    RestTemplate opfAcceleratorRestTemplate;
    @Resource(name = "cartService")
    private CartService cartService;
    @Resource(name = "modelService")
    private ModelService modelService;
    private CheckoutFacade checkoutFacade;
    private OPFHttpClient opfHttpClient;

    /**
     * Constructor for DefaultOPFAcceleratorPaymentService
     *
     * @param opfHttpClient
     *         the httpclient
     * @param configurationService
     *         configurationService
     */
    public DefaultOPFAcceleratorPaymentService(final OPFHttpClient opfHttpClient, final ConfigurationService configurationService,
          final CheckoutFacade checkoutFacade) {
        this.opfHttpClient = opfHttpClient;
        this.configurationService = configurationService;
        this.checkoutFacade = checkoutFacade;
    }

    /**
     * @param opfPaymentCTARequest
     *         request
     * @return OPFPaymentCTAResponse
     */
    @Override
    public OPFPaymentCTAResponse getCTAResponse(final OPFPaymentCTARequest opfPaymentCTARequest) {

        final HttpClientRequestDto<OPFPaymentCTAResponse> request = createPostRequest(OPFPaymentCTAResponse.class);
        request.setPath(getConfigurationValueForKey(OPF_CTA_URL));
        request.setRequestBody(opfPaymentCTARequest);
        LOG.debug("OPF CTA call");
        return opfHttpClient.httpExchange(getConfigurationValueForKey(OPF_BASE_URL), request);
    }

    /**
     * @return OPFActiveConfigResponse
     */
    @Override
    public OPFActiveConfigResponse getActiveConfigurations() {
        final HttpClientRequestDto<OPFActiveConfigResponse> request = createRequestForActiveConfig();
        request.setPath(getConfigurationValueForKey(OPF_ACTIVE_CONFIG_URL));
        return opfHttpClient.httpExchange(getConfigurationValueForKey(OPF_BASE_URL), request);
    }

    /**
     * create request for active config
     *
     * @return {@link HttpClientRequestDto}
     * @see HttpClientRequestDto
     * @see OPFActiveConfigResponse
     */
    protected HttpClientRequestDto<OPFActiveConfigResponse> createRequestForActiveConfig() {
        final HttpClientRequestDto<OPFActiveConfigResponse> request = new HttpClientRequestDto<>();
        request.setResponseType(OPFActiveConfigResponse.class);
        request.setHttpMethod(HttpMethod.GET);
        Map<String, String> queryParams = new HashMap<>();
        if (StringUtils.isNotEmpty(getConfigurationValueForKey(OPF_ACTIVE_CONFIG_PAGE_SIZE))) {
            queryParams.put("pageSize", getConfigurationValueForKey(OPF_ACTIVE_CONFIG_PAGE_SIZE));
        }
        if (StringUtils.isNotEmpty(getConfigurationValueForKey(OPF_ACTIVE_CONFIG_PAGE_NUMBER))) {
            queryParams.put("pageNumber", getConfigurationValueForKey(OPF_ACTIVE_CONFIG_PAGE_NUMBER));
        }
        if (StringUtils.isNotEmpty(getConfigurationValueForKey(OPF_ACTIVE_CONFIG_DIVISIONID))) {
            queryParams.put("divisionId", getConfigurationValueForKey(OPF_ACTIVE_CONFIG_DIVISIONID));
        }
        if (StringUtils.isNotEmpty(getConfigurationValueForKey(OPF_ACTIVE_CONFIG_CONFIGURATIONID))) {
            queryParams.put("configurationId", getConfigurationValueForKey(OPF_ACTIVE_CONFIG_CONFIGURATIONID));
        }
        if (MapUtils.isNotEmpty(queryParams)) {
            request.setQueryParams(queryParams);
        }
        request.setOAuth(true);
        return request;
    }

    /**
     * Initiate OPF Payment Session
     *
     * @param paymentSessionRequest
     *         Payment request data
     * @return OPFInitiatePaymentSessionResponse as response
     */
    @Override
    public OPFInitiatePaymentSessionResponse getInitiatePaymentResponse(final OPFInitiatePaymentSessionRequestData paymentSessionRequest) {
        final HttpClientRequestDto<OPFInitiatePaymentSessionResponse> request = createPostRequest(OPFInitiatePaymentSessionResponse.class);
        request.setPath(getConfigurationValueForKey(OPF_PAYMENT_INITIATE_URL));
        Pair<String, String> properties = opfHttpClient.getSecurityProperties();
        paymentSessionRequest.setClientId(properties.getKey());
        paymentSessionRequest.setPublicKey(properties.getValue());
        request.setRequestBody(paymentSessionRequest);

        OPFInitiatePaymentSessionResponse initiatePaymentSessionResponse = opfHttpClient.httpExchange(getConfigurationValueForKey(OPF_BASE_URL), request);
        createPaymentTransaction(initiatePaymentSessionResponse.getPaymentSessionId());
        return initiatePaymentSessionResponse;
    }

    /**
     * Create Payment Transaction Model for initiate API and gateway submit API for Quick Buy
     * @param paymentSessionId
     */
    private void createPaymentTransaction(String paymentSessionId){

        validateParameterNotNullStandardMessage("Payment Session Id", paymentSessionId);

        final CartModel cartModel = cartService.getSessionCart();
        final CartData cartData = checkoutFacade.getCheckoutCart();
        PaymentTransactionModel paymentTransactionModel;
        if(CollectionUtils.isNotEmpty(cartModel.getPaymentTransactions())){
            paymentTransactionModel = cartModel.getPaymentTransactions().get(0);
        }else{
            paymentTransactionModel = modelService.create(PaymentTransactionModel.class);
        }
        paymentTransactionModel.setCode(paymentSessionId);
        paymentTransactionModel.setInfo(cartModel.getPaymentInfo());
        paymentTransactionModel.setOrder(cartModel);
        paymentTransactionModel.setCurrency(cartModel.getCurrency());
        paymentTransactionModel.setPlannedAmount(cartData.getTotalPrice().getValue());
        paymentTransactionModel.setPaymentProvider(OpfacceleratorcoreConstants.PAYMENT_PROVIDER);
        modelService.save(paymentTransactionModel);
        modelService.refresh(paymentTransactionModel);
        checkoutFacade.prepareCartForCheckout();
    }

    /**
     *
     * @param opfPaymentSubmitRequest
     * @param isQuickBuy
     * @return
     */
    @Override
    public OPFPaymentSubmitResponse submitPayment(OPFPaymentSubmitRequest opfPaymentSubmitRequest, boolean isQuickBuy) {
        final HttpClientRequestDto<OPFPaymentSubmitResponse> request = createPostRequest(OPFPaymentSubmitResponse.class);
        request.setPath(getConfigurationValueForKey(OPF_SUBMIT_URL));

        //Quick Buy: Client id and public key are required for PaymentTransaction integration.
        if(isQuickBuy){
            Pair<String, String> properties = opfHttpClient.getSecurityProperties();
            opfPaymentSubmitRequest.setClientId(properties.getKey());
            opfPaymentSubmitRequest.setPublicKey(properties.getValue());
        }
        request.setRequestBody(opfPaymentSubmitRequest);
        OPFPaymentSubmitResponse paymentSubmitResponse = opfHttpClient.httpExchange(getConfigurationValueForKey(OPF_BASE_URL), request);

        //Quick Buy: create PaymentTransactionModel
        if(isQuickBuy){
            createPaymentTransaction(paymentSubmitResponse.getPaymentSessionId());
        }
        return paymentSubmitResponse;
    }

    /**
     * verify payment
     *
     * @param opfPaymentVerifyRequest
     *         opfPaymentVerifyRequest
     * @return {@link OPFPaymentVerifyResponse}
     * @see OPFPaymentVerifyResponse
     */
    @Override
    public OPFPaymentVerifyResponse verifyPayment(OPFPaymentVerifyRequest opfPaymentVerifyRequest) {
        final HttpClientRequestDto<OPFPaymentVerifyResponse> request = this.createPostRequest(OPFPaymentVerifyResponse.class);
        request.setPath(getConfigurationValueForKey(OPF_VERIFY_URL));
        request.setRequestBody(opfPaymentVerifyRequest);
        return opfHttpClient.httpExchange(getConfigurationValueForKey(OPF_BASE_URL), request);
    }

    /**
     * Submit and Complete OPF Payment
     *
     * @param paymentRequest
     *         Payment request data
     * @return OPFPaymentSubmitCompleteResponse as response
     */
    public OPFPaymentSubmitCompleteResponse getCompletedPaymentResponse(final OPFPaymentSubmitCompleteRequestData paymentRequest) {
        final HttpClientRequestDto<OPFPaymentSubmitCompleteResponse> request = createPostRequest(OPFPaymentSubmitCompleteResponse.class);
        request.setPath(getConfigurationValueForKey(OPF_SUBMIT_COMPLETE_URL));
        request.setRequestBody(paymentRequest);
        OPFPaymentSubmitCompleteResponse response = opfHttpClient.httpExchange(getConfigurationValueForKey(OPF_BASE_URL), request);
        Boolean paymentStatus = validatePaymentStatus(response);
        if (BooleanUtils.isFalse(paymentStatus)) {
            throw new CCAdapterClientException(
                    String.format("Payment validation failed: top-level status '%s' is not in the list of accepted statuses.",
                            response.getStatus()));
        }
        return response;
    }

    /**
     * Creates a POST request with a specified response type.
     *
     * @param <T>
     *         The type of the response expected from the HTTP call.
     * @param responseType
     *         The class type of the response.
     * @return A configured HttpClientRequestDto for the specified response type.
     */
    protected <T> HttpClientRequestDto<T> createPostRequest(Class<T> responseType) {
        final HttpClientRequestDto<T> request = new HttpClientRequestDto<>();
        request.setResponseType(responseType);
        request.setHttpMethod(HttpMethod.POST);
        request.setHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_VALUE));
        request.setOAuth(true);
        return request;
    }

    /**
     * Method is to validate payment status
     *
     * @param response
     *         OPFPaymentSubmitCompleteResponse
     * @return Boolean True/False
     */
    public Boolean validatePaymentStatus(OPFPaymentSubmitCompleteResponse response) {
        return isStatusValid(response.getStatus()) && isCustomFieldStatusValid(response);
    }

    @Override
    public void setPaymentInfo() {
        CartModel cartModel = cartService.getSessionCart();
        if (null != cartModel && cartModel.getPaymentInfo() == null) {
            SAPGenericPaymentInfoModel sapGenericPaymentInfoModel = modelService.create(SAPGenericPaymentInfoModel.class);
            if (null != cartModel.getUser()) {
                sapGenericPaymentInfoModel.setUser(cartModel.getUser());
                sapGenericPaymentInfoModel.setCode(cartModel.getUser().getUid() + "_" + UUID.randomUUID());
            }
            sapGenericPaymentInfoModel.setSapCartId(cartModel.getCode());
            modelService.save(sapGenericPaymentInfoModel);
            cartModel.setPaymentInfo(sapGenericPaymentInfoModel);
            modelService.save(cartModel);
            modelService.refresh(cartModel);
        }
    }

    @Override
    public OPFApplePayResponse getApplePayWebSession(OPFApplePayRequest opfApplePayRequest) {
        final HttpClientRequestDto<OPFApplePayResponse> request = this.createPostRequest(OPFApplePayResponse.class);
        request.setPath(getConfigurationValueForKey(OPF_APPLE_PAY_WEB_SESSION_URL));
        request.setRequestBody(opfApplePayRequest);
        return opfHttpClient.httpExchange(getConfigurationValueForKey(OPF_BASE_URL), request);
    }

    /**
     * Check top level status is valid or not
     *
     * @param status
     *         of type String
     * @return Boolean True/False
     */
    private Boolean isStatusValid(String status) {
        String paymentStatuses = getConfigurationValueForKey(OPF_PAYMENT_STATUSES);
        List<String> statusList = Arrays.asList(paymentStatuses.toUpperCase(Locale.ENGLISH).split(","));
        return statusList.contains(status.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Check custom field status is valid or not
     *
     * @param response
     *         OPFPaymentSubmitCompleteResponse
     * @return Boolean True/False
     */
    private Boolean isCustomFieldStatusValid(OPFPaymentSubmitCompleteResponse response) {
        String customFieldStatus = response.getCustomFields().stream().filter(cf -> StringUtils.equalsIgnoreCase("resultCode", cf.getKey()))
                .map(OPFPaymentAttribute::getValue).filter(String.class::isInstance).map(String.class::cast).findFirst().orElse(null);
        return customFieldStatus != null && isStatusValid(customFieldStatus);
    }

    /**
     * get configuration value for key
     *
     * @param key
     *         key
     * @return {@link String}
     * @see String
     */
    public String getConfigurationValueForKey(final String key) {
        return configurationService.getConfiguration().getString(key, StringUtils.EMPTY);
    }

}
