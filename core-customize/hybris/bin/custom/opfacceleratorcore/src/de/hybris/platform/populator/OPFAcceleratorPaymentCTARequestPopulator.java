/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;


import com.opf.dto.cta.CTARequestDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.cta.request.OPFPaymentCTAProductItems;
import de.hybris.platform.cta.request.OPFPaymentCTARequest;
import de.hybris.platform.cta.response.OPFPaymentCTAAttributes;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.stream.Collectors;

public class OPFAcceleratorPaymentCTARequestPopulator implements Populator<CTARequestDTO, OPFPaymentCTARequest> {
    @Override
    public void populate(CTARequestDTO source, OPFPaymentCTARequest target) throws ConversionException {
        if (source != null && target != null) {

            target.setAccountIds(source.getPaymentAccountIds());
            target.setScriptLocations(source.getScriptLocations());

            target.setCtaProductItems(source.getCtaProductItems().stream().map(item -> {
                OPFPaymentCTAProductItems ctaProductItems = new OPFPaymentCTAProductItems();
                ctaProductItems.setProductId(item.getProductId());
                ctaProductItems.setQuantity(item.getQuantity());
                return ctaProductItems;
            }).collect(Collectors.toList()));

            target.setAdditionalData(source.getAdditionalData().stream().map(data -> {
                OPFPaymentCTAAttributes ctaAttributes = new OPFPaymentCTAAttributes();
                ctaAttributes.setKey(data.getKey());
                ctaAttributes.setValue(data.getValue());
                return ctaAttributes;
            }).collect(Collectors.toList()));
        }
    }
}
