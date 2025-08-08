/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */

window.Opf = window.Opf || {};

$(document).ready(function () {
  'use strict';

  const error = sessionStorage.getItem('globalError');
  if (error) {
    showGlobalError(error);
    sessionStorage.removeItem('globalError');
  }

  const ctaScriptContext = $('input[name="cta-script-context"]').val();

  // Exit early if context is not available
  if (!ctaScriptContext?.trim()) return;

  const selectedLanguage = $('#lang-selector option:selected').val() || 'en';

  // First: fetch merchantIds from the active-configurations API
  OpfApis.fetchActiveConfigs().then((configs) => {
    const paymentAccountIds = configs.map((config) => config.id);

    // Then: Call cta-scripts-rendering API using the retrieved merchantIds
    if (paymentAccountIds.length > 0) {
      loadOpfCtaScript(selectedLanguage, ctaScriptContext, paymentAccountIds);
    } else {
      console.warn('No valid merchantIds found in active configs.');
    }
  });
});


/**
 * Sends a POST request to the OPF CTA script rendering API and injects the returned dynamic script and HTML into the page.
 */
function loadOpfCtaScript(selectedLanguage, ctaScriptContext, paymentAccountIds) {
  const url = `${ACC.config.encodedContextPath}/opf-payment/cta-scripts-rendering`;
  const ctaProductItemsArr = [];

  // Handle PDP context
  if (ctaScriptContext.indexOf('PDP') > -1) {
    const productCode = $('.code').html() || '';
    const quantity = $('#pdpAddtoCartInput').val() || 0;

    ctaProductItemsArr.push({
      productId: productCode,
      quantity: parseInt(quantity)
    });
  }

  // Handle CART context
  else if (ctaScriptContext.indexOf('CART') > -1) {
    document.querySelectorAll('form[id^="updateCartForm"]').forEach(form => {
      const productCodeSelected = form.querySelector('input[name="productCode"]');
      const quantitySelected = form.querySelector('input[name="quantity"]');

      ctaProductItemsArr.push({
        productId: productCodeSelected.value.trim(),
        quantity: parseInt(quantitySelected.value.trim())
      });
    });
  }

  const scriptIdentifier = getNewScriptIdentifier();

  const payload = {
    additionalData: [
      { key: "locale", value: selectedLanguage },
      { key: "currency", value: ACC.common.currentCurrency },
      { key: "scriptIdentifier", value: scriptIdentifier }
    ],
    paymentAccountIds,
    ctaProductItems: ctaProductItemsArr,
    scriptLocations: [ctaScriptContext]
  };

  $.ajax({
    url: url,
    data: JSON.stringify(payload),
    method: "POST",
    contentType: 'application/json',
    success: function (response) {
      if (response?.value?.length > 0) {
        const jsItem = response.value[0].dynamicScript.jsUrls[0];

        createAndAppendResource({
          type: 'script',
          url: jsItem.url,
          attributes: {
            ...(jsItem.attributes || []).reduce((acc, attr) => {
              acc[attr.key] = attr.value;
              return acc;
            }, {}),
            'data-opf-resource': 'true'
          }
        });

        const container = document.getElementById("opf-cta-script");
        if (container) {
          container.innerHTML = response.value[0].dynamicScript.html;
          executeScriptFromHtml(response.value[0].dynamicScript.html);
          scriptReady(ctaScriptContext, scriptIdentifier);
        }
      }
    },
    error: function (jqXHR, textStatus, errorThrown) {
      console.error(`Failed to close account. Error: [${errorThrown}]`);
    }
  });
}

