/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.validation;

import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * Validator for checking cart and checkout state before placing an order.
 */
public class OPFOrderCartValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OPFOrderCartValidator.class);

    @Resource(name = "checkoutFlowFacade")
    private CheckoutFlowFacade checkoutFlowFacade;
    @Resource(name = "checkoutFacade")
    private CheckoutFacade checkoutFacade;
    private MessageSource messageSource;
    private I18NService i18nService;

    public OPFOrderCartValidator(final I18NService i18NService, final MessageSource messageSource) {
        this.i18nService = i18NService;
        this.messageSource = messageSource;
    }

    /**
     * Validates the current cart and checkout state for placing an order.
     *
     * @return ErrorList containing validation errors, or null if none found.
     */
    @Override
    public boolean supports(final Class<?> clazz) {
        return CartData.class.equals(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {

        Locale currentLocale = getI18nService().getCurrentLocale();

        if (!checkoutFlowFacade.hasValidCart()) {
            errors.reject(getMessageSource().getMessage("checkout.error.cart.invalid", null, currentLocale));
        }

        if (checkoutFlowFacade.hasNoDeliveryAddress()) {
            errors.reject(getMessageSource().getMessage("checkout.deliveryAddress.notSelected", null, currentLocale));
        }

        if (checkoutFlowFacade.hasNoDeliveryMode()) {
            errors.reject(getMessageSource().getMessage("checkout.deliveryMethod.notSelected", null, currentLocale));
        }
        CartData cartData = (CartData) target;
        if (cartData.getSapGenericPaymentInfo() == null) {
            errors.reject(getMessageSource().getMessage("checkout.paymentMethod.notSelected", null, currentLocale));
        }

        if (!checkoutFacade.containsTaxValues()) {
            LOGGER.error("Cart {} is missing tax values. Tax calculation failed.", cartData.getCode());
            errors.reject(getMessageSource().getMessage("checkout.error.tax.missing", null, currentLocale));
        }

        if (!cartData.isCalculated()) {
            LOGGER.error("Cart {} is not marked as calculated.", cartData.getCode());
            errors.reject(getMessageSource().getMessage("checkout.error.cart.notcalculated", null, currentLocale));
        }
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
