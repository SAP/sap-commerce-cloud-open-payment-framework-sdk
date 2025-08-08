/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.controllers;

import com.opf.dto.cta.CTARequestDTO;
import com.opf.dto.cta.CTAResponseDTO;
import com.opf.dto.cta.OPFActiveConfigDTO;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController;

import de.hybris.platform.facade.OPFAcceleratorFacade;
import de.hybris.platform.opfacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfservices.client.CCAdapterClientException;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/opf-payment")
public class OPFPaymentController extends AbstractController {
    private static final Logger LOG = Logger.getLogger(OPFPaymentController.class);

    @Resource(name = "opfAcceleratorFacade")
    private OPFAcceleratorFacade opfAcceleratorFacade;

    /**
     * Handles CTA script rendering for OPF payments.
     *
     * @param ctaRequestWsDTO
     *         the request data transfer object
     * @return a CTAResponseWsDTO containing response data
     */
    @ResponseBody
    @RequestMapping(value = "/cta-scripts-rendering", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public CTAResponseDTO ctaScriptRendering(@RequestBody final CTARequestDTO ctaRequestWsDTO) {
        try {
            return opfAcceleratorFacade.getCTAResponse(ctaRequestWsDTO);
        }  catch (Exception exception) {
            throw new OPFAcceleratorException("Error occurred while processing CTA script rendering for OPF payment", exception);
        }

    }

    /**
     * get active configurations
     *
     * @return {@link OPFActiveConfigDTO}
     * @see OPFActiveConfigDTO
     */
    @GetMapping(value = "/active-configurations")
    @ResponseBody
    public OPFActiveConfigDTO getActiveConfigurations() {
        try {
            return opfAcceleratorFacade.getActiveConfigurations();
        } catch (CCAdapterClientException exception) {
            throw new OPFAcceleratorException("Client error occurred while getting active configurations from OPF payments", exception);
        } catch (Exception exception) {
            throw new OPFAcceleratorException("Error occurred while getting active configurations from OPF payments", exception);
        }
    }


}