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

// Configuration for Google Payment Client, defaulting to test environment
var googlePaymentClientOptions = {
  environment: "TEST",
};

// Reference to Google Payment Client instance (initialized later)
var googlePaymentClient;

// References to initial Google Pay payment request and transaction details defaults
var googlePaymentRequest = QuickBuyDefaults.initialGooglePaymentRequest;
var transactionDetails = QuickBuyDefaults.initialTransactionDetails;

/**
 * Initializes Google Pay Quick Buy flow with the given config
 * Sets allowed payment methods, updates client, and conditionally renders button if Google Pay is ready.
 * Handles errors by logging to console.
 *
 * @param {object} config Active quick buy configuration
 */
function googlePayQuickBuy(config) {
  setAllowedPaymentMethodsConfig(config);
  updateGooglePaymentClient();

  isReadyToPay()
    .then(function (response) {
      if (response.result) {
        const buttonContainer = document.getElementById(
          "googlePayButtonContainer"
        );
        if (buttonContainer) {
          renderPaymentButton(buttonContainer);
        } else {
          console.warn("Google Pay button container not found.");
        }
      }
    })
    .catch(function (err) {
      console.error("Error checking Google Pay readiness:", err);
    });
}

/**
 * Instantiates or updates the Google Payment Client instance using current options.
 */
function updateGooglePaymentClient() {
  googlePaymentClient = new google.payments.api.PaymentsClient(
    googlePaymentClientOptions
  );
}

/**
 * Configures `googlePaymentClientOptions` and `googlePaymentRequest` related to delivery type
 * Adjusts callbacks and requirements based on delivery type (pickup/shipping).
 * Updates Google Payment client after changes.
 *
 * @param {string} deliveryType - Type of the delivery (SHIPPING or PICKUP)
 * @param {string} merchantName - Merchant name shown in the payment sheet
 */
function setGooglePaymentRequestConfig(deliveryType, merchantName) {
  if (deliveryType === OpfQuickBuyDeliveryType.PICKUP) {
    googlePaymentClientOptions = {
      ...googlePaymentClientOptions,
      paymentDataCallbacks: {
        onPaymentAuthorized: handlePaymentCallbacks().onPaymentAuthorized,
      },
    };
    googlePaymentRequest = {
      ...QuickBuyDefaults.initialGooglePaymentRequest,
      shippingAddressRequired: false,
      shippingOptionRequired: false,
      callbackIntents: ["PAYMENT_AUTHORIZATION"],
    };
  } else {
    googlePaymentClientOptions = {
      ...googlePaymentClientOptions,
      paymentDataCallbacks: handlePaymentCallbacks(),
    };
    googlePaymentRequest = { ...QuickBuyDefaults.initialGooglePaymentRequest };
  }
  // Update merchant name dynamically
  googlePaymentRequest.merchantInfo.merchantName = merchantName;

  updateGooglePaymentClient();
}

/**
 * Returns the current Google Payment Client instance.
 * @returns {PaymentsClient}
 */
function getClient() {
  return googlePaymentClient;
}

/**
 * Checks if Google Pay is ready to pay with the current payment request.
 * Returns a Promise resolving to the result.
 * @returns {Promise<Object>}
 */
function isReadyToPay() {
  return googlePaymentClient.isReadyToPay(googlePaymentRequest);
}

/**
 * Builds a new transactionInfo object based on cart pricing details.
 * Returns undefined if pricing info is missing.
 * @param {object} cart - Cart data object
 * @returns {object|undefined} TransactionInfo object or undefined
 */
function getNewTransactionInfo(cart) {
  const priceInfo = cart?.totalPriceWithTax;
  if (priceInfo?.value && priceInfo?.currencyIso) {
    return {
      totalPrice: priceInfo.value.toString(),
      currencyCode: priceInfo.currencyIso.toString(),
      totalPriceStatus: "FINAL",
    };
  }
  return undefined;
}

