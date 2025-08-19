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

/**
 * Apple Pay (Quick Buy) integration for SAP Commerce Cloud.
 * Entry point: `applePayQuickBuy(config)`
 * Combines Apple Pay JS API with platform APIs for a complete purchase flow.
 *
 * (c) SAP SE 2025
 */

// ---- STATE ---- //
let applePaySession;
let isDeviceSupported = false; // Device Apple Pay support state
let countryCode;
let paymentInProgress = false; // To disable double interactions
var transactionDetails = QuickBuyDefaults.applePayInitialTransactionDetails;
const applePayApiVersion = 3; // ApplePay JS API Version

// ---- MAIN ENTRY ---- //

/**
 * Entry point for initializing Apple Pay
 * @param {Object} config - Merchant config and context
 */
function applePayQuickBuy(config) {
  const enabledConfig = OpfUtils.getEnabledApplePayConfig(config);
  if (isApplePaySupported(enabledConfig)) {
    countryCode = enabledConfig.countryCode;
    renderApplePayContainer();
  }
}

// ---- Apple Pay Environment Checks ---- //

/**
 * Determines whether Apple Pay is enabled and ready for use on this device & merchant.
 * @param {Object} enabledConfig
 * @returns {Observable<boolean>} emits `true` if available with active card for merchant
 */
function isApplePaySupported(enabledConfig) {
  return checkDeviceSupported() && supportsVersion(applePayApiVersion)
    ? canMakePaymentsWithActiveCard(enabledConfig.merchantid)
    : rxjs.of(false);
}

/**
 * Checks if Apple Pay is available on the current device/browser.
 * @returns {boolean}
 */
function checkDeviceSupported() {
  applePaySession = getApplePaySession();
  if (applePaySession) {
    isDeviceSupported = applePaySession.canMakePayments();
  }
  return isDeviceSupported;
}

/**
 * Gets reference to the ApplePaySession class from the window, if available.
 * @returns {Function|undefined}
 */
function getApplePaySession() {
  return window["ApplePaySession"];
}

/**
 * Checks if desired JS API version is supported.
 * @param {number} version
 * @returns {boolean}
 */
function supportsVersion(version) {
  try {
    return isDeviceSupported && applePaySession.supportsVersion(version);
  } catch (err) {
    return false;
  }
}

/**
 * Return true if Apple Pay session is possible for this device.
 * @returns {boolean}
 */
function canMakePayments() {
  try {
    return isDeviceSupported && applePaySession.canMakePayments();
  } catch (err) {
    return false;
  }
}

/**
 * Checks if user has an active card for the merchant (async).
 * @param {string} merchantId
 * @returns {Observable<boolean>}
 */
function canMakePaymentsWithActiveCard(merchantId) {
  return isDeviceSupported
    ? rxjs.from(applePaySession.canMakePaymentsWithActiveCard(merchantId))
    : rxjs.of(false);
}

// ---- UI RENDERING AND LAUNCH ---- //

/**
 * Renders the Apple Pay button inside the #cx-opf-quick-buy-buttons container.
 * Installs event handler to initiate the Quick Buy Apple Pay process.
 */
function renderApplePayContainer() {
  const applePayDiv = document.createElement("div");
  applePayDiv.className = "apple-pay-button";
  document.getElementById("cx-opf-quick-buy-buttons").appendChild(applePayDiv);
  applePayDiv.addEventListener("click", initApplePay);
}

/**
 * Handler for Apple Pay button. Orchestrates the Quick Buy flow.
 * @returns {Observable | void }
 */
function initApplePay() {
  if (paymentInProgress) {
    return rxjs.throwError(() => new Error("Apple Pay is already in progress"));
  }
  paymentInProgress = true;

  const transactionInput = {
    cart: cartData,
    countryCode,
  };

  setApplePayRequestConfig(transactionInput)
    .pipe(
      rxjs.switchMap((request) => {
        return applePayStart({
          request,
          onValidateMerchant: handleValidation,
          onShippingContactSelected: handleShippingContactSelected,
          onPaymentMethodSelected: handlePaymentMethodSelected,
          onShippingMethodSelected: handleShippingMethodSelected,
          onPaymentAuthorized: handlePaymentAuthorized,
        });
      }),
      rxjs.take(1),
      rxjs.catchError((error) => rxjs.throwError(() => error)),
      rxjs.finalize(() => {
        deleteUserAddresses();
        paymentInProgress = false;
      })
    )
    .subscribe({
      next: (value) => {
        console.log("Apple Pay flow completed:", value);
      },
      error: (err) => {
        console.error("Apple Pay flow error:", err);
      },
    });
}

// ---- APPLE PAY SESSION LIFECYCLE ---- //

