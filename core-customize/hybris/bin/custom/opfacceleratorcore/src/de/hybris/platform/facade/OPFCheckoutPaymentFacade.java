/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.facade;

import de.hybris.platform.commercefacades.user.data.AddressData;

/**
 * OPFCheckoutPaymentFacade defines the contract for handling shipping and billing address
 * during the checkout process.
 * This interface acts as an abstraction layer between the checkout process and
 * the underlying payment systems, allowing for modular and flexible payment/addresses handling.
 */
public interface OPFCheckoutPaymentFacade {
    /**
     * Set Billing Address as a Shipping Address
     * @param address
     *         Shipping Address Data
     */
    void setBillingAddressFromShipping(AddressData address);

    /**
     * Remove Shipping Address from the cart Payment Address
     * @param address
     *         Billing Address data
     */
    void removeShippingFromPaymentAddress(AddressData address);

    /**
     * Determines whether the user associated with the current session cart is a guest user.
     * @return Boolean flag true/false
     */
    boolean isGuestUser();
}
