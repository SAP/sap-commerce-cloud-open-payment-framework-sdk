/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.validation;

import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator for checking cart and checkout state before placing an order.
 */
public class OPFOrderCartValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OPFOrderCartValidator.class);

    @Resource(name = "checkoutFlowFacade")
    private CheckoutFlowFacade checkoutFlowFacade;
    @Resource(name = "checkoutFacade")
    private CheckoutFacade checkoutFacade;

    /**
     * Validates the current cart and checkout state for placing an order.
     *
     * @param termsChecked
     *         Whether the terms and conditions checkbox is accepted.
     * @return ErrorList containing validation errors, or null if none found.
     */
    public List<String> validate(final boolean termsChecked) {
        List<String> errors = new ArrayList<>();

        if (!checkoutFlowFacade.hasValidCart()) {
            errors.add("checkout.error.cart.invalid");
        }
        if (checkoutFlowFacade.hasNoDeliveryAddress()) {
            errors.add("checkout.deliveryAddress.notSelected");
        }
        if (checkoutFlowFacade.hasNoDeliveryMode()) {
            errors.add("checkout.deliveryMethod.notSelected");
        }
        CartData cartData = checkoutFacade.getCheckoutCart();
        if (cartData.getSapGenericPaymentInfo() == null) {
            errors.add("checkout.paymentMethod.notSelected");
        }
        if (!termsChecked) {
            errors.add("checkout.error.terms.not.accepted");
        }

        if (!checkoutFacade.containsTaxValues()) {
            LOGGER.error("Cart {} is missing tax values. Tax calculation failed.", cartData.getCode());
            errors.add("checkout.error.tax.missing");
        }
        if (!cartData.isCalculated()) {
            LOGGER.error("Cart {} is not marked as calculated.", cartData.getCode());
            errors.add("checkout.error.cart.notcalculated");
        }
        return errors.isEmpty() ? null : errors;
    }

}