/**
 * Starts an ApplePaySession and manages its events as RxJS observable.
 * Triggers merchant validation, shipping/payment selection, and authorization.
 * @param {Object} config - Apple Pay session config and event handlers.
 * @returns {Observable}
 */
function applePayStart(config) {
  return new rxjs.Observable((observer) => {
    let session;
    try {
      session = createSession(config.request);
      if (!session) throw new Error("Unable to start ApplePaySession");
    } catch (err) {
      observer.error(err);
      return;
    }

    const handleUnspecifiedError = (error) => {
      if (session && typeof session.abort === "function") session.abort();
      observer.error(error);
    };

    // Merchant validation handler
    session.addEventListener("validatemerchant", (event) => {
      config
        .onValidateMerchant(event)
        .pipe(rxjs.take(1))
        .subscribe({
          next: (merchantSession) => {
            session.completeMerchantValidation(merchantSession);
          },
          error: handleUnspecifiedError,
        });
    });

    // Payment cancelled by user
    session.addEventListener("cancel", () => {
      observer.error({ type: "PAYMENT_CANCELLED" });
    });

    // Payment method chosen by user
    if (typeof config.onPaymentMethodSelected === "function") {
      session.addEventListener("paymentmethodselected", (event) => {
        config
          .onPaymentMethodSelected(event)
          .pipe(rxjs.take(1))
          .subscribe({
            next: (update) => {
              session.completePaymentMethodSelection(update);
            },
            error: handleUnspecifiedError,
          });
      });
    }

    // Shipping contact chosen by user (if relevant)
    if (
      typeof config.onShippingContactSelected === "function" &&
      !isShippingTypePickup(config)
    ) {
      session.addEventListener("shippingcontactselected", (event) => {
        config
          .onShippingContactSelected(event)
          .pipe(rxjs.take(1))
          .subscribe({
            next: (update) => {
              session.completeShippingContactSelection(update);
            },
            error: handleUnspecifiedError,
          });
      });
    }

    // Shipping method chosen by user (if relevant)
    if (
      typeof config.onShippingMethodSelected === "function" &&
      !isShippingTypePickup(config)
    ) {
      session.addEventListener("shippingmethodselected", (event) => {
        config
          .onShippingMethodSelected(event)
          .pipe(rxjs.take(1))
          .subscribe({
            next: (update) => {
              session.completeShippingMethodSelection(update);
            },
            error: handleUnspecifiedError,
          });
      });
    }

    // Final payment authorization received from Apple Pay sheet
    session.addEventListener("paymentauthorized", (event) => {
      config
        .onPaymentAuthorized(event)
        .pipe(rxjs.take(1))
        .subscribe({
          next: (authResult) => {
            session.completePayment(authResult);
            // Success, error with validation
            if (!authResult?.errors?.length) {
              observer.next(authResult);
              observer.complete();
            } else {
              handleUnspecifiedError({
                message: authResult.errors[0]?.message,
              });
            }
          },
          error: handleUnspecifiedError,
        });
    });

    session.begin();
  });
}

/**
 * Creates an ApplePaySession object with the API version and request.
 * @param {Object} paymentRequest
 * @returns {ApplePaySession|undefined}
 */
function createSession(paymentRequest) {
  return isDeviceSupported
    ? new applePaySession(applePayApiVersion, paymentRequest)
    : undefined;
}

// ---- MERCHANT VALIDATION ---- //

/**
 * Handle the 'validatemerchant' event from ApplePaySession.
 * Performs back-end session validation.
 * @param {Event} event
 * @returns {Observable<Object>} Merchant session object for Apple Pay
 */
function handleValidation(event) {
  return validateOpfAppleSession(event);
}

/**
 * Performs back-end merchant validation.
 * @param {Event} event
 * @returns {Observable<Object>} Valid merchant session object (from OpfApis)
 */
function validateOpfAppleSession(event) {
  return handleCartGuestUser().pipe(
    rxjs.switchMap(() => {
      const verificationRequest = {
        validationUrl: event.validationURL,
        initiative: "web",
        initiativeContext: window.location?.hostname,
        cartId: cartData?.code,
      };
      return verifyApplePaySession(verificationRequest);
    })
  );
}

/**
 * API call to verify merchant session with Opf endpoint.
 * @param {Object} request
 * @returns {Observable<Object>}
 */
function verifyApplePaySession(request) {
  return OpfApis.getApplePayWebSession(request);
}

// ---- APPLE PAY EVENT HANDLERS ---- //

/**
 * Called when user selects payment method.
 * Updates Apple Pay form total if needed.
 * @param {Event} _event
 * @returns {Observable<Object>}
 */
function handlePaymentMethodSelected(_event) {
  const result = updateApplePayForm({ ...transactionDetails.total });
  return rxjs.of(result);
}

