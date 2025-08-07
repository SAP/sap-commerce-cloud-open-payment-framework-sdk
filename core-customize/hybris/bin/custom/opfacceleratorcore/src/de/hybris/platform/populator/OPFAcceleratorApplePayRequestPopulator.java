/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import com.opf.dto.request.OPFApplePayRequestDTO;
import com.opf.dto.submit.OPFPaymentSubmitRequestDTO;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.opf.data.OPFOrderData;
import de.hybris.platform.opf.data.OPFPaymentAttributeData;
import de.hybris.platform.opf.data.OPFPaymentBrowserInfoData;
import de.hybris.platform.opf.data.request.OPFApplePayRequest;
import de.hybris.platform.opf.data.request.OPFPaymentSubmitRequest;
import de.hybris.platform.opf.dto.OPFPaymentBrowserInfo;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.util.OPFAcceleratorCoreUtil;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 *  OPF apple pay request populator
 *
 */
public class OPFAcceleratorApplePayRequestPopulator implements Populator<OPFApplePayRequestDTO, OPFApplePayRequest> {

    private CartFacade cartFacade;

    /**
     * Constructor for the OPFAcceleratorApplePayRequestPopulator class.
     *
     * @param cartFacade The facade used to interact with the cart data.
     */
    public OPFAcceleratorApplePayRequestPopulator(CartFacade cartFacade) {
        this.cartFacade = cartFacade;
    }
    @Override
    public void populate(OPFApplePayRequestDTO source, OPFApplePayRequest target) throws ConversionException {
        if (source != null && target != null) {
        target.setInitiative(source.getInitiative());
        target.setValidationUrl(source.getValidationUrl());
        target.setInitiativeContext(source.getInitiativeContext());
            AbstractOrderData cartData = cartFacade.getSessionCart();
            if (cartData == null) {
                throw new ConversionException("Session cart is not available");
            }
                target.setDivisionId(cartData.getStore());
            }
        }




}