/**
 * Sets the delivery address during the Google Pay payment flow.
 * Uses the OpfUtils converter and invokes OpfApis to set the address on server.
 * Associates the returned address ID for session tracking.
 *
 * Returns an observable emitting the address or pickup indicator.
 *
 * @param {object} address - Raw shipping address from paymentDataResponse
 * @returns {Observable}
 */
function setDeliveryAddress(address) {
  const deliveryAddress = OpfUtils.convertAddress(address);
  if (
    transactionDetails?.deliveryInfo?.type === OpfQuickBuyDeliveryType.SHIPPING
  ) {
    return OpfApis.setDeliveryAddress(deliveryAddress).pipe(
      rxjs.tap((address) => {
        associateAddressId(address.id ?? "");
      })
    );
  } else {
    // For pickup, no address to set; just emit pickup delivery type
    return rxjs.of(OpfQuickBuyDeliveryType.PICKUP);
  }
}

/**
 * Sets the delivery mode on the cart, respecting delivery type.
 * Returns observable completing after setting mode or emits undefined if none set.
 *
 * @param {string} mode - Delivery mode code
 * @param {string} type - Delivery type (SHIPPING or PICKUP)
 * @returns {Observable}
 */
function setDeliveryMode(mode, type) {
  cartData.delivery = { code: mode };

  if (type === OpfQuickBuyDeliveryType.PICKUP) {
    mode = OpfQuickBuyDeliveryType.PICKUP.toLowerCase();
  }
  if (!mode && type === OpfQuickBuyDeliveryType.SHIPPING) {
    return rxjs.of(undefined);
  }
  return mode && verifyShippingOption(mode)
    ? OpfApis.setDeliveryMode(mode)
    : rxjs.of(undefined);
}

/**
 * Returns the currently selected delivery mode from the cart as an observable.
 * @returns {Observable<Object>}
 */
function getSelectedDeliveryMode() {
  return rxjs.from(Promise.resolve(cartData.delivery));
}

/**
 * Main handler for active cart transaction flow.
 * Configures transaction context, handles guest users, sets delivery mode,
 * and updates Google payment request transactionInfo.
 * Returns an observable that completes when setup is done.
 * @returns {Observable}
 */
function handleActiveCartTransaction() {
  transactionDetails.context = OpfQuickBuyLocation.CART;

  return handleCartGuestUser().pipe(
    rxjs.switchMap(() => {
      const deliveryInfo = getTransactionDeliveryInfo();
      const merchantName = getMerchantName();

      transactionDetails.deliveryInfo = deliveryInfo;
      setGooglePaymentRequestConfig(deliveryInfo.type, merchantName);

      return setDeliveryMode(undefined, deliveryInfo.type).pipe(
        // Update transactionInfo for Google Pay request after delivery mode is set
        rxjs.tap(() => {
          transactionDetails.cart = cartData;
          googlePaymentRequest.transactionInfo = {
            totalPrice:
              cartData.totalPrice.value ||
              QuickBuyDefaults.initialTransactionInfo.totalPrice,
            currencyCode:
              cartData.totalPrice.currencyIso ||
              QuickBuyDefaults.initialTransactionInfo.currencyCode,
            totalPriceStatus:
              QuickBuyDefaults.initialTransactionInfo.totalPriceStatus,
          };
        })
      );
    })
  );
}

/**
 * Starts the payment transaction flow.
 * Initializes cart data, transaction details, sets up active cart transaction,
 * and launches Google Pay payment UI.
 */
function initTransaction() {
  // Reset transaction details with a fresh copy of defaults and empty address list
  transactionDetails = {
    ...QuickBuyDefaults.initialTransactionDetails,
    addressIds: [],
  };

  handleActiveCartTransaction().subscribe(
    () => {
      googlePaymentClient.loadPaymentData(googlePaymentRequest).catch((err) => {
        // Customer canceled the popup; clean up associated addresses
        if (err.statusCode === "CANCELED") {
          deleteAssociatedAddresses();
        } else {
          console.error("Error loading payment data:", err);
        }
      });
    },
    (error) => {
      console.error("Error during active cart transaction handling:", error);
    }
  );
}

