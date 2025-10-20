/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import com.opf.order.data.OPFB2BPaymentTypeData;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.data.response.OPFActiveConfigValue;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

/**
 * Populator implementation for converting OPFActiveConfigValue to B2BPaymentTypeData. This class handles the mapping of data between the
 * source and target objects.
 */
public class OPFB2BAcceleratorPaymentActiveConfigResponsePopulator implements Populator<OPFActiveConfigValue, OPFB2BPaymentTypeData> {

    @Override
    public void populate(OPFActiveConfigValue source, OPFB2BPaymentTypeData target) throws ConversionException {
        if (source != null && target != null) {
            if (StringUtils.equals(source.getProvider(), "ACCOUNT_PAYMENT")) {
                target.setCode(CheckoutPaymentType.ACCOUNT.getCode());
            }else {
                target.setCode(CheckoutPaymentType.CARD.getCode());
            }
            target.setId(source.getId());
            target.setDisplayName(source.getDisplayName());
            target.setLogoUrl(source.getLogoUrl());
            target.setMerchantId(source.getMerchantId());
        }
    }
}
