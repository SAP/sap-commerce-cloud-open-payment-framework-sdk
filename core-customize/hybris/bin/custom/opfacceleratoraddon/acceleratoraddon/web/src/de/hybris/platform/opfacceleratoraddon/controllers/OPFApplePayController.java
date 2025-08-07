/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.controllers;

import com.opf.dto.cta.CTARequestDTO;
import com.opf.dto.cta.CTAResponseDTO;
import com.opf.dto.cta.OPFActiveConfigDTO;
import com.opf.dto.request.OPFApplePayRequestDTO;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController;
import de.hybris.platform.facade.OPFAcceleratorPaymentFacade;
import de.hybris.platform.opf.data.response.OPFApplePayResponse;
import de.hybris.platform.opfacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfacceleratoraddon.util.OPFAcceleratorUtil;
import de.hybris.platform.opfservices.client.CCAdapterClientException;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static de.hybris.platform.util.Sanitizer.sanitize;

@RestController
@RequestMapping(value = "/opf-payment")
public class OPFApplePayController extends AbstractController {
    private static final Logger LOG = Logger.getLogger(OPFApplePayController.class);

    @Resource(name = "opfAcceleratorPaymentFacade")
    private OPFAcceleratorPaymentFacade opfAcceleratorPaymentFacade;


    /**
     * create apple pay web session
     *
     * @param opfApplePayRequestDTO opfApplePayRequestDTO
     * @return {@link OPFApplePayResponse}
     * @see OPFApplePayResponse
     */
    @ResponseBody
    @RequestMapping(value = "/applepay-web-session", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OPFApplePayResponse createApplePayWebSession(@RequestBody final OPFApplePayRequestDTO opfApplePayRequestDTO) {
        try {
            return opfAcceleratorPaymentFacade.getApplePayWebSession(opfApplePayRequestDTO);
        } catch (CCAdapterClientException exception) {
            throw new OPFAcceleratorException("Client error occurred while getting Apple Pay web session", exception);
        } catch (Exception exception) {
            throw new OPFAcceleratorException("Error occurred while getting Apple Pay session", exception);
        }

    }


}