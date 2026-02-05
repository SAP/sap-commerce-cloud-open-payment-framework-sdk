<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="multi-checkout" tagdir="/WEB-INF/tags/responsive/checkout/multi"%>
<%@ taglib prefix="opf-multi-checkout" tagdir="/WEB-INF/tags/addons/opfb2bacceleratoraddon/responsive/checkout/multi" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/addons/opfb2bacceleratoraddon/responsive/common" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="address" tagdir="/WEB-INF/tags/responsive/address" %>
<spring:htmlEscape defaultHtmlEscape="true" />

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">

<!-- Displays a loading indicator during page processing -->
<common:opfPageLoader />

<div class="row">
    <!-- Opf Global Error Placeholder -->
    <div id="opf-global-error" class="opf-error-toast alert-danger" style="display:none;">
      <span class="message"></span>
      <button type="button" class="close-btn" aria-label="Close error message">&times;</button>
    </div>

    <div class="col-sm-6">
        <div class="checkout-headline">
            <span class="glyphicon glyphicon-lock"></span>
            <spring:theme code="checkout.multi.secure.checkout" />
        </div>
       <multi-checkout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
          <jsp:body>
             <div class="opf-payment-step">
             <ycommerce:testId code="checkoutStepThree">
             <!-- billing address-->
             <div class="opf-billing-headline">
                  <spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.billingAddress"/>
             </div>
              <form:form id="silentOrderPostForm" name="silentOrderPostForm" modelAttribute="sopPaymentDetailsForm" action="${paymentFormUrl}" method="POST">
                                             <input type="hidden" value="${fn:escapeXml(silentOrderPageData.parameters['billTo_email'])}" name="billTo_email" id="billTo_email">
                                     <c:if test="${cartData.deliveryItemsQuantity > 0}">
                                         <div id="useDeliveryAddressData"
                                             data-title="${fn:escapeXml(deliveryAddress.title)}"
                                             data-firstname="${fn:escapeXml(deliveryAddress.firstName)}"
                                             data-lastname="${fn:escapeXml(deliveryAddress.lastName)}"
                                             data-line1="${fn:escapeXml(deliveryAddress.line1)}"
                                             data-line2="${fn:escapeXml(deliveryAddress.line2)}"
                                             data-town="${fn:escapeXml(deliveryAddress.town)}"
                                             data-postalcode="${fn:escapeXml(deliveryAddress.postalCode)}"
                                             data-countryisocode="${fn:escapeXml(deliveryAddress.country.isocode)}"
                                             data-regionisocode="${fn:escapeXml(deliveryAddress.region.isocode)}"
                                             data-address-id="${fn:escapeXml(deliveryAddress.id)}"
                                         ></div>
                                         <formElement:formCheckbox
                                             path="useDeliveryAddress"
                                             idKey="useDeliveryAddress"
                                             labelKey="checkout.multi.sop.useMyDeliveryAddress"
                                             tabindex="11"/>
                                     </c:if>
                 <input type="hidden" value="${fn:escapeXml(silentOrderPageData.parameters['billTo_email'])}" class="text" name="billTo_email" id="billTo_email">

                 <!-- Billing Address Summary -->
                 <div id="billingAddressSummary"></div>

                 <!-- Billing Address Form -->
                 <div id="billingAddressFormContainer">
                     <address:billAddressFormSelector supportedCountries="${countries}" regions="${regions}" tabindex="12"/>
                     <div class="form-group">
                         <button type="button" class="btn btn-default" onclick="cancelBillingAddressEdit()">
                            <spring:theme code="checkout.multi.billingAddress.button.cancel"/>
                         </button>
                         <button type="button" class="btn btn-primary" onclick="saveBillingAddressEdit()">
                            <spring:theme code="checkout.multi.billingAddress.button.save"/>
                         </button>
                     </div>
                 </div>

               </form:form>
           <!-- billing address end-->

                <div class="opf-checkout-shipping">
                   <div class="checkout-indent">
                     <div class="opf-select-payment"><spring:theme code="checkout.summary.opfPayment.selectopfmethodfororder" /></div>
                     <spring:url var="selectOpfPaymentMethodUrl" value="{contextPath}/checkout/multi/opf-b2b-payment/select" htmlEscape="false" >
                         <spring:param name="contextPath" value="${request.contextPath}" />
                      </spring:url>
                      <form id="selectDeliveryMethodForm" action="${fn:escapeXml(selectOpfPaymentMethodUrl)}" method="get">
                         <div class="form-group">
                            <opf-multi-checkout:opfPaymentMethodSelector opfPaymentMethods="${opfPaymentMethods.value}"/>
                         </div>
                      </form>
                      <!--
                      <spring:theme var="deliveryMethodMessageHtml" code="checkout.multi.deliveryMethod.message" htmlEscape="false"/>
                      <p>${ycommerce:sanitizeHTML(deliveryMethodMessageHtml)}</p>
                      -->
                   </div>
                </div>
               </ycommerce:testId>
               </div>
          </jsp:body>
       </multi-checkout:checkoutSteps>
    </div>

    <div class="col-sm-6 hidden-xs">
       <multi-checkout:checkoutOrderDetails cartData="${cartData}" showDeliveryAddress="true" showPaymentInfo="false" showTaxEstimate="false" showTax="true" />
    </div>

     <div class="col-sm-12 col-lg-12">
        <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
            <cms:component component="${feature}"/>
        </cms:pageSlot>
    </div>
</div>

</template:page>

<script>
  // Get cart ID from JSP cart data, ensuring it's properly escaped
  const cartId = '${fn:escapeXml(cartData.code)}';

  /**
   * Initializes the UI toggle logic for using delivery address as billing address.
   */
  function initBillingAddressUIToggle() {
    const checkbox = $('#useDeliveryAddress');

    if (!checkbox.length) {
      console.warn('Checkbox #useDeliveryAddress not found.');
      return;
    }

    let isInitialLoad = true;

    /**
     * Updates the billing address UI based on checkbox state.
     */
    function updateBillingAddressUI() {
      const isChecked = checkbox.is(':checked');

      if (isChecked) {
        if (!isInitialLoad) {
          // On user interaction after initial load, use delivery address as billing address
          window.Opf.saveDeliveryAddress(cartId);
        }

        $('#billingAddressSummary').show();
        $('#billingAddressFormContainer').hide();
      } else {
        // Show form if not using delivery address
        $('#billingAddressFormContainer').show();
        $('#billingAddressSummary').hide();
      }

      isInitialLoad = false;
    }

    // Listen for checkbox changes
    checkbox.on('change', updateBillingAddressUI);
  }

  // Initialize toggle logic after DOM is ready
  initBillingAddressUIToggle();

  /**
   * Called when saving edits to the billing address form.
   */
  function saveBillingAddressEdit() {
    const isoCode = $('#address\\.country').val(); // Gets the selected ISO country code
    window.Opf.saveBillingAddressEdit(cartId, isoCode);
  }

  /**
   * Called when canceling billing address edit mode.
   */
  function cancelBillingAddressEdit() {
    window.Opf.cancelBillingAddressEdit();
  }
</script>