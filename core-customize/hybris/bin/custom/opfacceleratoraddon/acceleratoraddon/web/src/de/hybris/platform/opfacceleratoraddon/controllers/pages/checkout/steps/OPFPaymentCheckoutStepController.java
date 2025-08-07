/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.controllers.pages.checkout.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opf.dto.submit.OPFPaymentSubmitRequestDTO;
import com.opf.dto.submit.OPFPaymentSubmitResponseDTO;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SopPaymentDetailsForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.CheckoutPaymentFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.commerceservices.enums.CountryType;
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.controllers.OpfacceleratoraddonControllerConstants;
import de.hybris.platform.facade.OPFAcceleratorPaymentFacade;
import de.hybris.platform.facade.OPFAddressFacade;
import de.hybris.platform.facade.OPFCheckoutPaymentFacade;
import de.hybris.platform.opf.data.OPFInitiatePaymentData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteResponseData;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionRequest;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteRequest;
import de.hybris.platform.opf.dto.user.AddressWsDTO;
import de.hybris.platform.opfacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfacceleratoraddon.util.OPFAcceleratorUtil;
import de.hybris.platform.opfservices.client.CCAdapterClientException;

import de.hybris.platform.util.OPFAcceleratorCoreUtil;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import de.hybris.platform.opfacceleratoraddon.validation.OPFAddressValidator;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.hybris.platform.util.Sanitizer.sanitize;

@Controller
@RequestMapping(value = "/checkout/multi/opf-payment")
public class OPFPaymentCheckoutStepController extends AbstractCheckoutStepController {
    private static final Logger LOG = Logger.getLogger(OPFPaymentCheckoutStepController.class);
    private static final String OPF_PAYMENT = "opf-payment";
    @Resource(name = "opfAcceleratorPaymentFacade")
    private OPFAcceleratorPaymentFacade opfAcceleratorPaymentFacade;
    @Resource(name = "commerceCommonI18NService")
    private CommerceCommonI18NService commerceCommonI18NService;
    @Resource(name = "opfAddressValidator")
    private OPFAddressValidator opfAddressValidator;
    @Resource(name = "sapCheckoutPaymentFacade")
    private CheckoutPaymentFacade checkoutPaymentFacade;
    @Resource(name = "opfCheckoutPaymentFacade")
    private OPFCheckoutPaymentFacade opfCheckoutPaymentFacade;
    @Resource(name = "opfAddressFacade")
    private OPFAddressFacade opfAddressFacade;

