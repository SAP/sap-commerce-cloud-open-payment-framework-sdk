/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.facade.impl;

import com.opf.dto.cta.CTARequestDTO;
import com.opf.dto.cta.CTAResponseDTO;
import com.opf.dto.cta.OPFActiveConfigDTO;
import com.opf.dto.request.OPFApplePayRequestDTO;
import com.opf.dto.submit.OPFPaymentSubmitRequestDTO;
import com.opf.dto.submit.OPFPaymentSubmitResponseDTO;
import com.opf.dto.verify.OPFPaymentVerifyRequestDTO;
import de.hybris.platform.cta.request.OPFPaymentCTARequest;
import de.hybris.platform.cta.response.OPFPaymentCTAResponse;
import de.hybris.platform.data.response.OPFActiveConfigResponse;
import de.hybris.platform.opf.data.*;
import de.hybris.platform.opf.data.request.OPFApplePayRequest;
import de.hybris.platform.opf.data.request.OPFPaymentSubmitRequest;
import de.hybris.platform.opf.data.response.OPFApplePayResponse;
import de.hybris.platform.opf.data.response.OPFPaymentSubmitResponse;
import de.hybris.platform.opf.dto.*;
import de.hybris.platform.service.OPFAcceleratorService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import de.hybris.platform.opf.dto.user.AddressWsDTO;
import de.hybris.platform.opf.dto.user.CountryWsDTO;
import de.hybris.platform.opf.dto.user.RegionWsDTO;
import de.hybris.platform.commercefacades.user.data.AddressData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DefaultOPFAcceleratorFacadeTest {

    @InjectMocks
    private DefaultOPFAcceleratorFacade paymentFacade;

    @Mock
    private OPFAcceleratorService opfAcceleratorService;

    @Mock
    private Converter<CTARequestDTO, OPFPaymentCTARequest> opfAcceleratorCTARequestConverter;

    @Mock
    private Converter<OPFPaymentCTAResponse, CTAResponseDTO> opfAcceleratorCTAResponseConverter;

    @Mock
    private CTARequestDTO ctaRequestWsDTO;

    @Mock
    private OPFPaymentCTARequest opfPaymentCTARequest;

    @Mock
    private OPFPaymentCTAResponse opfPaymentCTAResponse;

    @Mock
    private CTAResponseDTO ctaResponseWsDTO;
    @Mock
    private Converter<OPFApplePayRequestDTO, OPFApplePayRequest> opfApplePayRequestConverter;

    @Mock
    private Converter<OPFInitiatePaymentSessionRequest, OPFInitiatePaymentSessionRequestData> requestConverter;
    @Mock
    private Converter<OPFPaymentSubmitCompleteRequest, OPFPaymentSubmitCompleteRequestData> opfPaymentSubmitCompleteRequestConverter;
    @Mock
    private Converter<OPFPaymentSubmitCompleteResponse, OPFPaymentSubmitCompleteResponseData> opfPaymentSubmitCompleteResponseConverter;

    @Mock
    private Converter<OPFInitiatePaymentSessionResponse, OPFInitiatePaymentData> responseConverter;
    private OPFInitiatePaymentSessionRequest paymentRequest;
    private OPFInitiatePaymentSessionResponse response;

    @Mock
    private Converter<OPFActiveConfigResponse, OPFActiveConfigDTO> opfAcceleratorActiveConfigResponseConverter;
    @Mock
    private Converter<OPFPaymentSubmitRequestDTO, OPFPaymentSubmitRequest> opfAcceleratorSubmitRequestConverter;
    @Mock
    private Converter<OPFPaymentSubmitResponse, OPFPaymentSubmitResponseDTO> opfAcceleratorSubmitResponseConverter;
    @Mock
    private Converter<OPFPaymentVerifyRequestDTO, OPFPaymentVerifyRequest> opfAcceleratorVerifyRequestConverter;

    @BeforeEach
    void setUp() {
        paymentRequest = new OPFInitiatePaymentSessionRequest();
        response = new OPFInitiatePaymentSessionResponse();
    }

    @Test
    public void getCTAResponseReturnsPopulatedResponseWhenRequestAndServiceResponseAreValid() {
        Mockito.when(opfAcceleratorCTARequestConverter.convert(ctaRequestWsDTO, opfPaymentCTARequest)).thenReturn(opfPaymentCTARequest);
        Mockito.when(opfAcceleratorService.getCTAResponse(opfPaymentCTARequest)).thenReturn(opfPaymentCTAResponse);
        Mockito.when(opfAcceleratorCTAResponseConverter.convert(opfPaymentCTAResponse, ctaResponseWsDTO)).thenReturn(ctaResponseWsDTO);

        CTAResponseDTO result = paymentFacade.getCTAResponse(ctaRequestWsDTO);

        Assertions.assertNotNull(result);

        Mockito.verify(opfAcceleratorService).getCTAResponse(any(OPFPaymentCTARequest.class));

    }

    @Test
    public void getCTAResponseReturnsEmptyResponseWhenRequestIsNull() {
        CTAResponseDTO result = paymentFacade.getCTAResponse(null);

        Assertions.assertNotNull(result);
        Mockito.verifyNoInteractions(opfAcceleratorCTARequestConverter, opfAcceleratorService, opfAcceleratorCTAResponseConverter);
    }

    @Test
    public void getCTAResponseReturnsEmptyResponseWhenServiceResponseIsNull() {
        Mockito.when(opfAcceleratorCTARequestConverter.convert(ctaRequestWsDTO, opfPaymentCTARequest)).thenReturn(opfPaymentCTARequest);
        Mockito.when(opfAcceleratorService.getCTAResponse(opfPaymentCTARequest)).thenReturn(null);

        CTAResponseDTO result = paymentFacade.getCTAResponse(ctaRequestWsDTO);

        Assertions.assertNotNull(result);

        Mockito.verify(opfAcceleratorService).getCTAResponse(any(OPFPaymentCTARequest.class));

    }

    @Test
    public void getCTAResponseThrowsExceptionWhenRequestConverterFails() {
        Mockito.doThrow(new RuntimeException("Converter error")).when(opfAcceleratorCTARequestConverter)
                .convert(any(CTARequestDTO.class), any(OPFPaymentCTARequest.class));

        Assertions.assertThrows(RuntimeException.class, () -> paymentFacade.getCTAResponse(ctaRequestWsDTO));
        Mockito.verify(opfAcceleratorCTARequestConverter).convert(any(CTARequestDTO.class), any(OPFPaymentCTARequest.class));
        Mockito.verifyNoInteractions(opfAcceleratorService, opfAcceleratorCTAResponseConverter);
    }

    @Test
    void testGetInitiatePaymentResponse_withValidRequest_shouldReturnConvertedData() {
        Mockito.doAnswer(invocation -> {
            OPFInitiatePaymentSessionRequest src = invocation.getArgument(0);
            OPFInitiatePaymentSessionRequestData dest = invocation.getArgument(1);
            return null;
        }).when(requestConverter).convert(any(OPFInitiatePaymentSessionRequest.class), any(OPFInitiatePaymentSessionRequestData.class));
        Mockito.when(opfAcceleratorService.getInitiatePaymentResponse(any(OPFInitiatePaymentSessionRequestData.class)))
                .thenReturn(response);
        Mockito.doAnswer(invocation -> {
            OPFInitiatePaymentSessionResponse src = invocation.getArgument(0);
            OPFInitiatePaymentData dest = invocation.getArgument(1);
            return null;
        }).when(responseConverter).convert(any(OPFInitiatePaymentSessionResponse.class), any(OPFInitiatePaymentData.class));
        OPFInitiatePaymentData actual = paymentFacade.getInitiatePaymentResponse(paymentRequest);
        Assertions.assertNotNull(actual);
        Mockito.verify(opfAcceleratorService).getInitiatePaymentResponse(any(OPFInitiatePaymentSessionRequestData.class));
    }

    @Test
    void testGetInitiatePaymentResponse_withNullRequest_shouldReturnEmptyData() {
        OPFInitiatePaymentData result = paymentFacade.getInitiatePaymentResponse(null);
        Assertions.assertNotNull(result);
        Mockito.verifyNoInteractions(requestConverter, opfAcceleratorService, responseConverter);
    }

    @Test
    void getCompletedPaymentResponse_nullRequest_returnsEmptyResponse() {
        // Act
        OPFPaymentSubmitCompleteResponseData result = paymentFacade.getCompletedPaymentResponse(null);

        // Assert
        Assertions.assertNotNull(result);
    }

    @Test
    void getCompletedPaymentResponse_validRequestWithValidResponse_callsConverters() {
        // Arrange
        OPFPaymentSubmitCompleteRequest inputRequest = new OPFPaymentSubmitCompleteRequest();
        OPFPaymentSubmitCompleteResponse paymentSubmitCompleteResponse = new OPFPaymentSubmitCompleteResponse();
        paymentSubmitCompleteResponse.setOrderPaymentId("ORDER123");
        paymentSubmitCompleteResponse.setStatus("COMPLETED");

        // Mock service call
        Mockito.when(opfAcceleratorService.getCompletedPaymentResponse(any())).thenReturn(paymentSubmitCompleteResponse);

        // Act
        OPFPaymentSubmitCompleteResponseData actualResponse = paymentFacade.getCompletedPaymentResponse(inputRequest);

        // Assert
        Assertions.assertNotNull(actualResponse);
        Mockito.verify(opfAcceleratorService).getCompletedPaymentResponse(any());
    }

    @Test
    void getCompletedPaymentResponse_responseWithNullFields_doesNotConvert() {
        // Arrange
        OPFPaymentSubmitCompleteRequest inputRequest = new OPFPaymentSubmitCompleteRequest();
        OPFPaymentSubmitCompleteResponse opfPaymentSubmitCompleteResponse = new OPFPaymentSubmitCompleteResponse();
        opfPaymentSubmitCompleteResponse.setOrderPaymentId(null);  // Invalid case
        opfPaymentSubmitCompleteResponse.setStatus("");            // Invalid case
        // Mock service call
        Mockito.when(opfAcceleratorService.getCompletedPaymentResponse(any())).thenReturn(opfPaymentSubmitCompleteResponse);

        // Act
        OPFPaymentSubmitCompleteResponseData actualResponse = paymentFacade.getCompletedPaymentResponse(inputRequest);

        // Assert
        Assertions.assertNotNull(actualResponse);
    }

    @Test
    void getActiveConfigurations_validResponse_returnsPopulatedDTO() {
        OPFActiveConfigResponse activeConfigResponse = new OPFActiveConfigResponse();
        OPFActiveConfigDTO activeConfigDTO = new OPFActiveConfigDTO();

        Mockito.when(opfAcceleratorService.getActiveConfigurations()).thenReturn(activeConfigResponse);
        Mockito.doAnswer(invocation -> {
            OPFActiveConfigResponse src = invocation.getArgument(0);
            OPFActiveConfigDTO dest = invocation.getArgument(1);
            return null;
        }).when(opfAcceleratorActiveConfigResponseConverter).convert(activeConfigResponse, activeConfigDTO);

        OPFActiveConfigDTO result = paymentFacade.getActiveConfigurations();

        Assertions.assertNotNull(result);
        Mockito.verify(opfAcceleratorService).getActiveConfigurations();

    }

    @Test
    void getActiveConfigurations_nullResponse_returnsEmptyDTO() {
        Mockito.when(opfAcceleratorService.getActiveConfigurations()).thenReturn(null);

        OPFActiveConfigDTO result = paymentFacade.getActiveConfigurations();

        Assertions.assertNotNull(result);
        Mockito.verify(opfAcceleratorService).getActiveConfigurations();

    }

    @Test
    void submitPayment_validRequestWithSessionId_returnsPopulatedResponse() {
        OPFPaymentSubmitRequestDTO requestDTO = new OPFPaymentSubmitRequestDTO();
        OPFPaymentSubmitResponse submitResponse = new OPFPaymentSubmitResponse();
        OPFPaymentSubmitResponseDTO responseDTO = new OPFPaymentSubmitResponseDTO();
        OPFPaymentSubmitRequest opfPaymentSubmitRequest = new OPFPaymentSubmitRequest();

        Mockito.when(opfAcceleratorSubmitRequestConverter.convert(requestDTO, opfPaymentSubmitRequest)).thenReturn(opfPaymentSubmitRequest);
        Mockito.when(opfAcceleratorService.submitPayment(opfPaymentSubmitRequest, false)).thenReturn(submitResponse);
        Mockito.when(opfAcceleratorSubmitResponseConverter.convert(submitResponse, responseDTO)).thenReturn(responseDTO);

        OPFPaymentSubmitResponseDTO result = paymentFacade.submitPayment(requestDTO, "SESSION123", "192.168.1.1");

        Assertions.assertNotNull(result);

        Mockito.verify(opfAcceleratorService).submitPayment(any(OPFPaymentSubmitRequest.class), anyBoolean());

    }

    @Test
    void submitPayment_nullRequest_returnsEmptyResponse() {
        OPFPaymentSubmitResponseDTO result = paymentFacade.submitPayment(null, "SESSION123", "192.168.1.1");

        Assertions.assertNotNull(result);
        Mockito.verifyNoInteractions(opfAcceleratorSubmitRequestConverter, opfAcceleratorService, opfAcceleratorSubmitResponseConverter);
    }

    @Test
    void submitPayment_quickBuyRequest_doesNotSetSessionId() {
        OPFPaymentSubmitRequestDTO requestDTO = new OPFPaymentSubmitRequestDTO();
        requestDTO.setPaymentMethod("GPay");
        OPFPaymentSubmitRequest submitRequest = new OPFPaymentSubmitRequest();
        OPFPaymentSubmitResponse submitResponse = new OPFPaymentSubmitResponse();
        OPFPaymentSubmitResponseDTO responseDTO = new OPFPaymentSubmitResponseDTO();

        Mockito.when(opfAcceleratorSubmitRequestConverter.convert(requestDTO, submitRequest)).thenReturn(submitRequest);
        Mockito.when(opfAcceleratorService.submitPayment(submitRequest, true)).thenReturn(submitResponse);
        Mockito.when(opfAcceleratorSubmitResponseConverter.convert(submitResponse, responseDTO)).thenReturn(responseDTO);

        OPFPaymentSubmitResponseDTO result = paymentFacade.submitPayment(requestDTO, null, "192.168.1.1");

        Assertions.assertNotNull(result);

        Mockito.verify(opfAcceleratorService).submitPayment(any(OPFPaymentSubmitRequest.class), anyBoolean());

    }

    @Test
    void submitPayment_serviceReturnsNull_returnsEmptyResponse() {
        OPFPaymentSubmitRequestDTO requestDTO = new OPFPaymentSubmitRequestDTO();
        OPFPaymentSubmitRequest submitRequest = new OPFPaymentSubmitRequest();

        Mockito.when(opfAcceleratorSubmitRequestConverter.convert(requestDTO, submitRequest)).thenReturn(submitRequest);
        Mockito.when(opfAcceleratorService.submitPayment(submitRequest, false)).thenReturn(null);

        OPFPaymentSubmitResponseDTO result = paymentFacade.submitPayment(requestDTO, "SESSION123", "192.168.1.1");

        Assertions.assertNotNull(result);

        Mockito.verify(opfAcceleratorService).submitPayment(any(OPFPaymentSubmitRequest.class), anyBoolean());
        Mockito.verifyNoInteractions(opfAcceleratorSubmitResponseConverter);
    }

    @Test
    void getApplePayWebSession_validRequest_returnsPopulatedResponse() {
        OPFApplePayRequestDTO requestDTO = new OPFApplePayRequestDTO();
        OPFApplePayRequest applePayRequest = new OPFApplePayRequest();
        OPFApplePayResponse applePayResponse = new OPFApplePayResponse();

        Mockito.when(opfApplePayRequestConverter.convert(requestDTO, applePayRequest)).thenReturn(applePayRequest);
        Mockito.when(opfAcceleratorService.getApplePayWebSession(applePayRequest)).thenReturn(applePayResponse);
        OPFApplePayResponse result = paymentFacade.getApplePayWebSession(requestDTO);
        Assertions.assertNotNull(result);
        Mockito.verify(opfAcceleratorService).getApplePayWebSession(any(OPFApplePayRequest.class));
    }

    @Test
    void getApplePayWebSession_nullRequest_returnsEmptyResponse() {
        OPFApplePayResponse result = paymentFacade.getApplePayWebSession(null);

        Assertions.assertNotNull(result);
        Mockito.verifyNoInteractions(opfApplePayRequestConverter, opfAcceleratorService);
    }

    @Test
    void getApplePayWebSession_serviceReturnsNull_returnsEmptyResponse() {
        OPFApplePayRequestDTO requestDTO = new OPFApplePayRequestDTO();
        OPFApplePayRequest applePayRequest = new OPFApplePayRequest();

        Mockito.when(opfApplePayRequestConverter.convert(requestDTO, applePayRequest)).thenReturn(applePayRequest);
        Mockito.when(opfAcceleratorService.getApplePayWebSession(applePayRequest)).thenReturn(null);

        OPFApplePayResponse result = paymentFacade.getApplePayWebSession(requestDTO);

        Assertions.assertNotNull(result);

        Mockito.verify(opfAcceleratorService).getApplePayWebSession(any(OPFApplePayRequest.class));
    }

    @Test
    void mapAddressWsDTOToAddressData_validSource_returnsMappedAddressData() {
        AddressWsDTO source = new AddressWsDTO();
        source.setFirstName("John");
        source.setLastName("Doe");
        CountryWsDTO country = new CountryWsDTO();
        country.setIsocode("US");
        source.setCountry(country);
        RegionWsDTO region = new RegionWsDTO();
        region.setIsocode("CA");
        source.setRegion(region);

        AddressData result = paymentFacade.mapAddressWsDTOToAddressData(source);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("John", result.getFirstName());
        Assertions.assertEquals("Doe", result.getLastName());
        Assertions.assertNotNull(result.getCountry());
        Assertions.assertEquals("US", result.getCountry().getIsocode());
        Assertions.assertNotNull(result.getRegion());
        Assertions.assertEquals("US-CA", result.getRegion().getIsocode());
    }

    @Test
    void mapAddressWsDTOToAddressData_sourceWithNullCountry_returnsAddressDataWithoutCountry() {
        AddressWsDTO source = new AddressWsDTO();
        source.setFirstName("John");
        source.setLastName("Doe");
        source.setCountry(null);

        AddressData result = paymentFacade.mapAddressWsDTOToAddressData(source);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("John", result.getFirstName());
        Assertions.assertEquals("Doe", result.getLastName());
        Assertions.assertNull(result.getCountry());
    }

    @Test
    void mapAddressWsDTOToAddressData_sourceWithNullRegion_returnsAddressDataWithoutRegion() {
        AddressWsDTO source = new AddressWsDTO();
        source.setFirstName("John");
        source.setLastName("Doe");
        CountryWsDTO country = new CountryWsDTO();
        country.setIsocode("US");
        source.setCountry(country);
        source.setRegion(null);

        AddressData result = paymentFacade.mapAddressWsDTOToAddressData(source);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("John", result.getFirstName());
        Assertions.assertEquals("Doe", result.getLastName());
        Assertions.assertNotNull(result.getCountry());
        Assertions.assertEquals("US", result.getCountry().getIsocode());
        Assertions.assertNull(result.getRegion());
    }

    @Test
    void mapAddressWsDTOToAddressData_sourceWithInvalidRegion_returnsAddressDataWithDefaultRegion() {
        AddressWsDTO source = new AddressWsDTO();
        source.setFirstName("John");
        source.setLastName("Doe");
        CountryWsDTO country = new CountryWsDTO();
        country.setIsocode("US");
        source.setCountry(country);
        RegionWsDTO region = new RegionWsDTO();
        region.setIsocode("INVALID");
        source.setRegion(region);

        AddressData result = paymentFacade.mapAddressWsDTOToAddressData(source);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getRegion());
        Assertions.assertEquals("US-INVALID", result.getRegion().getIsocode());
    }

    @Test
    void verifyPayment_validRequest_returnsPopulatedResponse() {
        OPFPaymentVerifyRequestDTO requestDTO = new OPFPaymentVerifyRequestDTO();
        OPFPaymentVerifyRequest verifyRequest = new OPFPaymentVerifyRequest();
        OPFPaymentVerifyResponse verifyResponse = new OPFPaymentVerifyResponse();

        Mockito.when(opfAcceleratorVerifyRequestConverter.convert(requestDTO, verifyRequest)).thenReturn(verifyRequest);
        Mockito.when(opfAcceleratorService.verifyPayment(verifyRequest)).thenReturn(verifyResponse);

        OPFPaymentVerifyResponse result = paymentFacade.verifyPayment(requestDTO);

        Assertions.assertNotNull(result);
        Mockito.verify(opfAcceleratorVerifyRequestConverter).convert(requestDTO, verifyRequest);
        Mockito.verify(opfAcceleratorService).verifyPayment(verifyRequest);
    }

    @Test
    void verifyPayment_nullRequest_returnsNullResponse() {
        OPFPaymentVerifyResponse result = paymentFacade.verifyPayment(null);

        Assertions.assertNull(result);
        Mockito.verifyNoInteractions(opfAcceleratorVerifyRequestConverter);
    }

    @Test
    void verifyPayment_serviceReturnsNull_returnsNullResponse() {
        OPFPaymentVerifyRequestDTO requestDTO = new OPFPaymentVerifyRequestDTO();
        OPFPaymentVerifyRequest verifyRequest = new OPFPaymentVerifyRequest();

        Mockito.when(opfAcceleratorVerifyRequestConverter.convert(requestDTO, verifyRequest)).thenReturn(verifyRequest);
        Mockito.when(opfAcceleratorService.verifyPayment(verifyRequest)).thenReturn(null);

        OPFPaymentVerifyResponse result = paymentFacade.verifyPayment(requestDTO);

        Assertions.assertNull(result);

        Mockito.verify(opfAcceleratorService).verifyPayment(any(OPFPaymentVerifyRequest.class));
    }
}


