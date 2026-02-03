/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.controllers.pages.checkout.steps;

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
import de.hybris.platform.opfacceleratoraddon.controllers.OpfacceleratoraddonControllerConstants;

import de.hybris.platform.facade.OPFAcceleratorFacade;
import de.hybris.platform.facade.OPFCheckoutPaymentFacade;
import de.hybris.platform.opf.data.OPFInitiatePaymentData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteResponseData;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionRequest;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteRequest;
import de.hybris.platform.opf.dto.user.AddressWsDTO;
import de.hybris.platform.opfacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfacceleratoraddon.exception.OPFRequestValidationException;
import de.hybris.platform.opfacceleratoraddon.validation.OPFAddressValidator;
import de.hybris.platform.opfservices.client.CCAdapterClientException;
import de.hybris.platform.util.OPFAcceleratorCoreUtil;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(value = "/checkout/multi/opf-payment")
public class OPFPaymentCheckoutStepController extends AbstractCheckoutStepController {
    private static final Logger LOG = Logger.getLogger(OPFPaymentCheckoutStepController.class);
    private static final String OPF_PAYMENT = "opf-payment";
    @Resource(name = "opfAcceleratorFacade")
    private OPFAcceleratorFacade opfAcceleratorFacade;
    @Resource(name = "commerceCommonI18NService")
    private CommerceCommonI18NService commerceCommonI18NService;
    @Resource(name = "opfAddressValidator")
    private OPFAddressValidator opfAddressValidator;
    @Resource(name = "sapCheckoutPaymentFacade")
    private CheckoutPaymentFacade checkoutPaymentFacade;
    @Resource(name = "opfCheckoutPaymentFacade")
    private OPFCheckoutPaymentFacade opfCheckoutPaymentFacade;


    @GetMapping(value = "/choose")
    @RequireHardLogIn
    @Override
    @PreValidateCheckoutStep(checkoutStep = OPF_PAYMENT)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        getCheckoutFacade().setDeliveryModeIfAvailable();
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        populateBillingAddress(model, cartData);
        model.addAttribute("cartData", cartData);
        model.addAttribute("opfPaymentMethods", opfAcceleratorFacade.getActiveConfigurations());
        this.prepareDataForPage(model);
        if (cartData.getSapBillingAddress() == null) {
            opfCheckoutPaymentFacade.setBillingAddressFromShipping(cartData.getDeliveryAddress());
        }
        opfAcceleratorFacade.setPaymentInfoOnCart();
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
    public OPFInitiatePaymentData initiatePaymentSession(@RequestBody final OPFInitiatePaymentSessionRequest paymentRequest,
            final HttpServletResponse response) {
        try {
            // Check if the necessary payment details are present
            boolean hasValidPaymentDetails = StringUtils.isNotEmpty(paymentRequest.getAccountId()) && StringUtils.isNotEmpty(
                    paymentRequest.getResultURL()) && StringUtils.isNotEmpty(paymentRequest.getCancelURL());

            if (hasValidPaymentDetails) {
                OPFInitiatePaymentData opfInitiatePaymentData = opfAcceleratorFacade.getInitiatePaymentResponse(paymentRequest);
                response.setStatus(HttpServletResponse.SC_CREATED);
                return opfInitiatePaymentData;
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new OPFRequestValidationException("Some required fields are missing or contain errors");
            }
        } catch (Exception ex) {
            throw new OPFAcceleratorException("Failed to initiate payment session due to unexpected error", ex);
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
    public OPFPaymentSubmitCompleteResponseData submit(final HttpServletRequest request, final HttpServletResponse response,@PathVariable final String paymentSessionId,
            @RequestBody final OPFPaymentSubmitCompleteRequest paymentRequest) {
        try {
            if (StringUtils.isEmpty(paymentSessionId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                LOG.error("Invalid payment session ID or payment request data");
                throw new OPFRequestValidationException("Some required fields are missing or contain errors");
            }
            OPFPaymentSubmitCompleteResponseData opfPaymentSubmitCompleteResponseData = opfAcceleratorFacade.getCompletedPaymentResponse(
                    paymentRequest);
            response.setStatus(HttpServletResponse.SC_CREATED);
            return opfPaymentSubmitCompleteResponseData;
        } catch (Exception exec) {
            LOG.error(String.format("HTTP Error Response: %s", exec));
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
    public OPFPaymentSubmitResponseDTO submitPayment(final HttpServletRequest request,final HttpServletResponse response,
            @PathVariable(value = "paymentSessionId", required = false) final String paymentSessionId,
            @RequestBody final OPFPaymentSubmitRequestDTO opfPaymentSubmitRequestDTO) {

        if (opfPaymentSubmitRequestDTO == null ||
              (!OPFAcceleratorCoreUtil.isQuickBuy(opfPaymentSubmitRequestDTO.getPaymentMethod())
                    && StringUtils.isEmpty(paymentSessionId))) {
            LOG.error("Invalid payment session ID or payment request data");
            throw new OPFRequestValidationException("Some required fields are missing or contain errors");
        }
        try {
            String ipAddress = request.getRemoteAddr();
            OPFPaymentSubmitResponseDTO  opfPaymentSubmitResponseDTO = opfAcceleratorFacade.submitPayment(opfPaymentSubmitRequestDTO, paymentSessionId, ipAddress);
            response.setStatus(HttpServletResponse.SC_CREATED);
            return opfPaymentSubmitResponseDTO;
        } catch (Exception exec) {
            throw new OPFAcceleratorException("Error during payment submit", exec);
        }
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
        AddressData addressData = opfAcceleratorFacade.mapAddressWsDTOToAddressData(addressWsDTO);
        final Errors errors = new BeanPropertyBindingResult(addressData, "addressData");
        opfAddressValidator.validate(addressData, errors);
        if (errors.hasErrors())
        {
            throw new OPFRequestValidationException("Some required fields are missing or contain errors",errors);
        }
        opfCheckoutPaymentFacade.removeShippingFromPaymentAddress(addressData);
        checkoutPaymentFacade.setPaymentAddress(addressData);
        getCheckoutFacade().prepareCartForCheckout();
        return ResponseEntity.ok(Collections.emptyMap());
    }
}