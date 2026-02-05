/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.controllers;

import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.facade.OPFAcceleratorFacade;
import de.hybris.platform.opf.dto.error.ErrorData;
import de.hybris.platform.opf.dto.error.ErrorListData;
import de.hybris.platform.opfacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfacceleratoraddon.exception.OPFRequestValidationException;
import de.hybris.platform.opfacceleratoraddon.validation.OPFOrderCartValidator;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/checkout/multi/summary/opf-payment")
public class OPFOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OPFOrderController.class);
    private static final String ERROR = "ERROR";
    @Resource(name = "cartFacade")
    private CartFacade cartFacade;
    @Resource(name = "opfOrderCartValidator")
    private OPFOrderCartValidator opfOrderCartValidator;
    @Resource(name = "checkoutFacade")
    private CheckoutFacade checkoutFacade;
    @Resource(name = "opfAcceleratorFacade")
    private OPFAcceleratorFacade opfAcceleratorFacade;
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
        final CartData cartData = checkoutFacade.getCheckoutCart();
        final Errors errors = new BeanPropertyBindingResult(cartData, "sessionCart");
        opfOrderCartValidator.validate(cartData, errors);
        if (errors.hasErrors())
        {
            throw new OPFRequestValidationException("Some required fields are missing or contain errors",errors);
        }
        List<CartModificationData> modifications;
        try {
            modifications = cartFacade.validateCartData();
        } catch (final CommerceCartModificationException e) {
            LOGGER.error("Failed to validate cart", e);
            throw new OPFRequestValidationException(getMessageSource().getMessage("checkout.error.cart.invalid", null, getI18nService().getCurrentLocale()));
        }
        if (!CollectionUtils.isEmpty(modifications)) {
            throw new OPFRequestValidationException(getMessageSource().getMessage("checkout.error.cart.invalid", null, getI18nService().getCurrentLocale()));
        }
        try {
            if (getSessionCart().getSapGenericPaymentInfo() != null) {
                OrderData orderData = checkoutFacade.placeOrder();
                return ResponseEntity.ok(orderData);
            } else {
                throw new OPFRequestValidationException(getMessageSource().getMessage("checkout.sapGenericPaymentInfo.not.present", null, getI18nService().getCurrentLocale()));
            }
        } catch (final Exception e) {
            LOGGER.error("Failed to place Order", e);
            throw new OPFAcceleratorException(getMessageSource().getMessage("checkout.placeOrder.failed", null, getI18nService().getCurrentLocale()));
        }
    }

    protected CartData getSessionCart() {
        return checkoutFacade.getCheckoutCart();
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public I18NService getI18nService() {
        return i18nService;
    }

    public void setI18nService(I18NService i18nService) {
        this.i18nService = i18nService;
    }
}
