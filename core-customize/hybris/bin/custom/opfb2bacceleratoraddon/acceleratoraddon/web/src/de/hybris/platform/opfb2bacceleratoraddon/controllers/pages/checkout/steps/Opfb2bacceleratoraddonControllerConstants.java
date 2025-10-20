/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfb2bacceleratoraddon.controllers.pages.checkout.steps;

/**
 * Constants for opfb2bacceleratoraddon controllers.
 */
public interface Opfb2bacceleratoraddonControllerConstants {
    static final String ADDON_PREFIX = "addon:/opfb2bacceleratoraddon/";
    static final String B2B_ADDON_PREFIX = "addon:/b2bacceleratoraddon/";

    interface Views {
        interface Pages {
            interface MultiStepCheckout {
                String ChoosePaymentTypePage = ADDON_PREFIX + "pages/checkout/multi/choosePaymentTypePage";
                String ChooseOpfPaymentPage = ADDON_PREFIX + "pages/checkout/multi/opfPaymentPage";
                String CheckoutSummaryPage = ADDON_PREFIX + "pages/checkout/multi/checkoutSummaryPage";
                String OpfVerifyPaymentPage = ADDON_PREFIX + "pages/checkout/multi/opfVerifyPaymentPage";
            }
        }
    }
}