function scriptReady(ctaScriptContext, scriptIdentifier) {
  const productInfo = [];

  // Check if the script is running on the Product Detail Page (PDP)
  if (ctaScriptContext.indexOf('PDP') > -1) {
    const productPrice = $('.price').html().trim().replace(/[^\d.]+/g, "");
    const quantity = $('#pdpAddtoCartInput').val() || 0;

    // Build the product info object for PDP
    productInfo.push({
      price: {
        sellingPrice: Number(productPrice)
      },
      quantity: parseInt(quantity)
    });

    // Send the product data to Klarna
    dispatchProductEvents(productInfo, scriptIdentifier);

    // Attach a delegated listener to the add-to-cart container
    // This captures both 'input' (typing) and 'click' (on +/- buttons) events
    $('.addtocart-component').on('input click', function (e) {
      const $input = $('#pdpAddtoCartInput');

      // Check if the event target is either:
      // - the quantity input itself
      // - one of the plus/minus quantity buttons
      const isQtyInput = e.target.id === 'pdpAddtoCartInput';
      const isQtyButton = $(e.target).closest('.js-qty-selector-plus, .js-qty-selector-minus').length > 0;

      if (isQtyInput || isQtyButton) {
        // Use a 0ms timeout to wait for the DOM to update the input value
        setTimeout(() => {
          // Get the updated quantity from the input field
          const updatedQty = parseInt($input.val()) || 1;

          // Get the current selling price from the DOM, stripping the '$' symbol
          const updatedPrice = $('.price').html().trim().replace(/[^\d.]+/g, "");

          // Create a product info object to pass to the tracking function
          const updatedProductInfo = [{
            price: {
              sellingPrice: Number(updatedPrice)
            },
            quantity: updatedQty
          }];

          // Dispatch the event to Klarna
          dispatchProductEvents(updatedProductInfo, scriptIdentifier);
        }, 0);
      }
    });
  }

  // Check if the script is running on the Cart page
  else if (ctaScriptContext.indexOf('CART') > -1) {
    const cartTotal = $('.text-right.grand-total').html().trim().replace(/[^\d.]+/g, "");

    const cart = {
      total: Number(cartTotal)
    };

    // Send the cart data to Klarna
    dispatchCartEvents(cart, scriptIdentifier);
  }
}

function dispatchProductEvents(productInfo,scriptIdentifiers) {
    const event = new CustomEvent('opfProductAmountChanged', {
        detail: {
            productInfo: productInfo,
            scriptIdentifiers: scriptIdentifiers
        }
    });

    // Dispatch the event globally
    window.dispatchEvent(event);
}

function dispatchCartEvents(cart,scriptIdentifiers) {
const event = new CustomEvent('opfCartChanged', {
        detail: {
            cart: cart,
            scriptIdentifiers: scriptIdentifiers
        }
    });

    // Dispatch the event globally
    window.dispatchEvent(event);
    }
/**
 * Get Script identifiers for CTA script
 * Count will increase on each page visit and will be reset once the browser tab is closed.
 */
function getNewScriptIdentifier() {
    const scriptIdentifiers = JSON.parse(sessionStorage.getItem('scriptIdentifiers') || '[]');
    scriptIdentifiers.push(String(scriptIdentifiers.length + 1));
    sessionStorage.setItem('scriptIdentifiers', JSON.stringify(scriptIdentifiers));
    return String(scriptIdentifiers[scriptIdentifiers.length - 1]).padStart(4, '0');
}

/**
 * Removes leading slashes from a path string.
 */
function removeLeadingSlashes(path) {
  return path.replace(/^\/+/, '');
}

/**
 * Fetches the payment method content (e.g., iframe or HTML snippet)
 * from the OPF payment-initiate endpoint and returns the response.
 */
window.Opf.fetchPaymentMethodContent = function(accountId, proceedPaymentLabel) {
  showPageLoader();
  showLoader();

  const url = `${ACC.config.contextPath}/checkout/multi/opf-payment/payment-initiate`;
  const origin = window?.location?.origin;
  const encodedContextPath = removeLeadingSlashes(ACC.config.encodedContextPath);

  const payload = {
    accountId,
    resultURL: `${origin}/${encodedContextPath}/opf/payment-verification-redirect/result`,
    cancelURL: `${origin}/${encodedContextPath}/opf/payment-verification-redirect/cancel`,
    browserInfo: getBrowserInfo()
  };

  // Fetch payment gateway content and render it using the provided configuration
  $.ajax({
    url,
    method: "POST",
    contentType: "application/json",
    data: JSON.stringify(payload)
  })
  .then((paymentGateway) => {
    renderPaymentGateway(paymentGateway, proceedPaymentLabel);

    if(isHostedFields(paymentGateway)) {
      registerSubmit('checkout', paymentGateway.paymentSessionId);
      registerSubmitComplete('checkout', paymentGateway.paymentSessionId);
    }
  })
  .catch((err) => {
    showPaymentGatewayError(err);
  })
  .always(() => {
    hidePageLoader();
  });
};


