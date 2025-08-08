/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.validation;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.springframework.context.MessageSource;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

/**
 *  OPF delivery address validator
 *
 */
public class OPFDeliveryAddressValidator implements Validator {
    private static final String FIELD_REQUIRED = "field.required";
    private static final String DELIVERY_ADDRESS_INVALID = "delivery.address.invalid";
    private static final String ADDRESS_ID = "id";
    private final DeliveryService deliveryService;
    private final CartService cartService;
    private final I18NService i18NService;
    private final MessageSource messageSource;

    /**
     * Constructs a new OPFDeliveryAddressValidator with the specified services.
     *
     * @param deliveryService The service used to handle delivery-related operations.
     * @param cartService The service used to manage cart-related operations.
     * @param i18NService The service used for internationalization and locale-specific operations.
     * @param messageSource The source for resolving messages, supporting internationalization.
     */
    public OPFDeliveryAddressValidator(DeliveryService deliveryService, CartService cartService, I18NService i18NService,
            MessageSource messageSource) {
        this.deliveryService = deliveryService;
        this.cartService = cartService;
        this.i18NService = i18NService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(final Class clazz) {
        return AddressData.class.isAssignableFrom(clazz);
    }

    /**
     * validate
     *
     * @param target target
     * @param errors errors
     */
    @Override
    public void validate(final Object target, final Errors errors) {
        final AddressData addressData = (AddressData) target;
        Assert.notNull(errors, "Errors object must not be null");

        if (addressData == null || addressData.getId() == null || addressData.getId().trim().isEmpty()) {
            //create ERROR
            errors.rejectValue(ADDRESS_ID, messageSource.getMessage(FIELD_REQUIRED, null, i18NService.getCurrentLocale()));
            return;
        }

        if (cartService.hasSessionCart()) {
            final CartModel sessionCartModel = cartService.getSessionCart();

            final List<AddressModel> addresses = deliveryService.getSupportedDeliveryAddressesForOrder(sessionCartModel, false);

            boolean isDeliveryAddressValid = addresses.stream() //
                    .anyMatch(address -> addressData.getId().equals(address.getPk().toString()));

            if (isDeliveryAddressValid) {
                return;
            }
        }
        // delivery is not supported. Create Error
        errors.rejectValue(ADDRESS_ID, messageSource.getMessage(DELIVERY_ADDRESS_INVALID, null, i18NService.getCurrentLocale()));
    }
}
