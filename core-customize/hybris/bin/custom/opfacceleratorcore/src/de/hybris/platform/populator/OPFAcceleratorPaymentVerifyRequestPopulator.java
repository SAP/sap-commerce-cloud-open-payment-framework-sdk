/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import com.opf.dto.verify.OPFPaymentVerifyRequestDTO;
import de.hybris.platform.constants.OpfacceleratorcoreConstants;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.opf.data.request.OPFAdditionalData;
import de.hybris.platform.opf.dto.OPFPaymentAttribute;
import de.hybris.platform.opf.dto.OPFPaymentVerifyRequest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * Populator implementation for converting an OPFPaymentVerifyRequestDTO to an OPFPaymentVerifyRequest.
 * This class handles the mapping of data between the source and target objects.
 */
public class OPFAcceleratorPaymentVerifyRequestPopulator implements Populator<OPFPaymentVerifyRequestDTO, OPFPaymentVerifyRequest> {
    @Resource(name = "configurationService")
    private ConfigurationService configurationService;
    @Override
    public void populate(OPFPaymentVerifyRequestDTO source, OPFPaymentVerifyRequest target) throws ConversionException {
        if (source != null && target != null) {
            target.setResponseMap(source.getResponseMap().stream().map(data -> {
                OPFAdditionalData opfAdditionalData = new OPFAdditionalData();
                opfAdditionalData.setKey(data.getKey());
                opfAdditionalData.setValue(data.getValue());
                return opfAdditionalData;
            }).collect(Collectors.toList()));
        }
        target.setPaymentSessionId(String.valueOf(source.getResponseMap().stream()
                .filter(data -> StringUtils.equals(data.getKey(), OpfacceleratorcoreConstants.PAYMENT_SESSION_ID_KEY))
                .findFirst()
                .map(OPFPaymentAttribute::getValue)
                .orElse(StringUtils.EMPTY)));
        target.setDivisionId(configurationService.getConfiguration().getString(OpfacceleratorcoreConstants.OPF_ACTIVE_CONFIG_DIVISION_ID, StringUtils.EMPTY));

    }

}