/**
 * Renders payment gateway resources by loading JS/CSS and injecting HTML for FULL_PAGE, HOSTED_FIELDS and IFRAME patterns.
 */
function renderPaymentGateway(paymentGateway, proceedPaymentLabel) {
  const destination = paymentGateway?.destination;
  const pattern = paymentGateway?.pattern;

  if (paymentGateway?.dynamicScript) {
    const html = paymentGateway.dynamicScript.html;
    const jsUrls = paymentGateway.dynamicScript.jsUrls || [];
    const cssUrls = paymentGateway.dynamicScript.cssUrls || [];

    loadPaymentResources(jsUrls, cssUrls,
      () => showPaymentGatewayHtml(html),
      () => showPaymentGatewayError(new Error('Unable to load payment provider scripts.')));
    return;
  }

  if (pattern === 'FULL_PAGE' && destination.url && destination.form?.length <= 0) {
    return showFullPageLink(destination.url, proceedPaymentLabel);
  }

  showPaymentGatewayError(new Error('Unknown or unsupported payment gateway render type.'));
}

// ------------------------------
// UI Helpers
// ------------------------------

/**
 * Shows loader and resets error/container.
 */
function showLoader() {
  document.getElementById('payment-gateway-loader').style.display = 'block';
  document.getElementById('payment-gateway-error').style.display = 'none';
  document.getElementById('payment-gateway-container').innerHTML = '';
}

/**
 * Displays a error message on the UI.
 *
 * Falls back to a default message if the error object has no message.
 */
function showPaymentGatewayError(error) {
  showError(error, 'Something went wrong while loading payment gateway.');
}

/**
 * Displays error message on UI.
 */
function showError(error, fallbackErrorMsg) {
  document.getElementById('payment-gateway-loader').style.display = 'none';
  const errorEl = document.getElementById('payment-gateway-error');
  errorEl.innerText = extractErrorMessage(error) || fallbackErrorMsg;
  errorEl.style.display = 'block';
}

/**
 * Displays dynamic payment gateway HTML and executes embedded scripts.
 */
function showPaymentGatewayHtml(html) {
  document.getElementById('payment-gateway-loader').style.display = 'none';
  document.getElementById('payment-gateway-error').style.display = 'none';
  document.getElementById('payment-gateway-container').innerHTML = html;

  // Delay to ensure HTML is rendered before script execution
  setTimeout(() => {
    executeScriptFromHtml(html);
  }, 0);
}

/**
 * Executes inline <script> tags from the provided HTML content.
 */
function executeScriptFromHtml(html) {
  const doc = new DOMParser().parseFromString(html, 'text/html');
  const scripts = doc.querySelectorAll('script');

  scripts.forEach(script => {
    if (script.innerText) {
      try {
        new Function(script.innerText)();
      } catch (e) {
        console.error('Script execution error:', e);
      }
    }
  });
}

// ------------------------------
// Dynamic Resource Loader
// ------------------------------

/**
 * Loads JS and CSS resources dynamically, executes callbacks accordingly.
 */
function loadPaymentResources(scripts, styles, onComplete, onError) {
  scripts = scripts || [];
  styles = styles || [];

  const resources = [];

  scripts.forEach(script => {
    script.type = 'SCRIPT';
    resources.push(script);
  });

  styles.forEach(style => {
    style.type = 'STYLES';
    resources.push(style);
  });

  if (!resources.length) {
    if (typeof onComplete === 'function') onComplete();
    return;
  }

  let remaining = resources.length;
  let hasError = false;

  const done = () => {
    if (--remaining === 0 && !hasError && typeof onComplete === 'function') {
      onComplete();
    }
  };

  const handleError = () => {
    hasError = true;
    if (typeof onError === 'function') onError();
  };

  resources.forEach((res) => {
    if (!res.url) {
      done();
      return;
    }

    if (res.type === 'SCRIPT') {
      loadPaymentScript(res, done, handleError);
    } else if (res.type === 'STYLES') {
      loadPaymentStyles(res, done, handleError);
    } else {
      done();
    }
  });
}

