<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<script id="quickbuy-cart-data-json" type="application/json">
  {
    "guid": "${cartData.guid}",
    "user": {
      "uid": "${cartData.user.uid}",
      "name": "${cartData.user.name}"
    },
    "deliveryItemsQuantity": "${cartData.deliveryItemsQuantity}",
    "site": "${cartData.site}",
    "store": "${cartData.store}",
    "code": "${cartData.code}",
    "totalItems": "${cartData.totalUnitCount}",
    "totalPrice": {
      "value": "${cartData.totalPrice.value}",
      "currencyIso": "${cartData.totalPrice.currencyIso}"
    },
    "totalPriceWithTax": {
      "value": "${cartData.totalPriceWithTax.value}",
      "currencyIso": "${cartData.totalPriceWithTax.currencyIso}"
    }
  }
</script>
<div id="cx-opf-quick-buy-buttons">
<div class="cx-opf-google-pay-button" id="googlePayButtonContainer"></div>
</div>
