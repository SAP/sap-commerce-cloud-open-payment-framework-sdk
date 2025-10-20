/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfb2bacceleratoraddon.controllers.pages.checkout.steps;

import com.opf.dto.cta.OPFActiveConfigDTO;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController;
import de.hybris.platform.facade.OPFAcceleratorFacade;
import de.hybris.platform.opfb2bacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfservices.client.CCAdapterClientException;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/opf-payment")
public class OPFB2BPaymentController extends AbstractController {

    @Resource(name = "opfAcceleratorFacade")
    private OPFAcceleratorFacade opfAcceleratorFacade;

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