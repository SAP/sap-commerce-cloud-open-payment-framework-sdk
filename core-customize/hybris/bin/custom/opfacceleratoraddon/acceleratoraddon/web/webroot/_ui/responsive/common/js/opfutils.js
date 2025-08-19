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
      firstName: AppConstants.DEFAULT_FIELD_VALUE,
      lastName: AppConstants.DEFAULT_FIELD_VALUE,
      country: {
        isocode: address?.countryCode,
      },
      town: address?.locality,
      district: address?.administrativeArea,
      postalCode: address?.postalCode,
      line1: address?.address1 || AppConstants.DEFAULT_FIELD_VALUE,
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
  function getEnabledApplePayConfig(config) {
    if (!config || !Array.isArray(config.digitalWalletQuickBuy)) {
      return [];
    }

    return (
      config.digitalWalletQuickBuy.find(
        (wallet) => wallet.provider === "APPLE_PAY" && wallet.enabled === true
      ) || null
    );
  }

  /**
   * Converts Apple Pay address to OPF format.
   * Fills missing fields with placeholders if partial is true.
   * @param {object} addr - Apple Pay address object
   * @param {boolean} partial - whether to fill missing fields with placeholders
   * @returns {object} converted address in OPF format
   */
  function convertAppleToOpfAddress(addr, partial = false) {
    const ADDRESS_FIELD_PLACEHOLDER = AppConstants.DEFAULT_FIELD_VALUE;
    return {
      firstName: partial ? ADDRESS_FIELD_PLACEHOLDER : addr?.givenName,
      lastName: partial ? ADDRESS_FIELD_PLACEHOLDER : addr?.familyName,
      line1: partial ? ADDRESS_FIELD_PLACEHOLDER : addr?.addressLines?.[0],
      line2: addr?.addressLines?.[1],
      email: addr?.emailAddress,
      town: addr?.locality,
      district: addr?.administrativeArea,
      postalCode: addr?.postalCode,
      phone: addr?.phoneNumber,
      country: {
        isocode: addr?.countryCode,
        name: addr?.country,
      },
      defaultAddress: false,
    };
  }

  // Aggregate and expose all functions as OpfUtils object on window
  window.OpfUtils = {
    loadCartData,
    isEmail,
    isUserLoggedIn,
    isGuestCart,
    convertAddress,
    getFirstAndLastName,
    removeLeadingSlashes,
    getEnabledApplePayConfig,
    convertAppleToOpfAddress,
    // generateOpt,
  };
})(window);
