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

// AppConstants: General constants used across the application
const AppConstants = {
  OCC_USER_ID_ANONYMOUS: "anonymous",
  OCC_USER_ID_GUEST: "guest",
  OPF_GOOGLE_PAY_PROVIDER_NAME: "googlePay",
  OPF_QUICK_BUY_DEFAULT_MERCHANT_NAME: "Store",
  PAYMENT_GATEWAY: "PAYMENT_GATEWAY",
  EMAIL_PATTERN:
    /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
  // Regular expression for basic email validation
  DEFAULT_FIELD_VALUE: "[FIELD_NOT_SET]",
};

// Expose AppConstants globally for cross-file access
window.AppConstants = AppConstants;

/**
 * Enum-like objects defining key string constants
 * for quick buy payment providers, purchase locations,
 * delivery types, payment methods, submit statuses, and error types.
 */

// Payment providers supported in quick buy flow
window.OpfQuickBuyProviderType = {
  APPLE_PAY: "APPLE_PAY",
  GOOGLE_PAY: "GOOGLE_PAY",
};

// Locations in the store context where quick buy can happen
window.OpfQuickBuyLocation = {
  CART: "CART",
  PRODUCT: "PRODUCT",
};

// Delivery types available for quick buy orders
window.OpfQuickBuyDeliveryType = {
  SHIPPING: "SHIPPING",
  PICKUP: "PICKUP",
};

// Supported payment methods identifiers
window.OpfPaymentMethod = {
  CREDIT_CARD: "CREDIT_CARD",
};

// Possible status values after payment submission
window.OpfPaymentSubmitStatus = {
  REJECTED: "REJECTED",
  ACCEPTED: "ACCEPTED",
  PENDING: "PENDING",
  DELAYED: "DELAYED",
};

// Types of payment errors that can occur during processing
window.OpfPaymentErrorType = {
  EXPIRED: "EXPIRED",
  INSUFFICIENT_FUNDS: "INSUFFICIENT_FUNDS",
  CREDIT_LIMIT: "CREDIT_LIMIT",
  INVALID_CARD: "INVALID_CARD",
  INVALID_CVV: "INVALID_CVV",
  LOST_CARD: "LOST_CARD",
  PAYMENT_REJECTED: "PAYMENT_REJECTED",
  PAYMENT_CANCELLED: "PAYMENT_CANCELLED",
  STATUS_NOT_RECOGNIZED: "STATUS_NOT_RECOGNIZED",
};

window.ApplePayShippingType = {
  SHIPPING: "shipping",
  DELIVERY: "delivery",
  STORE_PICKUP: "storePickup",
  SERVICE_PICKUP: "servicePickup",
};