/**
 * Loads external JavaScript dynamically if not already loaded.
 */
function loadPaymentScript(resource, onLoad, onError) {
  if (isScriptAlreadyLoaded(resource.url)) {
    onLoad();
    return;
  }

  createAndAppendResource({
    type: 'script',
    url: resource.url,
    attributes: createAttributesList(resource.attributes || {}),
    sri: resource.sri,
    onLoad,
    onError
  });
}

/**
 * Checks if a given JS script URL is already present in the DOM.
 */
function isScriptAlreadyLoaded(url) {
  const scripts = document.querySelectorAll('script[src]');
  return Array.from(scripts).some(script => script.src === url);
}

/**
 * Loads CSS dynamically if not already loaded.
 */
function loadPaymentStyles(resource, onLoad, onError) {
  if (isStyleAlreadyLoaded(resource.url)) {
    onLoad();
    return;
  }

  createAndAppendResource({
    type: 'style',
    url: resource.url,
    attributes: createAttributesList(resource.attributes || {}),
    sri: resource.sri,
    onLoad,
    onError
  });
}

/**
 * Checks if a given CSS URL is already loaded.
 */
function isStyleAlreadyLoaded(url) {
  const links = document.querySelectorAll('link[rel="stylesheet"]');
  return Array.from(links).some(link => link.href === url);
}

/**
 * Converts list of key-value attributes into a flat object
 * and adds OPF-specific attributes if applicable.
 */
function createAttributesList(keyValueList) {
  const attributes = {};
  const OPF_RESOURCE_ATTRIBUTE_KEY = 'data-opf-resource';
  const OPF_RESOURCE_LOAD_ONCE_ATTRIBUTE_KEY = 'opf-load-once';

  (keyValueList || []).forEach((kv) => {
    attributes[kv.key] = kv.value;
  });

  if (
    !attributes[OPF_RESOURCE_LOAD_ONCE_ATTRIBUTE_KEY] ||
    attributes[OPF_RESOURCE_LOAD_ONCE_ATTRIBUTE_KEY] !== 'true'
  ) {
    attributes[OPF_RESOURCE_ATTRIBUTE_KEY] = 'true';
  }

  delete attributes[OPF_RESOURCE_LOAD_ONCE_ATTRIBUTE_KEY];
  return attributes;
}

/**
 * Dynamically creates and appends a script or stylesheet resource to the document head.
 */
function createAndAppendResource({ type, url, attributes = {}, sri, onLoad, onError }) {
  let element;

  // Create the appropriate HTML element based on resource type
  if (type === 'script') {
    element = document.createElement('script');
    element.src = url;
    element.type = 'text/javascript';
    element.async = true;
    element.defer = true;
  } else if (type === 'style') {
    element = document.createElement('link');
    element.href = url;
    element.rel = 'stylesheet';
  } else {
    // Unsupported resource type
    console.warn(`Unsupported resource type: ${type}`);
    return;
  }

  // Apply any additional custom attributes
  Object.entries(attributes).forEach(([key, value]) => {
    element.setAttribute(key, value);
  });

  // If SRI is provided, set integrity and crossorigin for security
  if (sri) {
    element.setAttribute('integrity', sri);
    element.setAttribute('crossorigin', 'anonymous');
  }

  // Register event callbacks for success and failure
  if (onLoad) element.onload = onLoad;
  if (onError) element.onerror = onError;

  // Inject the element into the document head
  document.head.appendChild(element);
}


/**
 * Returns browser-related info.
 */
