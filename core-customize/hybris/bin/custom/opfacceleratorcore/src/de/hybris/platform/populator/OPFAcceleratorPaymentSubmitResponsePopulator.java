/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import com.opf.dto.submit.OPFPaymentSubmitResponseDTO;

import de.hybris.platform.converters.Populator;

import de.hybris.platform.opf.data.request.OPFAdditionalData;
import de.hybris.platform.opf.data.response.OPFPaymentSubmitResponse;

import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.stream.Collectors;

public class OPFAcceleratorPaymentSubmitResponsePopulator implements Populator<OPFPaymentSubmitResponse, OPFPaymentSubmitResponseDTO> {

    @Override
    public void populate(OPFPaymentSubmitResponse source, OPFPaymentSubmitResponseDTO target) throws ConversionException {
        if (source != null && target != null) {
            target.setCartId(source.getOrderPaymentId());
            if(CollectionUtils.isEmpty(source.getCustomFields())){
                target.setCustomFields(Collections.emptyList());
            }else{
                target.setCustomFields(source.getCustomFields().stream().map(data -> {
                    OPFAdditionalData opfAdditionalDataDTO = new OPFAdditionalData();
                    opfAdditionalDataDTO.setKey(data.getKey());
                    opfAdditionalDataDTO.setValue(data.getValue());
                    return opfAdditionalDataDTO;
                }).collect(Collectors.toList()));
            }
            target.setPaymentSessionId(source.getPaymentSessionId());
            target.setStatus(source.getStatus());
            target.setAuthorizedAmount(source.getAuthorizedAmount());
        }
    }

}
