/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.opf.data.OPFPaymentAttributeData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteResponseData;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteResponse;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.stream.Collectors;

public class OPFPaymentSubmitCompleteResponsePopulator
        implements Populator<OPFPaymentSubmitCompleteResponse, OPFPaymentSubmitCompleteResponseData> {
    /**
     * @param source
     *         opfPaymentSubmitCompleteResponse the source object
     * @param target
     *         opfPaymentSubmitCompleteResponseData the target to fill
     * @throws ConversionException
     */
    @Override
    public void populate(OPFPaymentSubmitCompleteResponse source, OPFPaymentSubmitCompleteResponseData target) throws ConversionException {
        if (source != null && target != null) {
            target.setOrderPaymentId(source.getOrderPaymentId());
            target.setStatus(source.getStatus());
            target.setReasonCode(source.getReasonCode());
            target.setCustomFields(source.getCustomFields().stream().map(data -> {
                OPFPaymentAttributeData ctaAttributes = new OPFPaymentAttributeData();
                ctaAttributes.setKey(data.getKey());
                ctaAttributes.setValue(data.getValue());
                return ctaAttributes;
            }).collect(Collectors.toList()));
        }
    }
}
