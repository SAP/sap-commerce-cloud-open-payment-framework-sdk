/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.opf.data.OPFInitiatePaymentSessionRequestData;
import de.hybris.platform.opf.data.OPFOrderData;
import de.hybris.platform.opf.data.OPFPaymentBrowserInfoData;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionRequest;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang3.StringUtils;

/**
 * Populates OPFInitiatePaymentSessionRequestData. This populator is used to map internal payment data into a request during the payment
 * session flow.
 */
public class OPFAcceleratorPaymentSessionRequestPopulator
        implements Populator<OPFInitiatePaymentSessionRequest, OPFInitiatePaymentSessionRequestData> {

    private Converter<AbstractOrderData, OPFOrderData> opfPaymentOrderConverter;
    private CartFacade cartFacade;

    @Override
    public void populate(OPFInitiatePaymentSessionRequest source, OPFInitiatePaymentSessionRequestData target) throws ConversionException {
        validateSourceAndTarget(source, target);

        populateBasicFields(source, target);
        populateBrowserInfo(source, target);
        // Fetch the session cart and set the order and payment ID
        AbstractOrderData cartData = getCartFacade().getSessionCart();
        target.setOrder(getOpfPaymentOrderConverter().convert(cartData));
        target.setOrderPaymentId(cartData.getCode());
    }

    /**
     * validate source and target
     *
     * @param source source
     * @param target target
     */
    private void validateSourceAndTarget(OPFInitiatePaymentSessionRequest source, OPFInitiatePaymentSessionRequestData target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source or target cannot be null.");
        }
    }

    /**
     * populate basic fields
     *
     * @param source source
     * @param target target
     */
    private void populateBasicFields(OPFInitiatePaymentSessionRequest source, OPFInitiatePaymentSessionRequestData target) {
        if (source != null) {
            if (StringUtils.isNotBlank(source.getAccountId())) {
                target.setAccountId(source.getAccountId());
            }
            if (StringUtils.isNotBlank(source.getResultURL())) {
                target.setResultURL(source.getResultURL());
            }
            if (StringUtils.isNotBlank(source.getCancelURL())) {
                target.setCancelURL(source.getCancelURL());
            }
        }
    }

    /**
     * populate browser info
     *
     * @param source source
     * @param target target
     */
    private void populateBrowserInfo(OPFInitiatePaymentSessionRequest source, OPFInitiatePaymentSessionRequestData target) {
        if (source.getBrowserInfo() != null) {
            OPFPaymentBrowserInfoData browserInfo = new OPFPaymentBrowserInfoData();
            browserInfo.setAcceptHeader(source.getBrowserInfo().getAcceptHeader());
            browserInfo.setColorDepth(source.getBrowserInfo().getColorDepth());
            browserInfo.setJavaEnabled(source.getBrowserInfo().isJavaEnabled());
            browserInfo.setJavaScriptEnabled(source.getBrowserInfo().isJavaScriptEnabled());
            browserInfo.setLanguage(source.getBrowserInfo().getLanguage());
            browserInfo.setScreenHeight(source.getBrowserInfo().getScreenHeight());
            browserInfo.setScreenWidth(source.getBrowserInfo().getScreenWidth());
            browserInfo.setUserAgent(source.getBrowserInfo().getUserAgent());
            browserInfo.setTimeZoneOffset(source.getBrowserInfo().getTimeZoneOffset());
            browserInfo.setIpAddress(source.getBrowserInfo().getIpAddress());
            browserInfo.setOriginUrl(source.getBrowserInfo().getOriginUrl());
            target.setBrowserInfo(browserInfo);
        }
    }

    public CartFacade getCartFacade() {
        return cartFacade;
    }

    public void setCartFacade(CartFacade cartFacade) {
        this.cartFacade = cartFacade;
    }

    public Converter<AbstractOrderData, OPFOrderData> getOpfPaymentOrderConverter() {
        return opfPaymentOrderConverter;
    }

    public void setOpfPaymentOrderConverter(Converter<AbstractOrderData, OPFOrderData> opfPaymentOrderConverter) {
        this.opfPaymentOrderConverter = opfPaymentOrderConverter;
    }
}
