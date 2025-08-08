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

// Start the fetch of active configs and initialize Quick Buy with first enabled provider.
window.addEventListener("DOMContentLoaded", function () {
  const quickBuyContainer = document.getElementById("cx-opf-quick-buy-buttons");
  if (quickBuyContainer) {
    OpfApis.fetchActiveConfigs()
      .then((configs) => {
        if (!Array.isArray(configs)) {
          console.error("Configs data is not an array:", configs);
          return;
        }
        const enabledConfig = fetchEnabledQuickBuyProvider(configs);
        if (!enabledConfig) {
          console.warn("No enabled quick buy provider config found.");
          return;
        }
        initQuickBuy(enabledConfig);
      })
      .catch((error) => {
        console.error("Failed to fetch active configs:", error);
      });
  }
});

/**
 * Initialize the Quick Buy workflow for each enabled digital wallet provider.
 * @param {object} activeConfiguration - The configuration object that contains digitalWalletQuickBuy array.
 */
function initQuickBuy(activeConfiguration) {
  if (
    !activeConfiguration ||
    !Array.isArray(activeConfiguration.digitalWalletQuickBuy)
  ) {
    console.error("Invalid activeConfiguration object:", activeConfiguration);
    return;
  }

  OpfUtils.loadCartData();

  activeConfiguration.digitalWalletQuickBuy.forEach((wallet) => {
    if (!wallet || !wallet.enabled) return;

    // load RxJS
    loadScript("https://unpkg.com/rxjs/dist/bundles/rxjs.umd.min.js", "rxjs")
      .then(() => {
        switch (wallet.provider) {
          case OpfQuickBuyProviderType.APPLE_PAY:
            // applePayQuickBuy(activeConfiguration);
            break;

          case OpfQuickBuyProviderType.GOOGLE_PAY:
            return loadScript(
              "https://pay.google.com/gp/p/js/pay.js",
              "google-pay-js"
            ).then(() => {
              // Google Pay script loaded, continue
              googlePayQuickBuy(activeConfiguration);
            });
          default:
            console.warn("Unsupported wallet provider:", wallet.provider);
        }
      })
      .catch((err) => {
        console.error("Script loading error:", err);
      });
  });
}

/**
 * Locate the enabled quick buy configuration with at least one enabled wallet.
 * @param {Array} configs - Array of config objects.
 * @returns {object|null} - The found config object, or null if none.
 */
function fetchEnabledQuickBuyProvider(configs) {
  if (!Array.isArray(configs)) {
    console.error("configs is not an array:", configs);
    return null;
  }

  const enabledQuickBuyConfig =
    configs.find(
      (item) =>
        item &&
        item.providerType === AppConstants.PAYMENT_GATEWAY &&
        Array.isArray(item.digitalWalletQuickBuy) &&
        item.digitalWalletQuickBuy.some((wallet) => wallet && wallet.enabled)
    ) || null;

  return enabledQuickBuyConfig;
}

/**
 *
 * Dynamic Script Loading Added for Google Pay and RxJS
 * Implemented a reusable loadScript(src, id) function that returns a Promise,
 * allowing scripts to load asynchronously and enabling conditional loading based on server config.
 *
 * This improves page performance by avoiding unnecessary loading of third-party scripts.
 *
 */
function loadScript(src, id) {
  return new Promise((resolve, reject) => {
    if (id && document.getElementById(id)) {
      // Already loaded
      resolve();
      return;
    }
    const script = document.createElement("script");
    script.src = src;
    if (id) script.id = id;
    script.type = "text/javascript";
    script.async = true;
    script.onload = resolve;
    script.onerror = reject;
    document.head.appendChild(script);
  });
}