    @GetMapping(value = "/choose")
    @RequireHardLogIn
    @Override
    @PreValidateCheckoutStep(checkoutStep = OPF_PAYMENT)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        getCheckoutFacade().setDeliveryModeIfAvailable();
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        populateBillingAddress(model, cartData);
        model.addAttribute("cartData", cartData);
        model.addAttribute("opfPaymentMethods", opfAcceleratorPaymentFacade.getActiveConfigurations());
        this.prepareDataForPage(model);
        if (cartData.getSapBillingAddress() == null) {
            opfCheckoutPaymentFacade.setBillingAddressFromShipping(cartData.getDeliveryAddress());
        }
        opfAcceleratorPaymentFacade.setPaymentInfoOnCart();
        getCheckoutFacade().prepareCartForCheckout();
        final ContentPageModel multiCheckoutSummaryPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, multiCheckoutSummaryPage);
        setUpMetaDataForContentPage(model, multiCheckoutSummaryPage);
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.deliveryMethod.breadcrumb"));
        model.addAttribute("metaRobots", "noindex,nofollow");
        setCheckoutStepLinksForModel(model, getCheckoutStep());
        return OpfacceleratoraddonControllerConstants.Views.Pages.MultiStepCheckout.ChooseOpfPaymentPage;
    }

    /**
     * Populates the billing address into the model using the cart data.
     *
     * @param model
     *         the Spring MVC model to populate with attributes for the view
     * @param cartData
     *         the CartData which contains the billing address information
     */
    private void populateBillingAddress(final Model model, CartData cartData) {
        populateCountryAndRegionData(model, cartData);
        final SopPaymentDetailsForm sopPaymentDetailsForm = new SopPaymentDetailsForm();
        final AddressData deliveryAddress = cartData.getDeliveryAddress();
        if (deliveryAddress != null) {
            model.addAttribute("deliveryAddress", deliveryAddress);
        }
        model.addAttribute("sopPaymentDetailsForm", sopPaymentDetailsForm);
    }

    /**
     * Initiates payment session.
     *
     * @param paymentRequest
     *         Payment request.
     * @return OPFInitiatePaymentData
     * @throws CCAdapterClientException
     *         If there's a client error.
     * @throws IllegalArgumentException
     *         If arguments do not meet requirements.
     */
    @PostMapping(value = "/payment-initiate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @RequireHardLogIn
    public OPFInitiatePaymentData initiatePaymentSession(@RequestBody final OPFInitiatePaymentSessionRequest paymentRequest) {
        try {
            // Check if the necessary payment details are present
            boolean hasValidPaymentDetails = StringUtils.isNotEmpty(paymentRequest.getAccountId()) && StringUtils.isNotEmpty(
                    paymentRequest.getResultURL()) && StringUtils.isNotEmpty(paymentRequest.getCancelURL());

            if (hasValidPaymentDetails) {
                return opfAcceleratorPaymentFacade.getInitiatePaymentResponse(paymentRequest);
            } else {
                throw new OPFAcceleratorException("Missing required fields");
            }
        } catch (CCAdapterClientException exec) {
            throw new OPFAcceleratorException("Failed to initiate payment session due to a client exception", exec);
        } catch (Exception exec) {
            throw new OPFAcceleratorException("Failed to initiate payment session due to unexpected error", exec);
        }
    }

    /**
     * Complete submit payment.
     *
     * @param paymentRequest
     *         Payment request.
     * @return OPFPaymentSubmitCompleteResponseData
     * @throws CCAdapterClientException
     *         If there's a client error.
     * @throws IllegalArgumentException
     *         If arguments do not meet requirements.
     */
    @PostMapping(value = "/{paymentSessionId}/payment-submit-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @RequireHardLogIn
    public OPFPaymentSubmitCompleteResponseData submit(final HttpServletRequest request, @PathVariable final String paymentSessionId,
            @RequestBody final OPFPaymentSubmitCompleteRequest paymentRequest) {
        try {
            if (StringUtils.isEmpty(paymentSessionId)) {
                LOG.error("Invalid payment session ID or payment request data");
                throw new OPFAcceleratorException("Invalid payment session ID or payment request data");
            }
            return opfAcceleratorPaymentFacade.getCompletedPaymentResponse(paymentRequest);
        } catch (CCAdapterClientException exec) {
            throw new OPFAcceleratorException("Client exception occurred during payment submit complete", exec);
        } catch (HttpClientErrorException e) {
            LOG.error(String.format("HTTP Error Response: %s", e.getResponseBodyAsString()));
            return handleHttpClientError(e);
        } catch (Exception exec) {
            throw new OPFAcceleratorException("Exception occurred during payment submit complete", exec);
        }
    }

    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    @GetMapping(value = "/next")
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(OPF_PAYMENT);
    }

    /**
     * submit payment
     *
     * @param request
     *         request
     * @param paymentSessionId
     *         paymentSessionId
     * @param opfPaymentSubmitRequestDTO
     *         opfPaymentSubmitRequestDTO
     * @return {@link OPFPaymentSubmitResponseDTO}
     * @see OPFPaymentSubmitResponseDTO
     */
    @PostMapping(value = {"/{paymentSessionId}/payment-submit", "/payment-submit"})
    @ResponseBody
    @RequireHardLogIn
    public OPFPaymentSubmitResponseDTO submitPayment(final HttpServletRequest request,
            @PathVariable(value = "paymentSessionId", required = false) final String paymentSessionId,
            @RequestBody final OPFPaymentSubmitRequestDTO opfPaymentSubmitRequestDTO) {

        if (opfPaymentSubmitRequestDTO == null ||
              (!OPFAcceleratorCoreUtil.isQuickBuy(opfPaymentSubmitRequestDTO.getPaymentMethod())
                    && StringUtils.isEmpty(paymentSessionId))) {
            LOG.error("Invalid payment session ID or payment request data");
            throw new IllegalArgumentException("Invalid payment session ID or payment request data");
        }
        try {
            String ipAddress = request.getRemoteAddr();
            return opfAcceleratorPaymentFacade.submitPayment(opfPaymentSubmitRequestDTO, paymentSessionId, ipAddress);
        } catch (CCAdapterClientException exec) {
            throw new OPFAcceleratorException("Client error during payment submit", exec);
        } catch (Exception exec) {
            throw new OPFAcceleratorException("Error during payment submit", exec);
        }
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler({ OPFAcceleratorException.class })
    public ErrorListWsDTO handleOpfCheckoutException(final Throwable ex) {
        LOG.error(sanitize(ex.getMessage()), ex);
        return OPFAcceleratorUtil.handleErrorInternal(ex);
    }

    /**
     * Handles an {@link HttpClientErrorException} by logging the error response and attempting to parse specific fields from the response
     * body JSON. It maps the JSON content into an instance of {@link OPFPaymentSubmitCompleteResponseData}.
     *
     * If parsing the JSON fails, a generic reason code will be set in the response data.
     *
     * @param e
     *         the {@link HttpClientErrorException} thrown during an HTTP client request
     * @return an instance of {@link OPFPaymentSubmitCompleteResponseData} containing parsed error details, or a default object with a
     *         parsing error message if JSON parsing fails
     */
    private OPFPaymentSubmitCompleteResponseData handleHttpClientError(HttpClientErrorException e) {
        LOG.error(String.format("HTTP Error Response: %s", e.getResponseBodyAsString()));
        OPFPaymentSubmitCompleteResponseData responseData = new OPFPaymentSubmitCompleteResponseData();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(e.getResponseBodyAsString());

            if (json.has("status")) {
                responseData.setStatus(json.get("status").asText());
            }
            if (json.has("reasonCode")) {
                responseData.setReasonCode(json.get("reasonCode").asText());
            }
            if (json.has("paymentSessionId")) {
                responseData.setPaymentSessionId(json.get("paymentSessionId").asText());
            }
        } catch (Exception parseEx) {
            LOG.error("Failed to parse error response JSON", parseEx);
            responseData.setReasonCode("Unable to parse error message.");
        }
        return responseData;
    }

    /**
     * Populates the given {@code model} with supported billing countries, regions for the current country ISO, and the current country ISO
     * code. This method retrieves the current locale from {@code commerceCommonI18NService}, obtains the current country ISO code, and
     * then: Adds supported billing countries to the model attribute "supportedCountries". Adds regions for the current country ISO to the
     * model attribute "regions". Adds the current country ISO to the model attribute "country". If any data is unavailable, empty lists or
     * empty strings are used as fallbacks.
     *
     * @param model
     *         the Spring MVC model to populate attributes for the view
     */
    private void populateCountryAndRegionData(Model model, CartData cartData) {
        String currentCountryIso = Optional.ofNullable(cartData.getDeliveryAddress()).map(AddressData::getCountry)
                .map(CountryData::getIsocode).orElse("");
        // Fetch supported billing countries
        List<CountryData> supportedBillingCountries = getCheckoutFacade().getCountries(CountryType.BILLING);
        model.addAttribute("supportedCountries", (supportedBillingCountries != null) ? supportedBillingCountries : Collections.emptyList());

        // Fetch regions for current country (if available)
        if (StringUtils.isNotEmpty(currentCountryIso)) {
            List<RegionData> regions = getI18NFacade().getRegionsForCountryIso(currentCountryIso);
            model.addAttribute("regions", (regions != null) ? regions : Collections.emptyList());
            model.addAttribute("country", currentCountryIso);
        } else {
            model.addAttribute("regions", Collections.emptyList());
            model.addAttribute("country", "");
        }
    }

    /**
     * Update the Billing address of the cart
     *
     * @param addressWsDTO
     *         Address DTO
     * @return the response entity
     */
    @PutMapping(value = "/{cartId}/addresses/billing", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "updateBillingAddress", summary = "Create or update the billing address of the cart.", description = "Creates or updates the billing address of the cart.")
    @RequireHardLogIn
    public ResponseEntity<Map<String, Object>> updateBillingAddress(
            @Parameter(description = "Address object.", required = true) @RequestBody final AddressWsDTO addressWsDTO) {
        AddressData addressData = opfAddressFacade.mapAddressWsDTOToAddressData(addressWsDTO);
        final Errors errors = new BeanPropertyBindingResult(addressData, "addressData");
        opfAddressValidator.validate(addressData, errors);
        Map<String, Object> errorList = OPFAcceleratorUtil.handleErrors(errors);
        if (!errorList.isEmpty()) {
            return ResponseEntity.badRequest().body(errorList);
        }
        opfCheckoutPaymentFacade.removeShippingFromPaymentAddress(addressData);
        checkoutPaymentFacade.setPaymentAddress(addressData);
        getCheckoutFacade().prepareCartForCheckout();
        return ResponseEntity.ok(Collections.emptyMap());
    }
}