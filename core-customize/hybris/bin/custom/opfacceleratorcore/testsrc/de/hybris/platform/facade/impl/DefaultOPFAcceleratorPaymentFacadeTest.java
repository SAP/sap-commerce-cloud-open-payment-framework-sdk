/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.facade.impl;

import com.opf.dto.cta.CTARequestDTO;
import com.opf.dto.cta.CTAResponseDTO;
import de.hybris.platform.cta.request.OPFPaymentCTARequest;
import de.hybris.platform.cta.response.OPFPaymentCTAResponse;
import de.hybris.platform.opf.data.OPFInitiatePaymentData;
import de.hybris.platform.opf.data.OPFInitiatePaymentSessionRequestData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteRequestData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteResponseData;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionRequest;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionResponse;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteRequest;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteResponse;
import de.hybris.platform.service.OPFAcceleratorPaymentService;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DefaultOPFAcceleratorPaymentFacadeTest {

    @InjectMocks
    private DefaultOPFAcceleratorPaymentFacade paymentFacade;

    @Mock
    private OPFAcceleratorPaymentService opfAcceleratorPaymentService;

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
    private Converter<OPFInitiatePaymentSessionRequest, OPFInitiatePaymentSessionRequestData> requestConverter;
    @Mock
    private Converter<OPFPaymentSubmitCompleteRequest, OPFPaymentSubmitCompleteRequestData> opfPaymentSubmitCompleteRequestConverter;
    @Mock
    private Converter<OPFPaymentSubmitCompleteResponse, OPFPaymentSubmitCompleteResponseData> opfPaymentSubmitCompleteResponseConverter;

    @Mock
    private Converter<OPFInitiatePaymentSessionResponse, OPFInitiatePaymentData> responseConverter;
    private OPFInitiatePaymentSessionRequest paymentRequest;
    private OPFInitiatePaymentSessionResponse response;

    @BeforeEach
    void setUp() {
        paymentRequest = new OPFInitiatePaymentSessionRequest();
        response = new OPFInitiatePaymentSessionResponse();
    }

    @Test
    public void getCTAResponseReturnsPopulatedResponseWhenRequestAndServiceResponseAreValid() {
        Mockito.when(opfAcceleratorCTARequestConverter.convert(ctaRequestWsDTO, opfPaymentCTARequest)).thenReturn(opfPaymentCTARequest);
        Mockito.when(opfAcceleratorPaymentService.getCTAResponse(opfPaymentCTARequest)).thenReturn(opfPaymentCTAResponse);
        Mockito.when(opfAcceleratorCTAResponseConverter.convert(opfPaymentCTAResponse, ctaResponseWsDTO)).thenReturn(ctaResponseWsDTO);

        CTAResponseDTO result = paymentFacade.getCTAResponse(ctaRequestWsDTO);

        Assertions.assertNotNull(result);
        Mockito.verify(opfAcceleratorCTARequestConverter).convert(ctaRequestWsDTO, opfPaymentCTARequest);
        Mockito.verify(opfAcceleratorPaymentService).getCTAResponse(opfPaymentCTARequest);
        Mockito.verify(opfAcceleratorCTAResponseConverter).convert(opfPaymentCTAResponse, ctaResponseWsDTO);
    }

    @Test
    public void getCTAResponseReturnsEmptyResponseWhenRequestIsNull() {
        CTAResponseDTO result = paymentFacade.getCTAResponse(null);

        Assertions.assertNotNull(result);
        Mockito.verifyNoInteractions(opfAcceleratorCTARequestConverter, opfAcceleratorPaymentService, opfAcceleratorCTAResponseConverter);
    }

    @Test
    public void getCTAResponseReturnsEmptyResponseWhenServiceResponseIsNull() {
        Mockito.when(opfAcceleratorCTARequestConverter.convert(ctaRequestWsDTO, opfPaymentCTARequest)).thenReturn(opfPaymentCTARequest);
        Mockito.when(opfAcceleratorPaymentService.getCTAResponse(opfPaymentCTARequest)).thenReturn(null);

        CTAResponseDTO result = paymentFacade.getCTAResponse(ctaRequestWsDTO);

        Assertions.assertNotNull(result);
        Mockito.verify(opfAcceleratorCTARequestConverter).convert(ctaRequestWsDTO, opfPaymentCTARequest);
        Mockito.verify(opfAcceleratorPaymentService).getCTAResponse(opfPaymentCTARequest);
        Mockito.verifyNoInteractions(opfAcceleratorCTAResponseConverter);
    }

    @Test
    public void getCTAResponseThrowsExceptionWhenRequestConverterFails() {
        Mockito.doThrow(new RuntimeException("Converter error")).when(opfAcceleratorCTARequestConverter)
                .convert(ctaRequestWsDTO, opfPaymentCTARequest);

        Assertions.assertThrows(RuntimeException.class, () -> paymentFacade.getCTAResponse(ctaRequestWsDTO));
        Mockito.verify(opfAcceleratorCTARequestConverter).convert(ctaRequestWsDTO, opfPaymentCTARequest);
        Mockito.verifyNoInteractions(opfAcceleratorPaymentService, opfAcceleratorCTAResponseConverter);
    }

    @Test
    void testGetInitiatePaymentResponse_withValidRequest_shouldReturnConvertedData() {
        Mockito.doAnswer(invocation -> {
                    OPFInitiatePaymentSessionRequest src = invocation.getArgument(0);
                    OPFInitiatePaymentSessionRequestData dest = invocation.getArgument(1);
                    return null;
                }).when(requestConverter)
                .convert(Mockito.any(OPFInitiatePaymentSessionRequest.class), Mockito.any(OPFInitiatePaymentSessionRequestData.class));
        Mockito.when(opfAcceleratorPaymentService.getInitiatePaymentResponse(Mockito.any(OPFInitiatePaymentSessionRequestData.class)))
                .thenReturn(response);
        Mockito.doAnswer(invocation -> {
            OPFInitiatePaymentSessionResponse src = invocation.getArgument(0);
            OPFInitiatePaymentData dest = invocation.getArgument(1);
            return null;
        }).when(responseConverter).convert(Mockito.any(OPFInitiatePaymentSessionResponse.class), Mockito.any(OPFInitiatePaymentData.class));
        OPFInitiatePaymentData actual = paymentFacade.getInitiatePaymentResponse(paymentRequest);
        Assertions.assertNotNull(actual);
        Mockito.verify(opfAcceleratorPaymentService).getInitiatePaymentResponse(Mockito.any(OPFInitiatePaymentSessionRequestData.class));
    }

    @Test
    void testGetInitiatePaymentResponse_withNullRequest_shouldReturnEmptyData() {
        OPFInitiatePaymentData result = paymentFacade.getInitiatePaymentResponse(null);
        Assertions.assertNotNull(result);
        Mockito.verifyNoInteractions(requestConverter, opfAcceleratorPaymentService, responseConverter);
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
        Mockito.when(opfAcceleratorPaymentService.getCompletedPaymentResponse(Mockito.any())).thenReturn(paymentSubmitCompleteResponse);

        // Act
        OPFPaymentSubmitCompleteResponseData actualResponse = paymentFacade.getCompletedPaymentResponse(inputRequest);

        // Assert
        Assertions.assertNotNull(actualResponse);
        Mockito.verify(opfAcceleratorPaymentService).getCompletedPaymentResponse(Mockito.any());
    }

    @Test
    void getCompletedPaymentResponse_responseWithNullFields_doesNotConvert() {
        // Arrange
        OPFPaymentSubmitCompleteRequest inputRequest = new OPFPaymentSubmitCompleteRequest();
        OPFPaymentSubmitCompleteResponse opfPaymentSubmitCompleteResponse = new OPFPaymentSubmitCompleteResponse();
        opfPaymentSubmitCompleteResponse.setOrderPaymentId(null);  // Invalid case
        opfPaymentSubmitCompleteResponse.setStatus("");            // Invalid case
        // Mock service call
        Mockito.when(opfAcceleratorPaymentService.getCompletedPaymentResponse(Mockito.any())).thenReturn(opfPaymentSubmitCompleteResponse);

        // Act
        OPFPaymentSubmitCompleteResponseData actualResponse = paymentFacade.getCompletedPaymentResponse(inputRequest);

        // Assert
        Assertions.assertNotNull(actualResponse);
    }
}


