/*
 * [y] hybris Platform
 *
 * Copyright (c) 2025 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * Confidential and proprietary information of SAP.
 */

(function (window) {
  /**
   * Unified AJAX utility returning an RxJS Observable.
   * Handles all HTTP verbs, errors, cancellation, and content types.
   * @param {object} config - {url, method, data, headers, parseJson}
   */
  function ajaxObservable({
    url,
    method = "GET",
    data = null,
    headers = {},
    parseJson = true,
  }) {
    const urlWithContext = `${ACC.config.encodedContextPath}${url}`;
    return new rxjs.Observable((observer) => {
      const useContentType =
        headers["Content-Type"] ||
        (["POST", "PUT", "PATCH"].includes(method)
          ? "application/json"
          : undefined);

      const jqXHR = $.ajax({
        url: urlWithContext,
        method,
        contentType: useContentType,
        data,
        headers,
        success: (response) => {
          observer.next(parseJson ? response : response);
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
      // RxJS cancellation
      return () => jqXHR && jqXHR.abort && jqXHR.abort();
    });
  }

  /**
   * Fetches delivery modes; returns Observable emitting array.
   */
  function getSupportedDeliveryModes() {
    return ajaxObservable({ url: "/opf/cart/deliverymodes" }).pipe(
      rxjs.operators.map((data) => data.deliveryModes || [])
    );
  }

  /**
   * Sets delivery mode; returns Observable.
   */
  function setDeliveryMode(modeId) {
    return ajaxObservable({
      url: `/opf/cart/deliverymode?deliveryModeId=${modeId}`,
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      parseJson: false,
    });
  }

  /**
   * Sets delivery address; returns Observable.
   */
  function setDeliveryAddress(address) {
    return ajaxObservable({
      url: "/opf/cart/addresses/delivery",
      method: "POST",
      data: JSON.stringify(address),
    });
  }

  /**
   * Fetches active OPF payment configurations as a Promise.
   */
  function fetchActiveConfigs() {
    const urlWithContext = `${ACC.config.encodedContextPath}/opf-payment/active-configurations`;
    return new Promise((resolve, reject) => {
      $.ajax({
        url: urlWithContext,
        method: "GET",
        contentType: "application/json",
        success: (data) => resolve(data.value || []),
        error: (xhr, status, error) =>
          reject(
            new Error(
              `AJAX error! Status: ${xhr.status || status}. Message: ${error}`
            )
          ),
      });
    });
  }

  /**
   * Creates a guest user for the cart; returns Observable.
   */
  function createCartGuestUser() {
    return ajaxObservable({
      url: "/opf/cart/guestuser",
      method: "POST",
    });
  }

  /**
   * Updates guest user email for the cart; returns Observable.
   */
  function updateCartGuestUserEmail(email) {
    return ajaxObservable({
      url: "/opf/cart/guestuser",
      method: "PATCH",
      data: JSON.stringify({ email }),
    });
  }

  /**
   * Sets/initiates payment info on the cart; returns Observable.
   */
  function setPaymentInfo() {
    return ajaxObservable({
      url: "/opf/cart/paymentinfo",
      method: "POST",
    });
  }

  /**
   * ApplePay web session request; returns Observable.
   */
  function getApplePayWebSession(request) {
    return ajaxObservable({
      url: "/opf-payment/applepay-web-session",
      method: "POST",
      data: JSON.stringify(request),
    });
  }

  window.OpfApis = {
    getSupportedDeliveryModes,
    setDeliveryMode,
    setDeliveryAddress,
    createCartGuestUser,
    updateCartGuestUserEmail,
    setPaymentInfo,
    fetchActiveConfigs,
    getApplePayWebSession,
  };
})(window);
