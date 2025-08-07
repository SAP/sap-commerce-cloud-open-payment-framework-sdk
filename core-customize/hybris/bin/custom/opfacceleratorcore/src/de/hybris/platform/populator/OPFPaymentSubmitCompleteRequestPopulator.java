/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.opf.data.OPFOrderData;
import de.hybris.platform.opf.data.OPFPaymentAttributeData;
import de.hybris.platform.opf.data.OPFPaymentSubmitCompleteRequestData;
import de.hybris.platform.opf.dto.OPFPaymentSubmitCompleteRequest;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.stream.Collectors;

public class OPFPaymentSubmitCompleteRequestPopulator
        implements Populator<OPFPaymentSubmitCompleteRequest, OPFPaymentSubmitCompleteRequestData> {
    private Converter<AbstractOrderData, OPFOrderData> opfPaymentOrderConverter;
    private CartFacade cartFacade;

    /**
     * @param source
     *         OPFPaymentSubmitCompleteRequest the source object
     * @param target
     *         OPFPaymentSubmitCompleteRequestData the target to fill
     * @throws ConversionException
     */
    @Override
    public void populate(OPFPaymentSubmitCompleteRequest source, OPFPaymentSubmitCompleteRequestData target) throws ConversionException {
        target.setPaymentSessionId(source.getPaymentSessionId());
        target.setAdditionalData(source.getAdditionalData().stream().map(data -> {
            OPFPaymentAttributeData ctaAttributes = new OPFPaymentAttributeData();
            ctaAttributes.setKey(data.getKey());
            ctaAttributes.setValue(data.getValue());
            return ctaAttributes;
        }).collect(Collectors.toList()));
        target.setOrder(getOpfPaymentOrderConverter().convert(getCartFacade().getSessionCart()));
    }

    public Converter<AbstractOrderData, OPFOrderData> getOpfPaymentOrderConverter() {
        return opfPaymentOrderConverter;
    }

    public void setOpfPaymentOrderConverter(Converter<AbstractOrderData, OPFOrderData> opfPaymentOrderConverter) {
        this.opfPaymentOrderConverter = opfPaymentOrderConverter;
    }

    public CartFacade getCartFacade() {
        return cartFacade;
    }

    public void setCartFacade(CartFacade cartFacade) {
        this.cartFacade = cartFacade;
    }
}
