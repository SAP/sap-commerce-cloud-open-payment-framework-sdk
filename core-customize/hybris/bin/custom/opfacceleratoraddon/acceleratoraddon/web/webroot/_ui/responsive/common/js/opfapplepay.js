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

var applePaySession;
var isDeviceSupported = false;
var countryCode;
var paymentInProgress = false;
var transactionDetails = GooglePayDefaults.applePayInitialTransactionDetails;
const applePayApiVersion = 3;

function applePayQuickBuy(config) {
  const enabledConfig = OpfUtils.getEnabledQuickBuyWallet(config);
  if (isApplePaySupported(enabledConfig)) {
    countryCode = enabledConfig.countryCode;
    renderApplePayContainer();
  }
}

function isApplePaySupported(enabledConfig) {
  return checkDeviceSupported() && supportsVersion(applePayApiVersion)
    ? canMakePaymentsWithActiveCard(enabledConfig.merchantid)
    : rxjs.of(false);
}

function checkDeviceSupported() {
  const applePaySession = getApplePaySession();
  if (applePaySession) {
    isDeviceSupported = applePaySession.canMakePayments();
  }

  return isDeviceSupported;
}

function renderApplePayContainer() {
  const applePayDiv = document.createElement("div");
  applePayDiv.className = "apple-pay-button";
  document.getElementById("cx-opf-quick-buy-buttons").appendChild(applePayDiv);

  applePayDiv.addEventListener("click", applePayStart);
}

function applePayStart() {
  if (paymentInProgress) {
    return rxjs.throwError(() => new Error("Apple Pay is already in progress"));
  }
  paymentInProgress = true;
}

function supportsVersion(version) {
  try {
    return isDeviceSupported && applePaySession.supportsVersion(version);
  } catch (err) {
    return false;
  }
}

function canMakePayments() {
  try {
    return isDeviceSupported && applePaySession.canMakePayments();
  } catch (err) {
    return false;
  }
}

function canMakePaymentsWithActiveCard(merchantId) {
  return isDeviceSupported
    ? rxjs.from(applePaySession.canMakePaymentsWithActiveCard(merchantId))
    : rxjs.of(false);
}

function getApplePaySession() {
  if (!window["ApplePaySession"]) {
    return undefined;
  }
  return window["ApplePaySession"];
}
