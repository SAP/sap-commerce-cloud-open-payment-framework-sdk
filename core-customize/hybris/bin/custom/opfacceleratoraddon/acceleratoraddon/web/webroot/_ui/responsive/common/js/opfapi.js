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

(function (window) {
  // Helper: fetch wrapper that handles JSON parsing and errors uniformly
  function fetchWithHandling(url, options = {}, parseJson = true) {
    return fetch(url, options).then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      return parseJson ? response.json() : response;
    });
  }

  // Helper: observable wrapper for jQuery Ajax calls, supporting all HTTP verbs
  function ajaxObservable({ url, method = "GET", data = null, headers = {} }) {
    return new rxjs.Observable((observer) => {
      $.ajax({
        url,
        method,
        contentType: "application/json",
        data: data,
        headers: headers,
        success: (response) => {
          observer.next(response || []); // Always emit value or empty array
          observer.complete();
        },
        error: (xhr, status, error) => {
          observer.error(
            new Error(
              `AJAX error! Status: ${xhr.status || status}. Message: ${error}`
            )
          );
        },
      });
    });
  }

  /**
   * GET: Retrieve supported delivery modes as array.
   * Returns an Observable emitting the delivery modes array.
   */
  function getSupportedDeliveryModes() {
    const url = `${ACC.config.encodedContextPath}/opf/cart/deliverymodes`;
    const promise = fetchWithHandling(url).then(
      (data) => data.deliveryModes || []
    );
    return rxjs.from(promise);
  }

  /**
   * PUT: Set the selected delivery mode.
   * @param {string} modeModeId - delivery mode id to set.
   * Returns Observable that completes when done.
   */
  function setDeliveryMode(modeModeId) {
    const url = `${ACC.config.encodedContextPath}/opf/cart/deliverymode?deliveryModeId=${modeModeId}`;
    const promise = fetchWithHandling(
      url,
      {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
      },
      false
    ); // no JSON parse on response expected
    return rxjs.from(promise);
  }

  /**
   * POST: Set/update delivery address.
   * @param {object} address - JSON address object.
   * Returns Observable emitting response JSON or empty array.
   */
  function setDeliveryAddress(address) {
    const url = `${ACC.config.encodedContextPath}/opf/cart/addresses/delivery`;

    return ajaxObservable({
      url,
      method: "POST",
      data: JSON.stringify(address),
    });
  }

  /**
   * Fetches active OPF payment configurations from the backend.
   */
  function fetchActiveConfigs() {
    const url = `${ACC.config.encodedContextPath}/opf-payment/active-configurations`;
    return fetchWithHandling(url).then((data) => data.value || []);
  }

  /**
   * POST: Create a guest user for the cart.
   * Returns Observable emitting response info.
   */
  function createCartGuestUser() {
    const url = `${ACC.config.contextPath}/opf/cart/guestuser`;
    return ajaxObservable({ url, method: "POST" });
  }

  /**
   * PATCH: Update guest user email on the cart.
   * @param {string} email - updated guest user email.
   * Returns Observable emitting update confirmation.
   */
  function updateCartGuestUserEmail(email) {
    const url = `${ACC.config.contextPath}/opf/cart/guestuser`;
    return ajaxObservable({
      url,
      method: "PATCH",
      data: JSON.stringify({ email }),
    });
  }

  /**
   * POST: Set / initiate payment information on the cart.
   * Returns Observable emitting response or empty array.
   */
  function setPaymentInfo() {
    const url = `${ACC.config.encodedContextPath}/opf/cart/paymentinfo`;
    return ajaxObservable({ url, method: "POST" });
  }

  window.OpfApis = {
    getSupportedDeliveryModes,
    setDeliveryMode,
    setDeliveryAddress,
    createCartGuestUser,
    updateCartGuestUserEmail,
    setPaymentInfo,
    fetchActiveConfigs,
  };
})(window);
