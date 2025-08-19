/*
 * [y] hybris Platform
 *
 * Copyright (c) 2025 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */

// QuickBuyDefaults - Default configuration objects for Google Pay and Apple Pay integrations
(function (window) {
  // Ensure global QuickBuyDefaults object exists (without overwriting if already defined)
  const QuickBuyDefaults = window.QuickBuyDefaults || {};

  /**
   * Initial Google Pay Payment Request object.
   * These settings configure the Google Pay API version, merchant info, and payment callbacks.
   */
  QuickBuyDefaults.initialGooglePaymentRequest = {
    apiVersion: 2,
    apiVersionMinor: 0,
    callbackIntents: [
      "PAYMENT_AUTHORIZATION",
      "SHIPPING_ADDRESS",
      "SHIPPING_OPTION",
    ],
    merchantInfo: {
      merchantName: "Store",
    },
    shippingOptionRequired: true,
    shippingAddressRequired: true,
    shippingAddressParameters: {
      phoneNumberRequired: false,
    },
    emailRequired: true,
  };

  /**
   * Default card payment parameters for Google Pay.
   * Specifies accepted card authorization methods and networks, plus billing address requirements.
   */
  QuickBuyDefaults.defaultGooglePayCardParameters = {
    allowedAuthMethods: ["PAN_ONLY", "CRYPTOGRAM_3DS"],
    allowedCardNetworks: [
      "AMEX",
      "DISCOVER",
      "INTERAC",
      "JCB",
      "MASTERCARD",
      "VISA",
    ],
    billingAddressRequired: true,
    billingAddressParameters: {
      format: "FULL",
    },
  };

  /**
   * Initial transaction details used in Google Pay flow.
   * Contains product info, quantity, delivery type, address, and payment total.
   */
  QuickBuyDefaults.initialTransactionDetails = {
    context: "PRODUCT",
    product: undefined,
    cart: undefined,
    quantity: 0,
    deliveryInfo: {
      type: "SHIPPING",
      pickupDetails: undefined,
    },
    addressIds: [],
    total: {
      label: "",
      amount: "",
      currency: "",
    },
  };

  /**
   * Initial transaction details tailored for Apple Pay integration.
   * Uses constants from your `OpfQuickBuy` enums where applicable.
   */
  QuickBuyDefaults.applePayInitialTransactionDetails = {
    context: OpfQuickBuyLocation.PRODUCT,
    product: undefined,
    cart: undefined,
    quantity: 0,
    addressIds: [],
    total: {
      label: "Store",
      amount: "",
      currency: "",
    },
    deliveryInfo: {
      type: OpfQuickBuyDeliveryType.SHIPPING,
      pickupDetails: undefined,
    },
  };

  /**
   * Google Pay initial transaction info.
   * Used by Google Pay API to specify total pricing details.
   */
  QuickBuyDefaults.initialTransactionInfo = {
    totalPrice: "0.00",
    totalPriceStatus: "ESTIMATED",
    currencyCode: "USD",
  };

  /**
   * Default error structure for OPF payment errors.
   * Can be extended or replaced with localization strings.
   */
  QuickBuyDefaults.opfDefaultPaymentError = {
    statusText: "Payment Error",
    message: "opfPayment.errors.proceedPayment", // Presumably a key for localization
    status: -1, // Custom status code for errors
    type: "", // Error type/category, if needed
  };

  /**
   * Apple Pay default card parameters.
   * Defines capabilities and supported card networks, plus required contact fields.
   */
  QuickBuyDefaults.defaultApplePayCardParameters = {
    shippingMethods: [],
    merchantCapabilities: ["supports3DS"], // ApplePay merchant capabilities, usually includes 3D Secure
    supportedNetworks: ["visa", "masterCard", "amex", "discover"],
    requiredShippingContactFields: ["email", "name", "postalAddress"],
    requiredBillingContactFields: ["email", "name", "postalAddress"],
  };

  // Assign back to global window if it didn't exist to begin with
  if (!window.QuickBuyDefaults) {
    window.QuickBuyDefaults = QuickBuyDefaults;
  }
})(window);