function getBrowserInfo() {
  return {
    acceptHeader: 'application/json',
    colorDepth: window?.screen?.colorDepth,
    javaEnabled: false,
    javaScriptEnabled: true,
    language: window?.navigator?.language,
    screenHeight: window?.screen?.height,
    screenWidth: window?.screen?.width,
    userAgent: window?.navigator?.userAgent,
    originUrl: window?.location?.origin,
    timeZoneOffset: new Date().getTimezoneOffset(),
  };
}

/**
 * Checks whether the payment session data is a "hosted fields" pattern.
 */
function isHostedFields(paymentSessionData) {
  return !!(
    !(paymentSessionData instanceof Error) &&
    paymentSessionData?.paymentSessionId &&
    paymentSessionData?.pattern === 'HOSTED_FIELDS'
  );
}

/**
 * Initializes or retrieves the global container for the payment domain.
 */
function getGlobalFunctionContainer(domain) {
  if (!window.Opf.payments) {
    window.Opf.payments = {};
  }

  if (!window.Opf.payments[domain]) {
    window.Opf.payments[domain] = {};
  }

  return window.Opf.payments[domain];
}

/**
 * Registers a `submit` function for a given domain and session ID
 * to initiate **initial payment submission** using Adyen Hosted Fields
 * with retry and error handling logic.
 */
function registerSubmit(domain, paymentSessionId) {
  const container = getGlobalFunctionContainer(domain);

  container.submit = function ({
        additionalData,
        submitSuccess = function () {},
        submitPending = function () {},
        submitFailure = function () {},
      }) {
    const payload = {
      additionalData,
      channel: "BROWSER",
      browserInfo: getBrowserInfo(),
      encryptedToken: "",
    };

    opfPaymentSubmit(payload, { submitSuccess, submitPending, submitFailure }, paymentSessionId);
  };
}

function opfPaymentSubmit(payload, callback, paymentSessionId) {
  let url = `${ACC.config.contextPath}/checkout/multi/opf-payment/${encodeURIComponent(paymentSessionId)}/payment-submit`;

  // Ensure the URL is clean and does not contain double slashes if paymentSessionId is empty for Quick Buy Payments
  url = url.replace(/\/{2,}/g, "/");

  attemptAjaxSubmit({
    url,
    payload,
    maxRetries: 2,
    onSuccess: (res) => handlePaymentResponse(res, callback),
    onError: callback.submitFailure,
  });
}

/**
 * Registers a `submitComplete` function for a given domain and session ID
 * to complete the **3D Secure challenge** or other finalization step for
 * Adyen Hosted Fields integration, with retry and error handling logic.
 */
function registerSubmitComplete(domain, paymentSessionId) {
  const container = getGlobalFunctionContainer(domain);

  container.submitComplete = function ({
    additionalData,
    submitSuccess = function () {},
    submitPending = function () {},
    submitFailure = function () {},
  }) {
    const payload = {
      paymentSessionId,
      additionalData,
    };

    const url = `${ACC.config.contextPath}/checkout/multi/opf-payment/${encodeURIComponent(paymentSessionId)}/payment-submit-complete`;

    attemptAjaxSubmit({
      url,
      payload,
      maxRetries: 2,
      onSuccess: (res) =>
        handlePaymentResponse(res, { submitSuccess, submitPending, submitFailure }),
      onError: submitFailure,
    });
  };
}

// --- Shared Payment Helper Functions ---

/**
 * Generic AJAX POST handler with retry support.
 * Used by both `submit` and `submitComplete` calls.
 */
function attemptAjaxSubmit({ url, payload, maxRetries, onSuccess, onError }) {
  let attempts = 0;

  showPageLoader();

  function trySubmit() {
    $.ajax({
      url,
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload),
      success: function (response) {
        hidePageLoader();
        onSuccess(response);
      },
      error: function (xhr, status, errorThrown) {
        attempts++;

        if (attempts < maxRetries) {
          console.warn('Retrying AJAX request...');
          trySubmit();
        } else {
          hidePageLoader();
          const error = {
            statusText: 'Request Failed',
            message: status,
            status: xhr.status,
            type: 'AJAX_FAILURE',
          };
          onError(error);
          showError(xhr);
        }
      },
    });
  }

  trySubmit();
}

