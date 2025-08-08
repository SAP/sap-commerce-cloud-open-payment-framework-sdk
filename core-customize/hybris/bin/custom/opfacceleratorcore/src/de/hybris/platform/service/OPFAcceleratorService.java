/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.service;

import de.hybris.platform.cta.request.OPFPaymentCTARequest;
import de.hybris.platform.cta.response.OPFPaymentCTAResponse;
import de.hybris.platform.data.response.OPFActiveConfigResponse;
import de.hybris.platform.opf.data.OPFInitiatePaymentSessionRequestData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteRequestData;
import de.hybris.platform.opf.data.request.OPFApplePayRequest;
import de.hybris.platform.opf.data.request.OPFPaymentSubmitRequest;
import de.hybris.platform.opf.data.response.OPFApplePayResponse;
import de.hybris.platform.opf.data.response.OPFPaymentSubmitResponse;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionResponse;

import de.hybris.platform.opf.dto.OPFPaymentVerifyRequest;
import de.hybris.platform.opf.dto.OPFPaymentVerifyResponse;

import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteResponse;

/**
 * Open Payment Framework Accelerator SDK Service
 */
public interface OPFAcceleratorService {
    /**
     * Get CTA scripting response
     *
     * @param request
     *         request
     * @return {@link OPFPaymentCTAResponse}
     * @see OPFPaymentCTAResponse
     */
    OPFPaymentCTAResponse getCTAResponse(OPFPaymentCTARequest request);

    /**
     * Get active configurations
     *
     * @return {@link OPFActiveConfigResponse}
     * @see OPFActiveConfigResponse
     */
    OPFActiveConfigResponse getActiveConfigurations();

    /**
     * Initiate OPF Payment session
     *
     * @param paymentRequest
     *         Payment request data
     * @return OPFInitiatePaymentSessionResponse as response
     */
    OPFInitiatePaymentSessionResponse getInitiatePaymentResponse(final OPFInitiatePaymentSessionRequestData paymentRequest);

    /**
     * OPF Payment Submit
     * @param opfPaymentSubmitRequest payment submit request data
     * @param isQuickBuy flag to identify if this submit request is for Quick Buy payments
     * @return OPFPaymentSubmitResponse Response as received from API
     */
    OPFPaymentSubmitResponse submitPayment(OPFPaymentSubmitRequest opfPaymentSubmitRequest, boolean isQuickBuy);

    /**
     * verify payment
     *
     * @param opfPaymentVerifyRequest
     *         opfPaymentVerifyRequest
     * @return {@link OPFPaymentVerifyResponse}
     * @see OPFPaymentVerifyResponse
     */
    OPFPaymentVerifyResponse verifyPayment(OPFPaymentVerifyRequest opfPaymentVerifyRequest);

    /**
     * Submit and Completed OPF Payment
     *
     * @param paymentRequest
     *         Payment request data
     * @return OPFPaymentSubmitCompleteResponse as response
     */
    OPFPaymentSubmitCompleteResponse getCompletedPaymentResponse(final OPFPaymentSubmitCompleteRequestData paymentRequest);

    /**
     * Return valid payment status
     * @param response OPFPaymentSubmitCompleteResponse
     * @return Boolean True/False
     */
    Boolean validatePaymentStatus(OPFPaymentSubmitCompleteResponse response);

    /**
     * set payment info
     *
     */
    void setPaymentInfo();

    /**
     * get apple pay web session
     *
     * @param request request
     * @return {@link OPFApplePayResponse}
     * @see OPFApplePayResponse
     */
    OPFApplePayResponse getApplePayWebSession(OPFApplePayRequest request) ;
}
