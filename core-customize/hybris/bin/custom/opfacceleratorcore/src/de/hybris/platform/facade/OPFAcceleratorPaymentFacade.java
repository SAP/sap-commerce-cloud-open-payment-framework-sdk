/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.facade;

import com.opf.dto.cta.CTARequestDTO;
import com.opf.dto.cta.CTAResponseDTO;
import com.opf.dto.cta.OPFActiveConfigDTO;
import com.opf.dto.request.OPFApplePayRequestDTO;
import com.opf.dto.submit.OPFPaymentSubmitRequestDTO;
import com.opf.dto.submit.OPFPaymentSubmitResponseDTO;
import com.opf.dto.verify.OPFPaymentVerifyRequestDTO;
import com.opf.dto.verify.OPFPaymentVerifyResponseDTO;
import de.hybris.platform.opf.data.OPFInitiatePaymentData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteResponseData;
import de.hybris.platform.opf.data.response.OPFApplePayResponse;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionRequest;
import de.hybris.platform.opf.dto.OPFPaymentVerifyResponse;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteRequest;

/**
 * Open Payment Framework Accelerator SDK Facade
 */
public interface OPFAcceleratorPaymentFacade {

    /**
     * Get CTA script rendering response
     *
     * @param ctaRequestWsDTO
     *         ctaRequestWsDTO
     * @return {@link CTAResponseDTO}
     * @see CTAResponseDTO
     */
    CTAResponseDTO getCTAResponse(final CTARequestDTO ctaRequestWsDTO);

    /**
     * Get active configurations
     *
     * @return {@link OPFActiveConfigDTO}
     * @see OPFActiveConfigDTO
     */
    OPFActiveConfigDTO getActiveConfigurations();

    /**
     * Initiate OPF Payment Session and get the response
     *
     * @param paymentRequest
     *         Payment request data
     * @return OPFInitiatePaymentData as response
     */
    OPFInitiatePaymentData getInitiatePaymentResponse(final OPFInitiatePaymentSessionRequest paymentRequest);

    /**
     * Submit payment
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

    OPFPaymentSubmitResponseDTO submitPayment(final OPFPaymentSubmitRequestDTO opfPaymentSubmitRequestDTO, final String paymentSessionId,
            final String ipAddress);

    /**
     * verify payment
     *
     *@param opfPaymentVerifyRequestDTO
     *         opfPaymentVerifyRequestDTO
     * @return {@link OPFPaymentVerifyResponseDTO}
     * @see OPFPaymentVerifyResponse
     */
    OPFPaymentVerifyResponse verifyPayment(final OPFPaymentVerifyRequestDTO opfPaymentVerifyRequestDTO);


    /**
     * Submit and complete payment and get the response
     * @param paymentRequest OPFPaymentSubmitCompleteRequest
     * @return OPFPaymentSubmitCompleteResponseData
     */
    OPFPaymentSubmitCompleteResponseData getCompletedPaymentResponse(final OPFPaymentSubmitCompleteRequest paymentRequest);

    /**
     * set payment info on cart
     *
     */
    void setPaymentInfoOnCart();

    /**
     * Get apple pay web session
     *
     * @param opfApplePayRequestDTO opfApplePayRequestDTO
     * @return {@link OPFApplePayResponse}
     * @see OPFApplePayResponse
     */
    OPFApplePayResponse getApplePayWebSession(final OPFApplePayRequestDTO opfApplePayRequestDTO);
}