/**
 * Evaluates the payment status from response and routes to correct callback.
 * Used by both submit & submitComplete to handle response consistently.
 */
function handlePaymentResponse(response, { submitSuccess, submitPending, submitFailure }) {
  const status = response.status;
  const error = {
    statusText: 'Payment Error',
    status: -1,
  };

  if (status === 'ACCEPTED' || status === 'DELAYED') {
    Promise.resolve(submitSuccess(response))
      .then(() => {
        return placePaymentAuthorizedOrder();
      })
      .then((orderResponse) => {
        const code =
          orderResponse?.user?.name === AppConstants.OCC_USER_ID_GUEST
            ? orderResponse?.guid
            : orderResponse?.code;

        if (code) {
          const encodedContextPath = removeLeadingSlashes(
            ACC.config.encodedContextPath
          );
          window.location.href = `${window.location.origin}/${encodedContextPath}/checkout/orderConfirmation/${code}`;
        }
      })
      .catch((err) => {
        console.error('Order placement failed:', err);
      });
  } else if (status === 'PENDING') {
    submitPending(response);
  } else if (status === 'REJECTED') {
    error.type = 'PAYMENT_REJECTED';
    error.message = 'Payment was rejected';
    submitFailure(error);
  } else {
    error.type = 'STATUS_NOT_RECOGNIZED';
    error.message = 'Unknown payment status';
    submitFailure(error);
  }
}

/**
 * Places an order after the payment was authorized
*/
function placePaymentAuthorizedOrder() {
  const url = `${ACC.config.contextPath}/checkout/multi/summary/opf-payment/placeOrder?termsChecked=true`;

  showPageLoader();

  return new Promise((resolve, reject) => {
    $.ajax({
      url,
      method: 'POST',
      contentType: 'application/json',
      data: null,
      success: function (response) {
        resolve(response);
      },
      error: function (xhr, status, error) {
        reject(error);
      },
      complete: function() {
        hidePageLoader();
      }
    });
  });
}

// =======================
// UI Helper Functions
// =======================

/** Show the billing address form container */
function showBillingAddressForm() {
  $('#billingAddressFormContainer').show();
}

/** Hide the billing address form container */
function hideBillingAddressForm() {
  $('#billingAddressFormContainer').hide();
}

/** Show the billing address summary section */
function showBillingAddressSummary() {
  $('#billingAddressSummary').show();
}

/** Hide the billing address summary section */
function hideBillingAddressSummary() {
  $('#billingAddressSummary').hide();
}

// =======================
// Data Access Helpers
// =======================

/**
 * Get the delivery address data from hidden DOM fields
 * (used when copying delivery address as billing address)
 */
function getDeliveryAddressFromDOM() {
  const $el = $('#useDeliveryAddressData');
  if (!$el.length) return null;

  const deliveryAddress = {
    country: { isocode: $el.data('countryisocode') || '' },
    titleCode: ($el.data('title') || '').toLowerCase().replace(/\.$/, ''),
    firstName: $el.data('firstname') || '',
    lastName: $el.data('lastname') || '',
    line1: $el.data('line1') || '',
    line2: $el.data('line2') || '',
    town: $el.data('town') || '',
    postalCode: String($el.data('postalcode') || '').trim(),
    phone: '',
    cellphone: '',
    defaultAddress: false,
    id: $el.data('address-id') || ''
  };

  const regionIsoCode = $el.data('regionisocode') || '';

  if (regionIsoCode) {
    deliveryAddress['region'] = { isocode: regionIsoCode }
  }

  return deliveryAddress;
}

// =======================
// Billing Address Logic
// =======================

/**
 * Send billing address to server via AJAX
 */