/**
 * Called when user selects a shipping method.
 * Calls Opf API to set shipping mode and update total.
 * @param {Event} _event
 * @returns {Observable<Object>}
 */
function handleShippingMethodSelected(_event) {
  const result = updateApplePayForm({ ...transactionDetails.total });

  return OpfApis.setDeliveryMode(_event.shippingMethod.identifier).pipe(
    rxjs.switchMap(() => {
      if (!cartData?.totalPrice?.value) {
        return rxjs.throwError(() => new Error("Total Price not available"));
      }
      result.newTotal.amount = cartData.totalPrice.value.toString();
      transactionDetails.total.amount = cartData.totalPrice.value.toString();
      return rxjs.of(result);
    })
  );
}

/**
 * Called when Apple Pay authorizes payment (user accepts).
 * Orchestrates back-end place order, returns status to Apple Pay JS API.
 * @param {Event} event
 * @returns {Observable<Object>}
 */
function handlePaymentAuthorized(event) {
  const result = { status: statusSuccess() };
  let orderSuccess;
  return placeOrderAfterPayment(event.payment).pipe(
    map((success) =>
      success ? result : { ...result, status: statusFailure() }
    ),
    catchError((error) =>
      of({
        ...result,
        status: statusFailure(),
        errors: [getApplePayFormWithError(error?.message ?? "Payment error")],
      })
    )
  );
}

/**
 * Handles updating cart, guest email, and final payment submission with Opf after user authorizes the payment.
 * @param {Object} applePayPayment - ApplePay JS payment object with contact & token
 * @returns {Observable<boolean>}
 */
function placeOrderAfterPayment(applePayPayment) {
  if (!applePayPayment) return of(false);

  const { shippingContact, billingContact } = applePayPayment;
  if (!billingContact) throw new Error("Error: empty billingContact");

  if (
    transactionDetails.deliveryInfo?.type ===
      OpfQuickBuyDeliveryType.SHIPPING &&
    !shippingContact
  ) {
    throw new Error("Error: empty shippingContact");
  }

  // Pick up vs shipping
  const deliveryTypeHandlingObservable =
    transactionDetails.deliveryInfo?.type === OpfQuickBuyDeliveryType.PICKUP
      ? OpfApis.setDeliveryMode(
          OpfQuickBuyDeliveryType.PICKUP.toLocaleLowerCase()
        )
      : OpfApis.setDeliveryAddress(
          OpfUtils.convertAppleToOpfAddress(shippingContact)
        ).pipe(tap((address) => recordDeliveryAddress(address.id)));

  // Add guest email, submit payment to backend; wrap OpfPaymentSubmit as Observable
  return deliveryTypeHandlingObservable.pipe(
    switchMap(() =>
      shippingContact?.emailAddress
        ? OpfApis.updateCartGuestUserEmail(shippingContact.emailAddress)
        : of(true)
    ),
    switchMap(() => {
      const encryptedToken = btoa(
        JSON.stringify(applePayPayment.token.paymentData)
      );
      const submitRequest = {
        additionalData: [],
        paymentSessionId: "",
        paymentMethod: OpfQuickBuyProviderType.APPLE_PAY,
        encryptedToken,
        callbacks: {
          submitSuccess: function () {},
          submitPending: function () {},
          submitFailure: function () {},
        },
      };

      return new rxjs.Observable((observer) => {
        opfPaymentSubmit(submitRequest, submitRequest.callbacks, "");
      });
    })
  );
}

/**
 * Handles when user selects or changes Apple Pay shipping address.
 * Updates shipping address, reloads supported methods, and recalculates total.
 * @param {Event} _event
 * @returns {Observable<Object>}
 */
function handleShippingContactSelected(_event) {
  const partialAddress = OpfUtils.convertAppleToOpfAddress(
    _event.shippingContact,
    true
  );
  const result = updateApplePayForm({ ...transactionDetails.total });

  return setApplePayDeliveryAddress(partialAddress).pipe(
    rxjs.tap((addrId) => recordDeliveryAddress(addrId)),
    rxjs.switchMap(() => OpfApis.getSupportedDeliveryModes()),
    rxjs.take(1),
    rxjs.map((modes) => {
      if (!modes.length) {
        return rxjs.of({
          ...result,
          errors: [
            getApplePayFormWithError(
              "No shipment methods available for this delivery address"
            ),
          ],
        });
      }
      // Map into Apple Pay shipping methods structure
      const newShippingMethods = modes.map((mode) => ({
        identifier: mode.code,
        label: mode.name,
        amount: (mode.deliveryCost?.value).toFixed(2),
        detail: mode.description ?? mode.name,
      }));
      result.newShippingMethods = newShippingMethods;
      return result;
    }),
    rxjs.switchMap(() => getCurrentCartTotalPrice()),
    rxjs.switchMap(() => {
      const price = cartData.totalPrice.value;
      if (!price) {
        return rxjs.throwError(() => new Error("Total Price not available"));
      }
      transactionDetails.total.amount = price.toString();
      result.newTotal.amount = price.toString();
      return rxjs.of(result);
    })
  );
}

