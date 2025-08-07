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
import de.hybris.platform.facade.OPFAcceleratorPaymentFacade;
import de.hybris.platform.opf.dto.error.ErrorData;
import de.hybris.platform.opf.dto.error.ErrorListData;
import de.hybris.platform.opfacceleratoraddon.validation.OPFOrderCartValidator;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
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
    @Resource(name = "opfAcceleratorPaymentFacade")
    private OPFAcceleratorPaymentFacade opfAcceleratorPaymentFacade;
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
        List<String> errorCodes = opfOrderCartValidator.validate(termsChecked);
        if (!CollectionUtils.isEmpty(errorCodes)) {
            return handleError(errorCodes, HttpStatus.BAD_REQUEST);
        }
        List<CartModificationData> modifications;
        try {
            modifications = cartFacade.validateCartData();
        } catch (final CommerceCartModificationException e) {
            LOGGER.error("Failed to validate cart", e);
            return handleError("checkout.error.cart.invalid", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (!CollectionUtils.isEmpty(modifications)) {
            return handleError("checkout.error.cart.invalid", HttpStatus.BAD_REQUEST);
        }
        try {
            if (getSessionCart().getSapGenericPaymentInfo() != null) {
                OrderData orderData = checkoutFacade.placeOrder();
                return ResponseEntity.ok(orderData);
            } else {
                return handleError("checkout.sapGenericPaymentInfo.not.present", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (final Exception e) {
            LOGGER.error("Failed to place Order", e);
            return handleError("checkout.placeOrder.failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle the errors
     *
     * @param errorInput
     *         Error Inputs
     * @param status
     *         Http status code
     * @return ResponseEntity
     */
    private ResponseEntity<?> handleError(Object errorInput, HttpStatus status) {
        List<String> errorCodes = new ArrayList<>();
        if (errorInput instanceof String) {
            errorCodes = Collections.singletonList((String) errorInput);
        } else if (errorInput instanceof List) {
            errorCodes = (List<String>) errorInput;
        }
        List<ErrorData> errorList = errorCodes.stream().map(code -> {
            ErrorData error = new ErrorData();
            error.setType(ERROR);
            error.setMessage(getMessageSource().getMessage(code, null, getI18nService().getCurrentLocale()));
            return error;
        }).collect(Collectors.toList());
        ErrorListData errorListData = new ErrorListData();
        errorListData.setErrors(errorList);
        return ResponseEntity.status(status).body(errorListData);
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
