/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.service.impl;

import de.hybris.platform.client.OPFHttpClient;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.cta.request.OPFPaymentCTARequest;
import de.hybris.platform.data.response.OPFActiveConfigResponse;
import de.hybris.platform.cta.response.OPFPaymentCTAResponse;
import de.hybris.platform.opf.data.OPFInitiatePaymentSessionRequestData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteRequestData;
import de.hybris.platform.opf.data.request.OPFApplePayRequest;
import de.hybris.platform.opf.data.response.OPFApplePayResponse;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionResponse;
import de.hybris.platform.opf.dto.OPFPaymentAttribute;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteResponse;
import de.hybris.platform.opfservices.client.CCAdapterClientException;
import de.hybris.platform.opfservices.dtos.http.HttpClientRequestDto;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.opf.data.request.OPFPaymentSubmitRequest;
import de.hybris.platform.opf.data.response.OPFPaymentSubmitResponse;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.SAPGenericPaymentInfoModel;

import de.hybris.platform.opf.dto.OPFPaymentVerifyRequest;
import de.hybris.platform.opf.dto.OPFPaymentVerifyResponse;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DefaultOPFAcceleratorServiceTest {
    @Spy
    @InjectMocks
    private DefaultOPFAcceleratorService paymentService;

    @Mock
    private OPFHttpClient opfHttpClient;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private OPFPaymentCTARequest opfPaymentCTARequest;

    @Mock
    private CartService cartService;

    @Mock
    private ModelService modelService;

    String clientId = "mock-client-id";
    String publicKey = "mock-public-key";
    private String HTTPS = "https://";
    private String BASE_URL = "mock-base-url";
    @Test
    public void getCTAResponseReturnsValidResponseWhenRequestIsSuccessful() {
        OPFPaymentCTAResponse expectedResponse = new OPFPaymentCTAResponse();
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        doReturn("/mock-cta-url").when(configuration).getString("opf.cta.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenReturn(expectedResponse);

        OPFPaymentCTAResponse actualResponse = paymentService.getCTAResponse(opfPaymentCTARequest);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getCTAResponseThrowsExceptionWhenHttpClientFails() {
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        doReturn("/mock-cta-url").when(configuration).getString("opf.cta.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenThrow(new RuntimeException("HTTP client error"));

        Assertions.assertThrows(RuntimeException.class, () -> paymentService.getCTAResponse(opfPaymentCTARequest));
    }

    @Test
    public void getCTAResponseHandlesNullRequestBodyGracefully() {
        Assertions.assertThrows(NullPointerException.class, () -> paymentService.getCTAResponse(null));
    }

    @Test
    public void getActiveConfigurationsReturnsValidResponseWhenRequestIsSuccessful() {
        OPFActiveConfigResponse expectedResponse = new OPFActiveConfigResponse();
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        doReturn("/mock-active-config-url").when(configuration).getString("opf.active.config.url", StringUtils.EMPTY);

        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenReturn(expectedResponse);

        OPFActiveConfigResponse actualResponse = paymentService.getActiveConfigurations();

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getActiveConfigurationsThrowsExceptionWhenHttpClientFails() {
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        doReturn("/mock-active-config-url").when(configuration).getString("opf.active.config.url", StringUtils.EMPTY);

        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenThrow(new RuntimeException("HTTP client error"));

        Assertions.assertThrows(RuntimeException.class, () -> paymentService.getActiveConfigurations());
    }

    @Test
    public void getActiveConfigurationsReturnsNullWhenResponseIsEmpty() {
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        doReturn("").when(configuration).getString("opf.active.config.pageSize", StringUtils.EMPTY);
        doReturn("").when(configuration).getString("opf.active.config.pageNumber", StringUtils.EMPTY);
        doReturn("/mock-active-config-url").when(configuration).getString("opf.active.config.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenReturn(null);

        OPFActiveConfigResponse actualResponse = paymentService.getActiveConfigurations();

        Assertions.assertNull(actualResponse);
    }

    @Test
    void testGetInitiatePaymentResponse_returnsValidResponse() {
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        doReturn("/mock-payment-session-url").when(configuration).getString("opf.initiate.payment.session.url", StringUtils.EMPTY);
        // Mock security properties
        Mockito.when(opfHttpClient.getSecurityProperties()).thenReturn(Pair.of(clientId, publicKey));

        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenThrow(new RuntimeException("HTTP client error"));
        OPFInitiatePaymentSessionRequestData requestData = new OPFInitiatePaymentSessionRequestData();
        OPFInitiatePaymentSessionResponse expectedResponse = new OPFInitiatePaymentSessionResponse();

        when(opfHttpClient.httpExchange(anyString(), any())).thenReturn(expectedResponse);

        OPFInitiatePaymentSessionResponse actualResponse = paymentService.getInitiatePaymentResponse(requestData);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void submitPaymentReturnsValidResponseWhenRequestIsSuccessful() {
        OPFPaymentSubmitRequest request = new OPFPaymentSubmitRequest();
        OPFPaymentSubmitResponse expectedResponse = new OPFPaymentSubmitResponse();

        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn("/mock-submit-url").when(configuration).getString("opf.submit.url", StringUtils.EMPTY);
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenReturn(expectedResponse);
        OPFPaymentSubmitResponse actualResponse = paymentService.submitPayment(request, Boolean.FALSE);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void submitPaymentHandlesNullResponseGracefully() {
        OPFPaymentSubmitRequest request = new OPFPaymentSubmitRequest();
        HttpClientRequestDto<OPFPaymentSubmitResponse> httpRequest = new HttpClientRequestDto<>();
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn("/mock-submit-url").when(configuration).getString("opf.submit.url", StringUtils.EMPTY);
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(HTTPS + BASE_URL, httpRequest))
                .thenReturn(null);

        OPFPaymentSubmitResponse actualResponse = paymentService.submitPayment(request, Boolean.FALSE);

        Assertions.assertNull(actualResponse);
    }

    @Test
    void submitPaymentThrowsExceptionWhenHttpClientFails() {
        OPFPaymentSubmitRequest request = new OPFPaymentSubmitRequest();
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn("/mock-submit-url").when(configuration).getString("opf.submit.url", StringUtils.EMPTY);
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenThrow(new RuntimeException("HTTP client error"));

        Assertions.assertThrows(RuntimeException.class, () -> paymentService.submitPayment(request, Boolean.FALSE));
    }

    @Test
    void verifyPaymentReturnsValidResponseWhenRequestIsSuccessful() {
        OPFPaymentVerifyRequest request = new OPFPaymentVerifyRequest();
        OPFPaymentVerifyResponse expectedResponse = new OPFPaymentVerifyResponse();
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn("/mock-verify-url").when(configuration).getString("opf.verify.url", StringUtils.EMPTY);
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenReturn(expectedResponse);

        OPFPaymentVerifyResponse actualResponse = paymentService.verifyPayment(request);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void verifyPaymentHandlesNullResponseGracefully() {
        OPFPaymentVerifyRequest request = new OPFPaymentVerifyRequest();
        HttpClientRequestDto<OPFPaymentVerifyResponse> httpRequest = new HttpClientRequestDto<>();
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn("/mock-verify-url").when(configuration).getString("opf.verify.url", StringUtils.EMPTY);
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(HTTPS + BASE_URL, httpRequest))
                .thenReturn(null);

        OPFPaymentVerifyResponse actualResponse = paymentService.verifyPayment(request);

        Assertions.assertNull(actualResponse);
    }

    @Test
    void verifyPaymentThrowsExceptionWhenHttpClientFails() {
        OPFPaymentVerifyRequest request = new OPFPaymentVerifyRequest();
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn("/mock-verify-url").when(configuration).getString("opf.verify.url", StringUtils.EMPTY);
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenThrow(new RuntimeException("HTTP client error"));

        Assertions.assertThrows(RuntimeException.class, () -> paymentService.verifyPayment(request));
    }
    @Test
    void testCompletedPaymentResponse_returnsValidResponse() {
        // Arrange
        String expectedStatus = "COMPLETED";

        // Setup mock configuration
        Configuration configuration = mock(Configuration.class);
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString(eq("opf.base.url"), anyString())).thenReturn(HTTPS + BASE_URL);
        when(configuration.getString(eq("opf.payment.statuses"), anyString())).thenReturn("COMPLETED");

        // Setup request and expected response
        OPFPaymentSubmitCompleteRequestData requestData = new OPFPaymentSubmitCompleteRequestData();
        OPFPaymentSubmitCompleteResponse expectedResponse = new OPFPaymentSubmitCompleteResponse();
        expectedResponse.setStatus(expectedStatus);

        OPFPaymentAttribute customField = new OPFPaymentAttribute();
        customField.setKey("status");
        customField.setValue("COMPLETED");
        expectedResponse.setCustomFields(List.of(customField));

        // Mock HTTP response
        when(opfHttpClient.httpExchange(eq(HTTPS + BASE_URL), any(HttpClientRequestDto.class))).thenReturn(expectedResponse);

        // Act
        OPFPaymentSubmitCompleteResponse actualResponse = paymentService.getCompletedPaymentResponse(requestData);

        // Assert
        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expectedStatus, actualResponse.getStatus());
        Assertions.assertEquals("COMPLETED", actualResponse.getCustomFields().get(0).getValue());
    }

    @Test
    void getCompletedPaymentResponse_invalidStatus_throwsException() {
        // Arrange

        OPFPaymentSubmitCompleteRequestData requestData = new OPFPaymentSubmitCompleteRequestData();

        OPFPaymentSubmitCompleteResponse mockedResponse = new OPFPaymentSubmitCompleteResponse();
        mockedResponse.setStatus("REJECTED");
        mockedResponse.setCustomFields(Collections.emptyList());

        // Mock configuration
        Configuration mockConfig = mock(Configuration.class);
        when(configurationService.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.getString(eq("opf.base.url"), anyString())).thenReturn(HTTPS + BASE_URL);

        // Mock HTTP exchange
        when(opfHttpClient.httpExchange(eq(HTTPS + BASE_URL), any(HttpClientRequestDto.class))).thenReturn(mockedResponse);

        // Spy validatePaymentStatus to force false
        doReturn(false).when(paymentService).validatePaymentStatus(mockedResponse);

        // Act & Assert
        CCAdapterClientException exception = Assertions.assertThrows(CCAdapterClientException.class, () -> {
            paymentService.getCompletedPaymentResponse(requestData);
        });

        Assertions.assertTrue(exception.getMessage().contains("Payment validation failed"));
        Assertions.assertTrue(exception.getMessage().contains("REJECTED"));
    }

    @Test
    void validatePaymentStatus_bothStatusesValid_returnsTrue() {
        // Arrange
        Configuration configuration = mock(Configuration.class);
        OPFPaymentAttribute customField = new OPFPaymentAttribute();
        customField.setKey("status");
        customField.setValue("COMPLETED");
        OPFPaymentSubmitCompleteResponse response = new OPFPaymentSubmitCompleteResponse();
        response.setStatus("COMPLETED");
        response.setCustomFields(List.of(customField));
        Mockito.when(configurationService.getConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getString(eq("opf.payment.statuses"), anyString())).thenReturn("COMPLETED");

        // Act
        Boolean result = paymentService.validatePaymentStatus(response);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void validatePaymentStatus_invalidTopLevelStatus_returnsFalse() {
        // Arrange
        OPFPaymentAttribute customField = new OPFPaymentAttribute();
        customField.setKey("status");
        customField.setValue("COMPLETED");
        OPFPaymentSubmitCompleteResponse response = new OPFPaymentSubmitCompleteResponse();
        response.setStatus("FAILED"); // not in list
        response.setCustomFields(List.of(customField));

        doReturn("COMPLETED,PENDING").when(paymentService).getConfigurationValueForKey("opf.payment.statuses");

        // Act
        Boolean result = paymentService.validatePaymentStatus(response);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void validatePaymentStatus_invalidCustomFieldStatus_returnsFalse() {
        OPFPaymentAttribute customField = new OPFPaymentAttribute();
        customField.setKey("status");
        customField.setValue("INVALID");
        OPFPaymentSubmitCompleteResponse response = new OPFPaymentSubmitCompleteResponse();
        response.setStatus("COMPLETED"); // valid
        response.setCustomFields(List.of(customField));

        doReturn("COMPLETED,PENDING").when(paymentService).getConfigurationValueForKey("opf.payment.statuses");

        Boolean result = paymentService.validatePaymentStatus(response);
        Assertions.assertFalse(result);
    }

    @Test
    void validatePaymentStatus_missingCustomFieldStatus_returnsFalse() {
        OPFPaymentSubmitCompleteResponse response = new OPFPaymentSubmitCompleteResponse();
        response.setStatus("COMPLETED"); // valid
        response.setCustomFields(List.of()); // no custom field

        doReturn("COMPLETED,PENDING").when(paymentService).getConfigurationValueForKey("opf.payment.statuses");

        Boolean result = paymentService.validatePaymentStatus(response);
        Assertions.assertFalse(result);
    }

    @Test
    void setPaymentInfo_createsPaymentInfoWhenNoneExists() {
        CartModel cartModel = mock(CartModel.class);
        SAPGenericPaymentInfoModel paymentInfo = mock(SAPGenericPaymentInfoModel.class);
        when(cartService.getSessionCart()).thenReturn(cartModel);
        when(cartModel.getPaymentInfo()).thenReturn(null);
        when(cartModel.getUser()).thenReturn(mock(UserModel.class));
        when(modelService.create(SAPGenericPaymentInfoModel.class)).thenReturn(paymentInfo);

        paymentService.setPaymentInfo();

        verify(modelService).create(SAPGenericPaymentInfoModel.class);
        verify(modelService).save(paymentInfo);
        verify(cartModel).setPaymentInfo(paymentInfo);
        verify(modelService).save(cartModel);
        verify(modelService).refresh(cartModel);
    }

    @Test
    void setPaymentInfo_doesNothingWhenPaymentInfoExists() {
        CartModel cartModel = mock(CartModel.class);
        SAPGenericPaymentInfoModel existingPaymentInfo = mock(SAPGenericPaymentInfoModel.class);

        when(cartService.getSessionCart()).thenReturn(cartModel);
        when(cartModel.getPaymentInfo()).thenReturn(existingPaymentInfo);

        paymentService.setPaymentInfo();

        verify(modelService, never()).create(SAPGenericPaymentInfoModel.class);
        verify(modelService, never()).save(any());
        verify(cartModel, never()).setPaymentInfo(any());
    }

    @Test
    void setPaymentInfo_handlesNullCartGracefully() {

        when(cartService.getSessionCart()).thenReturn(null);

        paymentService.setPaymentInfo();

        verify(modelService, never()).create(SAPGenericPaymentInfoModel.class);
        verify(modelService, never()).save(any());
    }

    @Test
    void getApplePayWebSession_validRequest_returnsPopulatedResponse() {
        OPFApplePayRequest request = new OPFApplePayRequest();
        OPFApplePayResponse expectedResponse = new OPFApplePayResponse();
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn("/mock-applepay-web-session-url").when(configuration).getString("opf.applepay.web.session.url", StringUtils.EMPTY);
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenReturn(expectedResponse);

        OPFApplePayResponse actualResponse = paymentService.getApplePayWebSession(request);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getApplePayWebSession_nullRequest_throwsException() {
        Assertions.assertThrows(NullPointerException.class, () -> paymentService.getApplePayWebSession(null));
    }

    @Test
    void getApplePayWebSession_httpClientReturnsNull_returnsNullResponse() {
        OPFApplePayRequest request = new OPFApplePayRequest();
        Configuration configuration = mock(Configuration.class);
        doReturn(configuration).when(configurationService).getConfiguration();
        doReturn("/mock-applepay-web-session-url").when(configuration).getString("opf.applepay.web.session.url", StringUtils.EMPTY);
        doReturn(HTTPS + BASE_URL).when(configuration).getString("opf.base.url", StringUtils.EMPTY);
        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL), Mockito.any(HttpClientRequestDto.class)))
                .thenReturn(null);

        OPFApplePayResponse actualResponse = paymentService.getApplePayWebSession(request);

        Assertions.assertNull(actualResponse);
    }

    @Test
    void getApplePayWebSession_httpClientThrowsException_propagatesException() {
        OPFApplePayRequest request = new OPFApplePayRequest();

        Mockito.when(opfHttpClient.httpExchange(Mockito.eq(HTTPS + BASE_URL ), Mockito.any(HttpClientRequestDto.class)))
                .thenThrow(new RuntimeException("HTTP client error"));

        Assertions.assertThrows(RuntimeException.class, () -> paymentService.getApplePayWebSession(request));
    }

}