/**
 * Sets (backs API) the shipping address and returns the new address ID.
 * @param {Object} deliveryAddress
 * @returns {Observable<string>} Address ID
 */
function setApplePayDeliveryAddress(deliveryAddress) {
  return OpfApis.setDeliveryAddress(deliveryAddress);
}

/**
 * Record list of address IDs used in session, for possible cleanup.
 * @param {string} addrId
 */
function recordDeliveryAddress(addrId) {
  if (!transactionDetails.addressIds?.includes(addrId)) {
    transactionDetails.addressIds?.push(addrId);
  }
}

// ---- TRANSACTION FORM & DETAILS ---- //

/**
 * Returns Apple Pay update structure for form total.
 * @param {Object} total { amount, label }
 * @returns {Object}
 */
function updateApplePayForm(total) {
  return {
    newTotal: {
      amount: total.amount,
      label: total.label,
    },
  };
}

/**
 * Prepares and returns observable with ApplePay request config for session.
 * @param {Object} transactionInput { cart, countryCode }
 * @returns {Observable<Object>} ApplePaySession request object
 */
function setApplePayRequestConfig(transactionInput) {
  transactionDetails = initTransactionDetails(transactionInput);
  const countryCode = transactionInput?.countryCode || "";
  const initialRequest = {
    ...QuickBuyDefaults.defaultApplePayCardParameters,
    currencyCode: transactionDetails.total.currency,
    total: {
      amount: transactionDetails.total.amount,
      label: transactionDetails.total.label,
    },
    countryCode,
  };

  return rxjs
    .forkJoin({
      deliveryInfo: rxjs.of(getTransactionDeliveryInfo()),
      merchantName: rxjs.of(getMerchantName()),
    })
    .pipe(
      rxjs.switchMap(({ deliveryInfo, merchantName }) => {
        transactionDetails.total.label = merchantName;
        initialRequest.total.label = merchantName;
        transactionDetails.deliveryInfo = deliveryInfo;
        return rxjs.of(undefined);
      }),
      rxjs.map((opfQuickBuyDeliveryInfo) => {
        if (!opfQuickBuyDeliveryInfo) {
          return initialRequest;
        }
        transactionDetails.deliveryInfo = opfQuickBuyDeliveryInfo;
        return initialRequest;
      })
    );
}

/**
 * Builds initial transaction details (cart or product flow).
 * @param {Object} transactionInput { cart }
 * @returns {Object} transactionDetails
 */
function initTransactionDetails(transactionInput) {
  transactionDetails = {
    ...QuickBuyDefaults.applePayInitialTransactionDetails,
    addressIds: [],
  };

  if (transactionInput?.cart) {
    transactionDetails = {
      ...transactionDetails,
      context: OpfQuickBuyLocation.CART,
      cart: transactionInput.cart,
      total: {
        amount: `${transactionInput.cart.totalPrice?.value}`,
        label: `${transactionInput.cart.code}`,
        currency: transactionInput.cart?.totalPrice?.currencyIso,
      },
    };
  }

  return transactionDetails;
}

// ---- UTILITY / HELPER FUNCTIONS ---- //

/**
 * Remove addresses created during this Apple Pay quick buy, for cleanup.
 */
function deleteUserAddresses() {
  if (
    Array.isArray(transactionDetails.addressIds) &&
    transactionDetails.addressIds.length
  ) {
    transactionDetails.addressIds = [];
  }
}

/**
 * Get ApplePay API status code for success.
 * @returns {number|string}
 */
function statusSuccess() {
  return isDeviceSupported ? applePaySession.STATUS_SUCCESS : 1;
}

/**
 * Get ApplePay API status code for failure.
 * @returns {number|string}
 */
function statusFailure() {
  return isDeviceSupported ? applePaySession.STATUS_FAILURE : 1;
}

/**
 * Apple Pay error response for form.
 * @param {string} message
 * @param {string} code
 * @returns {{code: string, message: string}}
 */
function getApplePayFormWithError(message, code = "unknown") {
  return { code, message };
}

/**
 * Determines if the shipping type is 'STORE_PICKUP'.
 * @param {Object} config
 * @returns {boolean}
 */
function isShippingTypePickup(config) {
  return config.request.shippingType === ApplePayShippingType.STORE_PICKUP;
}

// ---- END ---- //
