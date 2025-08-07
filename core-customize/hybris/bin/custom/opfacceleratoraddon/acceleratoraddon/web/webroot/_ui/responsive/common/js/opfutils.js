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
  /**
   * Parses cart data JSON embedded in a script tag with ID 'quickbuy-cart-data-json'.
   * Sets global window.cartData on success.
   */
  function loadCartData() {
    const cartDataScript = document.getElementById("quickbuy-cart-data-json");

    if (cartDataScript) {
      try {
        const cartData = JSON.parse(cartDataScript.textContent || "{}");
        window.cartData = cartData;
      } catch (error) {
        console.error("Error parsing cart data:", error);
      }
    }
  }

  /**
   * Validates whether the input string matches email pattern.
   * @param {string} email
   * @returns {boolean}
   */
  function isEmail(email) {
    return !!(email && email.match(AppConstants.EMAIL_PATTERN));
  }

  /**
   * Determines if the user in cartData is logged in (non-anonymous).
   * Returns an RxJS Observable emitting a boolean.
   * @param {object} cartData
   * @returns {Observable<boolean>}
   */
  function isUserLoggedIn(cartData) {
    const cartUser = cartData?.user;
    return rxjs.of(
      Boolean(cartUser && cartUser.uid !== AppConstants.OCC_USER_ID_ANONYMOUS)
    );
  }

  /**
   * Determines if the cart belongs to a guest user.
   * Checks user name 'guest' or if uid contains email.
   * Returns an RxJS Observable emitting a boolean.
   * @param {object} cartData
   * @returns {Observable<boolean>}
   */
  function isGuestCart(cartData) {
    const cartUser = cartData?.user;
    const uidEmailPart = cartUser?.uid?.split("|").slice(1).join("|");
    return rxjs.of(
      Boolean(
        cartUser &&
          (cartUser.name === AppConstants.OCC_USER_ID_GUEST ||
            isEmail(uidEmailPart))
      )
    );
  }

  /**
   * Converts an address object from external to internal format.
   * Fills missing fields with placeholders.
   * @param {object} address
   * @returns {object} converted address
   */
  function convertAddress(address) {
    let convertedAddress = {
      firstName: "[FIELD_NOT_SET]",
      lastName: "[FIELD_NOT_SET]",
      country: {
        isocode: address?.countryCode,
      },
      town: address?.locality,
      district: address?.administrativeArea,
      postalCode: address?.postalCode,
      line1: address?.address1 || "[FIELD_NOT_SET]",
      line2: `${address?.address2 || ""} ${address?.address3 || ""}`.trim(),
    };

    if (address?.name) {
      convertedAddress = {
        ...convertedAddress,
        ...getFirstAndLastName(address.name),
      };
    }

    return convertedAddress;
  }

  /**
   * Splits full name into first and last name.
   * @param {string} name
   * @returns {{firstName: string, lastName: string}}
   */
  function getFirstAndLastName(name) {
    const firstName = name?.split(" ")[0] || "";
    const lastName = name?.substring(firstName.length).trim() || firstName;
    return { firstName, lastName };
  }

  /**
   * Removes leading slashes from a path string.
   * @param {string} path
   * @returns {string}
   */
  function removeLeadingSlashes(path) {
    return (path || "").replace(/^\/+/, "");
  }

  /**
   * Filters enabled quick buy wallets from configuration.
   * Returns empty array if input invalid.
   * @param {object} config
   * @returns {Array}
   */
  function getEnabledQuickBuyWallet(config) {
    if (!config || !Array.isArray(config.digitalWalletQuickBuy)) {
      return [];
    }

    return config.digitalWalletQuickBuy.filter(
      (wallet) => wallet.enabled === true
    );
  }

  /*
  function generateOpt() {
    setCartData();
    const jsonData = {
      cartCode: cartData.code,
      cartGuid: cartData.guid,
      creationTime: Date.now().toString(),
    };

    const jsonString = JSON.stringify(jsonData);
    const otpKey = btoa(jsonString);

    return otpKey;
  }
  */

  // Aggregate and expose all functions as OpfUtils object on window
  window.OpfUtils = {
    loadCartData,
    isEmail,
    isUserLoggedIn,
    isGuestCart,
    convertAddress,
    getFirstAndLastName,
    removeLeadingSlashes,
    getEnabledQuickBuyWallet,
    // generateOpt,
  };
})(window);
