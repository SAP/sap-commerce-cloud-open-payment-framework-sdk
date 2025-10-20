<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="opfPaymentMethods" required="true" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="opf-multi-checkout" tagdir="/WEB-INF/tags/addons/opfb2bacceleratoraddon/responsive/checkout/multi" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<!-- Payment gateway content -->
<div id="payment-gateway-loader" style="display: none; padding: 5px 10px">Loading payment gateway...</div>
<div id="payment-gateway-error" style="display: none; color: red; padding: 5px 10px"></div>
<div id="payment-gateway-container"></div>

<script>
function initiatePayment(){
 const paymentId = '${fn:escapeXml(cartData.opfB2BPaymentId)}';
     window.Opf.fetchPaymentMethodContent(
          paymentId,
          '<spring:theme code="checkout.multi.paymentMethod.button.proceedPayment" />'
        );
}

 window.onload=initiatePayment;
</script>