function saveBillingAddress(cartId, billingAddress) {
  if (!cartId) {
    console.error('Cart ID is missing.');
    return;
  }

  const url = `/mystorestorefront/checkout/multi/opf-payment/${cartId}/addresses/billing`;

  showPageLoader();

  $.ajax({
    url: url,
    method: 'PUT',
    contentType: 'application/json',
    data: JSON.stringify(billingAddress),
    success: function (response) {
      hideBillingAddressForm();
      showBillingAddressSummary();
      renderBillingAddressSummary(billingAddress);
    },
    error: function (xhr, status, error) {
      console.error('Error saving billing address:', error);
    },
    complete: function() {
      hidePageLoader();
    }
  });
}

/**
 * Render billing address summary from a billing address object
 */
function renderBillingAddressSummary(address) {
  const titlePart = address.titleCode ? address.titleCode + '&nbsp;' : '';
  const firstNamePart = address.firstName ? address.firstName + '&nbsp;' : '';
  const lastNamePart = address.lastName || '';

  const summaryHtml = `
    ${titlePart}${firstNamePart}${lastNamePart}<br />
    ${address.line1 ? address.line1 + ',' : ''}
    ${address.line2 ? address.line2 + ',' : ''}
    ${address.town ? address.town + ',' : ''}
    ${address.postalCode ? address.postalCode + ',' : ''}
  `;

  $('#billingAddressSummary').html(summaryHtml);
}

// =======================
// Opf Namespace Functions
// =======================

/**
 * Copy delivery address into billing using data from DOM
 */
window.Opf.saveDeliveryAddress = function (cartId) {
  const deliveryAddress = getDeliveryAddressFromDOM();
  if (deliveryAddress) {
    saveBillingAddress(cartId, deliveryAddress);
  } else {
    console.warn('Delivery address not found in DOM.');
  }
};

/**
 * Save billing address after editing the form
 */
window.Opf.saveBillingAddressEdit = function (cartId, countryIsoCode) {
  const form = $('#silentOrderPostForm')[0];

  if (!form) {
    console.error('Form is missing.');
    return;
  }

  const formData = new FormData(form);
  const billingAddress = {};

  // Convert form data into object
  for (let [key, value] of formData.entries()) {
    billingAddress[key] = value;
  }

  // Format billing address to match backend schema
  const payload = {
    country: { isocode: countryIsoCode },
    titleCode: (billingAddress.billTo_titleCode || '').toLowerCase().replace(/\.$/, ''),
    firstName: billingAddress.billTo_firstName,
    lastName: billingAddress.billTo_lastName,
    line1: billingAddress.billTo_street1,
    line2: billingAddress.billTo_street2,
    town: billingAddress.billTo_city,
    postalCode: billingAddress.billTo_postalCode,
    phone: billingAddress.billTo_phoneNumber,
    cellphone: '',
    defaultAddress: false,
    id: ''
  };

  if (billingAddress.billTo_state) {
    payload['region'] = { isocode: billingAddress.billTo_state }
  }

  saveBillingAddress(cartId, payload);
};

/**
 * Cancel editing billing address, revert to delivery address
 */
window.Opf.cancelBillingAddressEdit = function () {
  const checkbox = $('#useDeliveryAddress');

  if (!checkbox.length) {
    console.warn('Checkbox #useDeliveryAddress not available for cancellation.');
    return;
  }

  // Set checkbox as checked and trigger change
  checkbox.prop('checked', true).trigger('change');

  // Optional: sync with DOM (useful if checkbox was rendered server-side)
  document.getElementById('useDeliveryAddress')?.setAttribute('checked', 'checked');

  // Reset billing address form
  $('#silentOrderPostForm')[0]?.reset();
};

/**
 * Extracts and processes the payment session parameters from the requestMap.
 */
function verifyResultUrl(requestMap) {
  const paramsMap = Object.entries(requestMap).map(([key, value]) => ({ key, value }));

  const paymentSessionIdParam = paramsMap.find(p => p.key === 'opfPaymentSessionId');
  const afterRedirectScriptFlagParam = paramsMap.find(p => p.key === 'opfAfterRedirectScriptFlag');

  if (!paymentSessionIdParam?.value) {
    throw new Error('Missing opfPaymentSessionId');
  }

  return {
    paramsMap,
    afterRedirectScriptFlag: afterRedirectScriptFlagParam?.value,
  };
}