/**
 * Renders and appends the Google Pay button to the given container element.
 * Initializes click handler to start transaction flow.
 *
 * @param {HTMLElement} container
 */
function renderPaymentButton(container) {
  container.appendChild(
    getClient().createButton({
      onClick: () => initTransaction(),
      buttonSizeMode: "fill",
    })
  );
}

/**
 * Returns an object with Google Pay payment callbacks handlers.
 * Includes authorization and data changed handlers with RxJS observable handling.
 */
function handlePaymentCallbacks() {
  return {
    onPaymentAuthorized: (paymentDataResponse) => {
      // Returns a promise resolving the transaction state object expected by Google Pay
      return rxjs
        .lastValueFrom(
          setDeliveryAddress(paymentDataResponse.shippingAddress).pipe(
            rxjs.switchMap(() => {
              return paymentDataResponse?.email
                ? OpfApis.updateCartGuestUserEmail(paymentDataResponse.email)
                : rxjs.of(true);
            }),
            rxjs.switchMap(() => OpfApis.setPaymentInfo()),
            rxjs.switchMap(() => prepareAndSubmitPayment(paymentDataResponse)),
            rxjs.catchError((e) => {
              console.error("Error during payment processing:", e);
              return rxjs.of(false);
            })
          )
        )
        .then((isSuccess) => {
          deleteAssociatedAddresses();
          return { transactionState: isSuccess ? "SUCCESS" : "ERROR" };
        });
    },

    onPaymentDataChanged: function (intermediatePaymentData) {
      return rxjs.lastValueFrom(
        setDeliveryAddress(intermediatePaymentData.shippingAddress).pipe(
          rxjs.switchMap(() => getShippingOptionParameters()),
          rxjs.switchMap((shippingOptions) => {
            const selectedMode =
              verifyShippingOption(
                intermediatePaymentData.shippingOptionData?.id
              ) ?? shippingOptions?.defaultSelectedOptionId;

            return setDeliveryMode(selectedMode).pipe(
              rxjs.switchMap(() => getSelectedDeliveryMode()),
              rxjs.switchMap((mode) => {
                const paymentDataRequestUpdate = {
                  newShippingOptionParameters: shippingOptions,
                  newTransactionInfo: getNewTransactionInfo(cartData),
                };

                if (
                  paymentDataRequestUpdate.newShippingOptionParameters
                    ?.defaultSelectedOptionId
                ) {
                  paymentDataRequestUpdate.newShippingOptionParameters.defaultSelectedOptionId =
                    mode?.code;
                }

                return rxjs.of(paymentDataRequestUpdate);
              })
            );
          }),
          rxjs.catchError((error) => {
            console.error("Error during onPaymentDataChanged:", error);
            return rxjs.of({
              error: {
                reason: "SHIPPING_ADDRESS_UNSERVICEABLE",
                message: "Something went wrong while processing your address.",
                intent: "SHIPPING_ADDRESS",
              },
            });
          })
        )
      );
    },
  };
}

/**
 * Retrieves supported delivery modes from backend and maps them for Google Pay UI use.
 * Returns observable emitting shipping option parameters.
 * @returns {Observable}
 */
function getShippingOptionParameters() {
  return OpfApis.getSupportedDeliveryModes().pipe(
    rxjs.take(1),
    rxjs.operators.map((modes) => ({
      defaultSelectedOptionId: modes[0]?.code,
      shippingOptions: modes?.map((mode) => ({
        id: mode?.code,
        label: mode?.name,
        description: mode?.description,
      })),
    }))
  );
}

/**
 * Obtains up-to-date transaction info from cart pricing data.
 * Returns undefined if no valid pricing info.
 */
