/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.controllers;

import com.opf.dto.request.OPFApplePayRequestDTO;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController;
import de.hybris.platform.facade.OPFAcceleratorFacade;
import de.hybris.platform.opf.data.response.OPFApplePayResponse;
import de.hybris.platform.opfacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfacceleratoraddon.exception.OPFRequestValidationException;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/opf-payment")
public class OPFApplePayController extends AbstractController {
    private static final Logger LOG = Logger.getLogger(OPFApplePayController.class);

    @Resource(name = "opfAcceleratorFacade")
    private OPFAcceleratorFacade opfAcceleratorFacade;

    /**
     * create apple pay web session
     *
     * @param opfApplePayRequestDTO
     *         opfApplePayRequestDTO
     * @return {@link OPFApplePayResponse}
     * @see OPFApplePayResponse
     */
    @ResponseBody
    @RequestMapping(value = "/applepay-web-session", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OPFApplePayResponse createApplePayWebSession(@RequestBody final OPFApplePayRequestDTO opfApplePayRequestDTO) {
        try {
            if (Stream.of(opfApplePayRequestDTO.getValidationUrl(),
                            opfApplePayRequestDTO.getInitiative(),
                            opfApplePayRequestDTO.getInitiativeContext())
                    .anyMatch(s -> s == null || s.isEmpty())) {
                throw new OPFRequestValidationException("Some required fields are missing or contain errors");
            }
            return opfAcceleratorFacade.getApplePayWebSession(opfApplePayRequestDTO);
        } catch (Exception exception) {
            throw new OPFAcceleratorException("Error occurred while getting Apple Pay session", exception);
        }
    }
}