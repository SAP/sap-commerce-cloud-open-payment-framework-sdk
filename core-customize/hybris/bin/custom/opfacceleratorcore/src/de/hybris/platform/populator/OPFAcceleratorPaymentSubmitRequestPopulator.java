/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import com.opf.dto.submit.OPFPaymentSubmitRequestDTO;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.opf.data.OPFOrderData;
import de.hybris.platform.opf.data.OPFPaymentAttributeData;
import de.hybris.platform.opf.data.OPFPaymentBrowserInfoData;
import de.hybris.platform.opf.data.request.OPFPaymentSubmitRequest;
import de.hybris.platform.opf.dto.OPFPaymentBrowserInfo;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.util.OPFAcceleratorCoreUtil;

import java.util.Collections;
import java.util.stream.Collectors;

public class OPFAcceleratorPaymentSubmitRequestPopulator implements Populator<OPFPaymentSubmitRequestDTO, OPFPaymentSubmitRequest> {

    private Converter<AbstractOrderData, OPFOrderData> opfPaymentOrderConverter;
    private CartFacade cartFacade;

    public OPFAcceleratorPaymentSubmitRequestPopulator(
            Converter<AbstractOrderData, OPFOrderData> opfPaymentOrderConverter,
            CartFacade cartFacade) {
        this.opfPaymentOrderConverter = opfPaymentOrderConverter;
        this.cartFacade = cartFacade;
    }
    @Override
    public void populate(OPFPaymentSubmitRequestDTO source, OPFPaymentSubmitRequest target) throws ConversionException {
        if (source != null && target != null) {

            target.setChannel(source.getChannel());
            target.setAdditionalData(source.getAdditionalData().stream().map(data -> {
                OPFPaymentAttributeData opfAdditionalData = new OPFPaymentAttributeData();
                opfAdditionalData.setKey(data.getKey());
                opfAdditionalData.setValue(data.getValue());
                return opfAdditionalData;
            }).collect(Collectors.toList()));
            // Fetch the session cart and set the order and payment ID
            AbstractOrderData cartData = cartFacade.getSessionCart();
            if (cartData == null) {
                throw new ConversionException("Session cart is not available");
            }
            target.setOrder(opfPaymentOrderConverter.convert(cartData));
            populateBrowserInfoData(source, target);

            //Quick Buy
            if(OPFAcceleratorCoreUtil.isQuickBuy(source.getPaymentMethod())){
                target.setPaymentMethod(source.getPaymentMethod().toUpperCase());
                target.setEncryptedToken(source.getEncryptedToken());
                target.setOrderPaymentId(cartData.getCode());
                target.setAdditionalData(Collections.emptyList());
                target.getOrder().setDivisionId(cartData.getStore());
            }
        }
    }


    /**
     * populate browser info data
     *
     * @param requestDTO
     *         requestDTO
     * @param target
     *         target
     */
    private void populateBrowserInfoData(OPFPaymentSubmitRequestDTO requestDTO, OPFPaymentSubmitRequest target) {
        OPFPaymentBrowserInfoData browserInfoData = new OPFPaymentBrowserInfoData();
        if (requestDTO.getBrowserInfo() != null) {
            OPFPaymentBrowserInfo browserInfo = requestDTO.getBrowserInfo();
            browserInfoData.setUserAgent(browserInfo.getUserAgent());
            browserInfoData.setScreenWidth(browserInfo.getScreenWidth());
            browserInfoData.setScreenHeight(browserInfo.getScreenHeight());
            browserInfoData.setColorDepth(browserInfo.getColorDepth());
            browserInfoData.setTimeZoneOffset(browserInfo.getTimeZoneOffset());
            browserInfoData.setLanguage(browserInfo.getLanguage());
            browserInfoData.setJavaEnabled(browserInfo.isJavaEnabled());
            browserInfoData.setJavaScriptEnabled(browserInfo.isJavaScriptEnabled());
            browserInfoData.setOriginUrl(browserInfo.getOriginUrl());
            browserInfoData.setAcceptHeader(browserInfo.getAcceptHeader());
            target.setBrowserInfo(browserInfoData);
        }
    }

}
