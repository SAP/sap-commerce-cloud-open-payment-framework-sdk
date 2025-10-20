<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/addons/opfb2bacceleratoraddon/responsive/common" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<spring:htmlEscape defaultHtmlEscape="true" />

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">

  <!-- Displays a loading indicator during page processing -->
  <common:opfPageLoader />

  <p class="center-text">
    <spring:theme code="checkout.multi.paymentVerificationRedirect.processingPayment"/>
  </p>

</template:page>

<script>
  // Convert server-side requestMap to a usable JavaScript object
  const requestMap = {
    <c:forEach var="entry" items="${requestMap}" varStatus="loopStatus">
      "${fn:escapeXml(entry.key)}": "${fn:escapeXml(entry.value)}"<c:if test="${!loopStatus.last}">,</c:if>
    </c:forEach>
  };

  // Trigger payment verification
  window.Opf.verifyPayment(requestMap);
</script>
