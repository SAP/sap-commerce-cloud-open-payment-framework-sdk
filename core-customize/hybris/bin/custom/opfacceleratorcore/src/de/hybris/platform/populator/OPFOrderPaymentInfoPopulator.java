/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

/**
 * Populates order payment info for guest users during quick buy.
 */
public class OPFOrderPaymentInfoPopulator implements Populator<AbstractOrderModel, AbstractOrderData> {

    /**
     * Populates the target order data with payment information for guest users login.
     * If a billing address is available, it sets the guest user's email (if present) on the billing address
     * and assigns it to a new CCPaymentInfoData object, which is then set as the payment info on the target
     * @param source the source order model containing the user and other order-related data
     * @param target the target order data object to be populated
     * @throws ConversionException if any issue occurs during the population process
     */
    public void populate(AbstractOrderModel source, AbstractOrderData target) throws ConversionException {
        validateParameterNotNullStandardMessage("source", source);
        validateParameterNotNullStandardMessage("target", target);
        if (source.getUser() instanceof CustomerModel customer) {

            // This implementation is specifically intended for the quick buy scenario for guest users.
            // To minimize code changes, we use CCPaymentInfo as the OOTB payment mechanism currently only supports CCPaymentInfo.
            // It does not support generic SAP payment types like Google Pay or Apple Pay.
            CCPaymentInfoData ccPaymentInfoData = new CCPaymentInfoData();
            AddressData address = target.getSapBillingAddress();
            if (address != null && CustomerType.GUEST.equals(customer.getType())) {

                //quick buy
                if (StringUtils.isNotEmpty(customer.getSapGuestUserEmail())) {
                    address.setEmail(customer.getSapGuestUserEmail());
                }
            }
                ccPaymentInfoData.setBillingAddress(address);
                target.setPaymentInfo(ccPaymentInfoData);
        }
    }
}