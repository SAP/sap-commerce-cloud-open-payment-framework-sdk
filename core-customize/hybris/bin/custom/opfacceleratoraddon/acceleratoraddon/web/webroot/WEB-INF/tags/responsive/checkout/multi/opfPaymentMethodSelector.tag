<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="opfPaymentMethods" required="true" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="opf-multi-checkout" tagdir="/WEB-INF/tags/addons/opfacceleratoraddon/responsive/checkout/multi" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- Custom payment method dropdown -->
<div class="opf-custom-select" id="opf-payment-method-dropdown">
  <div class="opf-selected-option">
    <spring:theme code="checkout.multi.paymentMethod.selectPaymentMethod" />
  </div>
  <ul class="opf-options-list" style="display: none;">
    <c:forEach items="${opfPaymentMethods}" var="opfPaymentMethod">
      <li data-id="${fn:escapeXml(opfPaymentMethod.id)}">
        <span>${fn:escapeXml(opfPaymentMethod.displayName)}</span>
        <c:if test="${not empty opfPaymentMethod.logoUrl}">
          <img src="${opfPaymentMethod.logoUrl}" alt="payment-method-img" />
        </c:if>
      </li>
    </c:forEach>
  </ul>
</div>

<!-- Payment gateway content -->
<div id="payment-gateway-loader" style="display: none; padding: 5px 10px">Loading payment gateway...</div>
<div id="payment-gateway-error" style="display: none; color: red; padding: 5px 10px"></div>
<div id="payment-gateway-container"></div>

<script>
  // ------------------------------
  // Triggered when payment method changes
  // ------------------------------
  function onPaymentMethodChange(selectedPaymentMethod) {
    if (!selectedPaymentMethod?.value) {
      console.warn('Selected payment method not found.');
      return;
    }

    window.Opf.fetchPaymentMethodContent(
      selectedPaymentMethod.value,
      '<spring:theme code="checkout.multi.paymentMethod.button.proceedPayment" />'
    );
  }

  (function () {
    const dropdown = document.getElementById('opf-payment-method-dropdown');
    const selected = dropdown.querySelector('.opf-selected-option');
    const optionsList = dropdown.querySelector('.opf-options-list');

    // Toggle dropdown visibility
    selected.addEventListener('click', () => {
      const isOpen = optionsList.style.display === 'block';
      optionsList.style.display = isOpen ? 'none' : 'block';
    });

    // Handle option selection
    optionsList.addEventListener('click', (e) => {
      const option = e.target.closest('li');
      if (!option) return;

      const methodId = option.dataset.id;
      const methodName = option.querySelector('span').textContent;
      const methodLogo = option.querySelector('img')?.cloneNode(true);

      // Replace content of selected display
      selected.innerHTML = '';
      selected.append(methodName);
      if (methodLogo) {
        selected.append(' ');
        selected.appendChild(methodLogo);
      }

      // Trigger logic (same as native select)
      onPaymentMethodChange({ value: methodId });

      optionsList.style.display = 'none';
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', (e) => {
      if (!dropdown.contains(e.target)) {
        optionsList.style.display = 'none';
      }
    });
  })();
</script>