/**
 * Chooses the appropriate payment pattern logic to run.
 */
function runPaymentPattern({ paramsMap, afterRedirectScriptFlag }) {
  if (afterRedirectScriptFlag === 'true') {
    return Promise.reject(new Error('Hosted field pattern not implemented'));
  }

  return runHostedPagePattern(paramsMap);
}

/**
 * Executes the hosted page pattern by calling the verification endpoint.
 */
function runHostedPagePattern(paramsMap) {
  const baseUrl = `${ACC.config.contextPath}/opf/payment-verification-redirect/verify`;

  // Convert param array to query string
  const queryParams = new URLSearchParams(paramsMap.map(({ key, value }) => [key, value]));
  const url = `${baseUrl}?${queryParams.toString()}`;

  showPageLoader();

  return new Promise((resolve, reject) => {
    $.ajax({
      url,
      method: 'GET',
    })
    .done(resolve)
    .fail(reject)
    .always(() => {
      hidePageLoader();
    })
  });
}

/**
 * Starts the payment verification process.
 * If successful, places the order and redirects to order confirmation.
 * If anything fails, redirects to the fallback page.
 */
window.Opf.verifyPayment = function(requestMap) {
  const encodedContextPath = removeLeadingSlashes(ACC.config.encodedContextPath);
  const fallbackUrl = `${window.location.origin}/${encodedContextPath}/checkout/multi/opf-payment/choose`;

  try {
    const { paramsMap, afterRedirectScriptFlag } = verifyResultUrl(requestMap);

    runPaymentPattern({ paramsMap, afterRedirectScriptFlag })
      .then(() => {
        return placePaymentAuthorizedOrder()
          .then(orderResponse => {
            if (orderResponse?.code) {
              window.location.href = `${window.location.origin}/${encodedContextPath}/checkout/orderConfirmation/${orderResponse.code}`;
            } else {
              throw new Error('Empty order response');
            }
          });
      })
      .catch(error => {
        const message = extractErrorMessage(error) || 'Something went wrong during payment processing.';
        sessionStorage.setItem('globalError', message);
        window.location.href = fallbackUrl;
      });

  } catch (error) {
    const message = extractErrorMessage(error) || 'Unexpected error during payment verification.';
    sessionStorage.setItem('globalError', message);
    window.location.href = fallbackUrl;
  }
};

/**
 * Renders a full-page payment link as a clickable button.
 */
function showFullPageLink(url, proceedPaymentLabel) {
  const container = document.getElementById('payment-gateway-container');

  container.innerHTML = `
    <a class="btn btn-primary" href="${url}">
      ${proceedPaymentLabel}
    </a>
  `;

  document.getElementById('payment-gateway-loader').style.display = 'none';
}

/**
 * Extracts a error message from different types of error objects.
 */
function extractErrorMessage(error) {
  // Case 1: Native JS Error
  if (error instanceof Error) {
    return error.message;
  }

  // Case 2: jQuery AJAX error format with nested responseJSON
  if (typeof error === 'object' && error !== null) {
    const errors = error?.responseJSON?.errors;
    if (Array.isArray(errors) && errors.length > 0) {
      const err = errors[0];

      // Safely access most descriptive message
      return (
        err.message ||
        err.exceptionMessage
      );
    }
  }

  // Case 3: Unknown Error
  return null;
}

/**
 * Displays a global error message at the top of the page with close button.
 */
function showGlobalError(message) {
  const errorContainer = document.getElementById('opf-global-error');
  if (!errorContainer) return;

  const messageEl = errorContainer.querySelector('.message');
  const closeBtn = errorContainer.querySelector('.close-btn');

  messageEl.innerText = message;
  errorContainer.style.display = 'flex';

  // Add click listener to close button
  closeBtn.onclick = function () {
    errorContainer.style.display = 'none';
  };
}

// Shows the full-page loader overlay to block user interaction
function showPageLoader() {
  $('#page-loader').show();
}

// Hides the full-page loader overlay once processing is complete
function hidePageLoader() {
  $('#page-loader').hide();
}

