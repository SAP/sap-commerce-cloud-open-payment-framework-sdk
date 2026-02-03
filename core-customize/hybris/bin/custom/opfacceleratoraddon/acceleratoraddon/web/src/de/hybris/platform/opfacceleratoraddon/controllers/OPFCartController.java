/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.controllers;

import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.CheckoutPaymentFacade;
import de.hybris.platform.commercefacades.order.data.DeliveryModesData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercewebservicescommons.dto.order.SAPGuestUserRequestWsDTO;

import de.hybris.platform.facade.OPFAcceleratorFacade;
import de.hybris.platform.facade.OPFCheckoutPaymentFacade;
import de.hybris.platform.opf.dto.SAPGuestUserRequestDTO;
import de.hybris.platform.opf.dto.user.AddressWsDTO;
import de.hybris.platform.opfacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfacceleratoraddon.exception.OPFRequestValidationException;
import de.hybris.platform.opfacceleratoraddon.validation.OPFAddressValidator;
import de.hybris.platform.opfacceleratoraddon.validation.OPFDeliveryAddressValidator;
import de.hybris.platform.opfacceleratoraddon.validation.OPFGuestValidator;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/opf/cart")
public class OPFCartController extends AbstractPageController {
    private static final Logger LOG = Logger.getLogger(OPFCartController.class);
    private static final String DEFAULT_GUEST_USER_NAME = "guest";
    @Resource(name = "acceleratorCheckoutFacade")
    private CheckoutFacade checkoutFacade;
    @Resource(name = "opfCheckoutPaymentFacade")
    private OPFCheckoutPaymentFacade opfCheckoutPaymentFacade;
    @Resource(name = "userFacade")
    private UserFacade userFacade;
    @Resource(name = "opfAddressValidator")
    private OPFAddressValidator opfAddressValidator;
    @Resource(name = "opfGuestValidator")
    private OPFGuestValidator opfGuestValidator;
    @Resource(name = "opfDeliveryAddressValidator")
    private OPFDeliveryAddressValidator opfDeliveryAddressValidator;
    @Resource(name = "customerFacade")
    private CustomerFacade customerFacade;
    @Resource(name = "cartFacade")
    private CartFacade cartFacade;
    @Resource(name = "opfAcceleratorFacade")
    private OPFAcceleratorFacade opfAcceleratorFacade;
    @Resource(name = "sapCheckoutPaymentFacade")
    private CheckoutPaymentFacade checkoutPaymentFacade;
    @Resource(name = "guidCookieStrategy")
    private GUIDCookieStrategy guidCookieStrategy;

