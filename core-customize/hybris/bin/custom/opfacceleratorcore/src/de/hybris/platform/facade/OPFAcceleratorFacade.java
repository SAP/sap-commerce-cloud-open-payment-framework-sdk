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
import com.opf.order.data.OPFB2BPaymentTypeData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.opf.data.OPFInitiatePaymentData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteResponseData;
import de.hybris.platform.opf.data.response.OPFApplePayResponse;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionRequest;
import de.hybris.platform.opf.dto.OPFPaymentVerifyResponse;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteRequest;
import de.hybris.platform.opf.dto.user.AddressWsDTO;

import java.util.List;

/**
 * Open Payment Framework Accelerator SDK Facade
 */
public interface OPFAcceleratorFacade {

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
    AddressData mapAddressWsDTOToAddressData(AddressWsDTO source);
    /**
     * Retrieves a list of active B2B payment configurations.
     * This method is typically used to fetch payment types that are available
     * and active for B2B transactions in the current context.
     *
     * @return a list of {@link B2BPaymentTypeData} objects representing the active B2B payment configurations.
     */
   List<OPFB2BPaymentTypeData> getB2BActiveConfigurations();

    /**
     * Sets payment information on the cart for an account.
     * This method is used to associate payment details with the cart
     * in the context of an account-based transaction.
     */
    void setPaymentInfoOnCartForAccount();

    /**
     * Clears the SAP Payment Option ID from the current session or cart.
     * This method is useful for resetting or removing any previously set payment option identifiers.
     */
    void  clearSapPaymentOptionId();
}
