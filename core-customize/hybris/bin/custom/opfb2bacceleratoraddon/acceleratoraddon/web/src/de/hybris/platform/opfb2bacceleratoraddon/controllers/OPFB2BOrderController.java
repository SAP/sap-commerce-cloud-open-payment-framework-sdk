/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfb2bacceleratoraddon.controllers;

import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.opfb2bacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfb2bacceleratoraddon.exception.OPFRequestValidationException;
import de.hybris.platform.opfb2bacceleratoraddon.validation.OPFB2BOrderCartValidator;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping(value = "/checkout/multi/summary/opf-b2b-payment")
public class OPFB2BOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OPFB2BOrderController.class);
    @Resource(name = "cartFacade")
    private CartFacade cartFacade;
    @Resource(name = "opfB2BOrderCartValidator")
    private OPFB2BOrderCartValidator opfB2BOrderCartValidator;
    @Resource(name = "b2bCheckoutFacade")
    private CheckoutFacade b2bCheckoutFacade;
    @Resource(name = "messageSource")
    private MessageSource messageSource;
    @Resource(name = "i18nService")
    private I18NService i18nService;

    /**
     * Places an order based on the current user's cart and agreement to terms. This method handles POST requests to the /placeOrder
     * endpoint.
     *
     * @param termsChecked
     *         indicates whether the user has accepted the terms and conditions
     * @return a response entity containing the result of the order placement
     */
    @RequestMapping(value = "/placeOrder", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @RequireHardLogIn
    public ResponseEntity<?> placeOrder(@RequestParam final boolean termsChecked) {
        if (!termsChecked) {
            throw new OPFRequestValidationException("Some required fields are missing or contain errors");
        }
        final CartData cartData = b2bCheckoutFacade.getCheckoutCart();
        final Errors errors = new BeanPropertyBindingResult(cartData, "sessionCart");
        opfB2BOrderCartValidator.validate(cartData, errors);
        if (errors.hasErrors()) {
            throw new OPFRequestValidationException("Some required fields are missing or contain errors", errors);
        }
        List<CartModificationData> modifications;
        try {
            modifications = cartFacade.validateCartData();
        } catch (final CommerceCartModificationException e) {
            LOGGER.error("Failed to validate cart", e);
            throw new OPFRequestValidationException(
                    getMessageSource().getMessage("checkout.error.cart.invalid", null, getI18nService().getCurrentLocale()));
        }
        if (!CollectionUtils.isEmpty(modifications)) {
            throw new OPFRequestValidationException(
                    getMessageSource().getMessage("checkout.error.cart.invalid", null, getI18nService().getCurrentLocale()));
        }
        try {
            if (getSessionCart().getSapGenericPaymentInfo() != null) {
                OrderData orderData = b2bCheckoutFacade.placeOrder();
                return ResponseEntity.ok(orderData);
            } else {
                throw new OPFRequestValidationException(getMessageSource().getMessage("checkout.sapGenericPaymentInfo.not.present", null,
                        getI18nService().getCurrentLocale()));
            }
        } catch (final Exception e) {
            LOGGER.error("Failed to place Order", e);
            throw new OPFAcceleratorException(
                    getMessageSource().getMessage("checkout.placeOrder.failed", null, getI18nService().getCurrentLocale()));
        }
    }

    protected CartData getSessionCart() {
        return b2bCheckoutFacade.getCheckoutCart();
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public I18NService getI18nService() {
        return i18nService;
    }

}
