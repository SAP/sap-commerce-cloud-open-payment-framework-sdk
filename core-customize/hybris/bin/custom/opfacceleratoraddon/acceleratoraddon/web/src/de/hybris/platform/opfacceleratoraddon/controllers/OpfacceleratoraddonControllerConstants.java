/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.controllers;


/**
 *  opfacceleratoraddon controller constants
 *
 */
public interface OpfacceleratoraddonControllerConstants {
    static final String ADDON_PREFIX = "addon:/opfacceleratoraddon/";

    interface Views {
        interface Pages {
            interface MultiStepCheckout {
                String ChooseOpfPaymentPage = ADDON_PREFIX + "pages/checkout/multi/opfPaymentPage";
                String OpfVerifyPaymentPage = ADDON_PREFIX + "pages/checkout/multi/opfVerifyPaymentPage";
            }
            interface Product {
                String CustomOpfProductPage = ADDON_PREFIX + "pages/product/opfProductPage";
            }
        }
    }
}