    /**
     * Creates a guest user for the cart during checkout if the current user is anonymous.
     *
     * If the user is anonymous, attempts to create a guest user with a default username. Returns HTTP status codes based on the operation
     * result: - 201 (Created) if the guest user is successfully created. - 500 (Internal Server Error) if a DuplicateUidException or any
     * other exception occurs. - 200 (OK) if the current user is not anonymous.
     *
     * @param guestUser
     *         the SAPGuestUserRequestDTO containing guest user information (optional)
     * @return an integer representing the HTTP status code indicating the result of the operation
     */
    @PostMapping(value = "/guestuser", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> createCartGuestUser(@RequestBody(required = false) final SAPGuestUserRequestDTO guestUser) {
        try {
            
            if (getUserFacade().isAnonymousUser()) {
                customerFacade.createGuestUserForCheckout(null, DEFAULT_GUEST_USER_NAME);
                return ResponseEntity.status(HttpStatus.CREATED).build();
            }
        } catch (Exception e) {
            throw new OPFAcceleratorException(String.format("Failed to save QuickBuy guest user profile: %s", guestUser.getEmail()), e);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Updates the current user's guest profile with the provided guest user details.
     *
     * This method performs the following steps: - Retrieves the current cart's customer data. - Sets the guest user's email from the
     * request. - Validates the updated customer data. - If validation errors exist, returns a 400 Bad Request with error details. -
     * Attempts to update the guest user profile in the database. - If a model saving exception occurs, returns a 500 Internal Server Error
     * with an error message. - If the current user is still a guest, sets cookies and session attributes appropriately. - Returns the
     * updated guest user info or an empty 200 OK response.
     *
     * @param guestUser
     *         the SAPGuestUserRequestWsDTO containing guest user details to update
     * @param request
     *         the HttpServletRequest from the client
     * @param response
     *         the HttpServletResponse to send cookies if necessary
     * @return a ResponseEntity containing either: - Bad Request status and validation errors if validation fails - Internal Server Error
     *         status and message if saving fails - OK status and updated guest user data if successful and guest user - OK status with
     *         empty body if successful and not a guest user
     */
    @PatchMapping(value = "/guestuser", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateCurrentUserProfile(@RequestBody final SAPGuestUserRequestWsDTO guestUser,
            final HttpServletRequest request, final HttpServletResponse response) {

        CustomerData guestCustomerData = cartFacade.getCurrentCartCustomer();
        guestCustomerData.setSapGuestUserEmail(guestUser.getEmail());
        Errors errors = new BeanPropertyBindingResult(guestCustomerData, "guestCustomerData");
        opfGuestValidator.validate(guestCustomerData, errors);
        if (errors.hasErrors())
        {
            throw new OPFRequestValidationException("Some required fields are missing or contain errors",errors);
        }
        try {
            customerFacade.updateGuestUserProfile(guestCustomerData);
        } catch (ModelSavingException e) {
            throw new OPFAcceleratorException(String.format("Failed to save QuickBuy guest user profile: %s", guestUser.getEmail()), e);
        }
        if (opfCheckoutPaymentFacade.isGuestUser()) {
            try {
                guidCookieStrategy.setCookie(request, response);
                getSessionService().setAttribute(WebConstants.ANONYMOUS_CHECKOUT, Boolean.TRUE);
                return ResponseEntity.ok(guestUser);
            } catch (Exception e) {
                throw new OPFAcceleratorException(
                        String.format("Failed to update QuickBuy guest user profile for email: %s", guestUser.getEmail()), e);
            }
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Only for Quick Buy
     * Creates a delivery address and payment address for the cart.
     *
     * Accepts a delivery address payload, maps it to AddressData, validates it and adds it to the user's
     * address book. If marked as default, it is set as the user's default address.
     *
     * @param address
     *         the address data transferred from the client
     * @return the created and saved address data
     */
    @PostMapping(value = "/addresses/delivery", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createCartAddress(@RequestBody final AddressWsDTO address) {
        AddressData addressData = opfAcceleratorFacade.mapAddressWsDTOToAddressData(address);
        final Errors errors = new BeanPropertyBindingResult(addressData, "addressData");
        opfAddressValidator.validate(addressData, errors);
        if (errors.hasErrors())
        {
            throw new OPFRequestValidationException("Some required fields are missing or contain errors",errors);
        }
        addressData.setShippingAddress(true);
        addressData.setBillingAddress(true);
        addressData.setVisibleInAddressBook(true);
        userFacade.addAddress(addressData);
        if (addressData.isDefaultAddress()) {
            userFacade.setDefaultAddress(addressData);
        }
        address.setId(addressData.getId());
        opfDeliveryAddressValidator.validate(addressData, errors);
        if (errors.hasErrors())
        {
            throw new OPFRequestValidationException("Some required fields are missing or contain errors",errors);
        }
        // For Quick Buy, same address to be used for delivery and billing address
        checkoutFacade.setDeliveryAddress(addressData);
        checkoutPaymentFacade.setPaymentAddress(addressData);
        return ResponseEntity.status(HttpStatus.CREATED).body(addressData);
    }

    /**
     * Retrieves the list of available delivery modes for the current cart.
     *
     * @return a wrapper object containing supported delivery modes
     */
    @GetMapping(value = "/deliverymodes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeliveryModesData getCartDeliveryModes() {
        final DeliveryModesData deliveryModesData = new DeliveryModesData();
        deliveryModesData.setDeliveryModes(checkoutFacade.getSupportedDeliveryModes());
        return deliveryModesData;
    }
     /*
     * Set Generic payment info model for Quick Buy flow
     *
     */
    @PostMapping(value = "/paymentinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void setCartPaymentInfo() {
        try{
            opfAcceleratorFacade.setPaymentInfoOnCart();
            checkoutFacade.prepareCartForCheckout();
        }catch(Exception ex){
            throw new OPFAcceleratorException("Failed to set payment info", ex);
        }
    }

    /**
     * Sets or replaces the delivery mode for the current cart.
     *
     * Throws an exception if the delivery mode cannot be set.
     *
     * @param deliveryModeId
     *         the identifier of the delivery mode to be set
     */
    @PutMapping(value = "/deliverymode", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void replaceCartDeliveryMode(@RequestParam(required = true) final String deliveryModeId) {
        try {
            if (StringUtils.isEmpty(deliveryModeId)) {
                throw new OPFRequestValidationException("Some required fields are missing or contain errors");
            }
            if (!checkoutFacade.setDeliveryMode(deliveryModeId)) {
                throw new OPFAcceleratorException(String.format("Failed to set delivery mode with ID: %s", deliveryModeId));
            }
        } catch (Exception e) {
            throw new OPFAcceleratorException(String.format("Failed to set delivery mode with ID: %s", deliveryModeId));
        }
    }
}