function getNewTransactionInfo() {
  const priceInfo = cartData.totalPriceWithTax;
  if (priceInfo?.value && priceInfo?.currencyIso) {
    return {
      totalPrice: priceInfo.value.toString(),
      currencyCode: priceInfo.currencyIso.toString(),
      totalPriceStatus: "FINAL",
    };
  }
  return undefined;
}

/**
 * Prepares payment submission request based on payment data response,
 * submits payment to backend, and returns an observable for submission status.
 *
 * @param {object} paymentDataResponse - Google Pay payment data response
 * @returns {Observable}
 */
function prepareAndSubmitPayment(paymentDataResponse) {
  const encryptedToken = btoa(
    paymentDataResponse.paymentMethodData.tokenizationData.token
  );

  const submitRequest = {
    additionalData: [],
    encryptedToken,
    paymentSessionId: "",
    channel: "BROWSER",
    paymentMethod: OpfQuickBuyProviderType.GOOGLE_PAY,
    browserInfo: getBrowserInfo(),
  };

  // Clear token if payment method isn't credit card
  submitRequest.encryptedToken =
    submitRequest.paymentMethod !== OpfPaymentMethod.CREDIT_CARD
      ? encryptedToken
      : "";

  return new rxjs.Observable((observer) => {
    const callback = {
      submitSuccess: function () {},
      submitPending: function () {},
      submitFailure: function () {},
    };

    submitRequest.callbacks = callback;

    opfPaymentSubmit(submitRequest, callback, "");
  });
}

/**
 * Sanitizes the shipping mode value.
 * Returns undefined if the special unselected value is passed.
 *
 * @param {string} mode - shipping mode code
 * @returns {string|undefined}
 */
function verifyShippingOption(mode) {
  return mode === "shipping_option_unselected" ? undefined : mode;
}

/**
 * Associates a new address ID with transaction details if not already tracked.
 *
 * @param {string} addressId
 */
function associateAddressId(addressId) {
  if (!isAddressIdAssociated(addressId)) {
    transactionDetails.addressIds.push(addressId);
  }
}

/**
 * Checks if the address ID has been associated with the transaction.
 *
 * @param {string} addressId
 * @returns {boolean}
 */
function isAddressIdAssociated(addressId) {
  return transactionDetails.addressIds.includes(addressId);
}

/**
 * Clears all tracked associated addressIds from the transaction details.
 */
function resetAssociatedAddresses() {
  transactionDetails.addressIds = [];
}

/**
 * Deletes associated user addresses based on tracked IDs,
 * then resets the tracking list.
 * (Implementation placeholder commented as per original code)
 */
function deleteAssociatedAddresses() {
  if (transactionDetails.addressIds?.length) {
    resetAssociatedAddresses();
  }
}

/**
 * Configures allowed payment methods in the Google Pay request
 * using current active configuration.
 *
 * @param {object} activeConfiguration - Active quick buy config for Google Pay
 */
function setAllowedPaymentMethodsConfig(activeConfiguration) {
  const googlePayConfig = getQuickBuyProviderConfig(
    OpfQuickBuyProviderType.GOOGLE_PAY,
    activeConfiguration
  );

  googlePaymentRequest.allowedPaymentMethods = [
    {
      parameters: { ...QuickBuyDefaults.defaultGooglePayCardParameters },
      tokenizationSpecification: {
        parameters: {
          gateway: String(googlePayConfig?.googlePayGateway),
          gatewayMerchantId: String(activeConfiguration.merchantId),
        },
        type: activeConfiguration.providerType,
      },
      type: "CARD",
    },
  ];
}

/**
 * Returns the quick buy provider config for the requested provider.
 *
 * @param {string} provider - Provider name to search for
 * @param {object} activeConfiguration - Current active configuration
 * @returns {object|undefined} Matching provider config or undefined
 */
function getQuickBuyProviderConfig(provider, activeConfiguration) {
  if (!activeConfiguration || !activeConfiguration.digitalWalletQuickBuy)
    return undefined;

  return activeConfiguration.digitalWalletQuickBuy.find(
    (item) => item.provider === provider
  );
}
