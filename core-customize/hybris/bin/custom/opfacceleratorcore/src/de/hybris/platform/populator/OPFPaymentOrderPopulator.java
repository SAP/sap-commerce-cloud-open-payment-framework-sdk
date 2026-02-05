/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.data.request.OPFPaymentAddressData;
import de.hybris.platform.opf.data.OPFOrderData;
import de.hybris.platform.opf.data.OPFOrderLineData;
import de.hybris.platform.opf.data.OPFShippingMethodData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static de.hybris.platform.constants.OpfacceleratorcoreConstants.OPF_ACTIVE_CONFIG_DIVISION_ID;

/**
 * Populator implementation for converting an AbstractOrderData object to an OPFOrderData object.
 * This class handles the mapping of data between the source and target objects.
 */
public class OPFPaymentOrderPopulator implements Populator<AbstractOrderData, OPFOrderData> {

    private ConfigurationService configurationService;
    @Resource(name = "commerceCommonI18NService")
    private CommerceCommonI18NService commerceCommonI18NService;
    /**
     * @param cart
     *         the source object
     * @param order
     *         the target to fill
     * @throws ConversionException
     */
    @Override
    public void populate(AbstractOrderData cart, OPFOrderData order) throws ConversionException {
        if (cart == null) {
            return;
        }

        populateOrderDetails(cart, order);
        populateAddresses(cart, order);
        populateOrderLines(cart, order);
    }

    /**
     * Populates the order details from the given cart data into the target order object.
     *
     * @param cart  the source {@link AbstractOrderData} containing the cart details
     * @param order the target {@link OPFOrderData} to be populated with order details
     */
    private void populateOrderDetails(AbstractOrderData cart, OPFOrderData order) {
        order.setCode(cart.getCode());
        if (cart.getTotalPrice() != null && cart.getTotalPrice().getValue() != null) {
            order.setTotal(cart.getTotalPrice().getValue().doubleValue());
            order.setCurrency(cart.getTotalPrice().getCurrencyIso());
        }

        if (cart.getSubTotal() != null && cart.getSubTotal().getValue() != null) {
            order.setSubTotalWithTax(cart.getSubTotal().getValue().doubleValue());
        }

        if (cart.getTotalDiscounts() != null && cart.getTotalDiscounts().getValue() != null) {
            order.setTotalDiscount(cart.getTotalDiscounts().getValue().doubleValue());
        }

        if (cart.getUser() != null) {
            order.setCustomerId(cart.getUser().getUid());
        }

        if (cart.getSapCustomerEmail() != null) {
            order.setCustomerEmail(cart.getSapCustomerEmail());
        }

        if (cart.getSite() != null) {
            order.setLanguageAtCreation(commerceCommonI18NService.getCurrentLanguage().getIsocode());
        }

        order.setDivisionId(getConfigurationValueForKey(OPF_ACTIVE_CONFIG_DIVISION_ID));
        order.setShFeeWithTax(0.0);
        order.setShFeeTax(0.0);
    }

    /**
     * Populates the address details from the given cart data into the target order object.
     * If a delivery address is present in the cart, it maps it to the shipping address
     * and sets the billing address to the same value. Additionally, it populates the shipping method.
     *
     * @param cart  the source {@link AbstractOrderData} containing the cart details
     * @param order the target {@link OPFOrderData} to be populated with address details
     */
    private void populateAddresses(AbstractOrderData cart, OPFOrderData order) {
     if (cart.getDeliveryAddress() != null) {
            order.setShippingAddress(mapToOpfAddress(cart.getDeliveryAddress()));
            order.setBillingAddress(order.getShippingAddress());
          }
     order.setShippingMethod(populateShippingMethod(cart));

    }

    /**
     * Populates the order lines from the given cart data into the target order object.
     * Iterates through the entries in the cart, mapping each entry to an order line
     * and setting various details such as quantity, delivery method, pricing, and product information.
     * Default values are assigned for discounts, shipping fees, and taxes.
     *
     * @param cart  the source {@link AbstractOrderData} containing the cart details
     * @param order the target {@link OPFOrderData} to be populated with order line details
     */
    private void populateOrderLines(AbstractOrderData cart, OPFOrderData order) {
        if (CollectionUtils.isEmpty(cart.getEntries())) {
            return;
        }

        List<OPFOrderLineData> orderLines = new ArrayList<>();

        for (OrderEntryData entry : cart.getEntries()) {
            OPFOrderLineData line = new OPFOrderLineData();
            line.setQuantity(entry.getQuantity());

            if (entry.getDeliveryMode() != null) {
                line.setDeliveryMethod(entry.getDeliveryMode().getCode());
            }

            if (entry.getTotalPrice() != null && entry.getTotalPrice().getValue() != null) {
                double totalPrice = entry.getTotalPrice().getValue().doubleValue();
                line.setLineTotal(totalPrice);
                line.setLineSubTotalWithTax(totalPrice);
            }

            if (entry.getBasePrice() != null && entry.getBasePrice().getValue() != null) {
                double basePrice = entry.getBasePrice().getValue().doubleValue();
                line.setUnitPrice(basePrice);
                line.setUnitPriceWithTax(basePrice);
            }

            if (entry.getProduct() != null) {
                line.setProductId(entry.getProduct().getCode());
                line.setProductName(entry.getProduct().getName());
                line.setProductDescription(entry.getProduct().getDescription());
            }

            // Default values
            line.setLineDiscount(0.0);
            line.setLineShFeeTax(0.0);
            line.setLineShFeeTaxPercent(0.0);
            line.setLineTaxPercent(0.0);
            line.setLineTax(0.0);

            orderLines.add(line);
        }

        order.setOrderLines(orderLines);
    }

    /**
     * Converts an {@link AddressData} object into an {@link OPFPaymentAddressData} object.
     *
     * @param address the {@link AddressData} containing the customer address details to be converted.
     * @return a populated {@link OPFPaymentAddressData} with the information from {@code address}.
     */
    private OPFPaymentAddressData mapToOpfAddress(AddressData address) {
        OPFPaymentAddressData paymentAddress = new OPFPaymentAddressData();
        if (address != null) {
            paymentAddress.setFirstName(address.getFirstName());
            paymentAddress.setLastName(address.getLastName());
            paymentAddress.setAddressLine1(address.getLine1());
            paymentAddress.setZip(address.getPostalCode());
            paymentAddress.setCity(address.getTown());
            paymentAddress.setCountry(address.getCountry() != null ? address.getCountry().getIsocode() : StringUtils.EMPTY);
            paymentAddress.setPhoneNumber(address.getCellphone());
            paymentAddress.setEmailAddress(address.getEmail());
            paymentAddress.setState(address.getRegion() != null ? address.getRegion().getIsocode() : StringUtils.EMPTY);
        }
        return paymentAddress;
    }

    /**
     * populate shipping method
     *
     * @param cartData
     *         cartData
     * @return {@link OPFShippingMethodData}
     * @see OPFShippingMethodData
     */
    private OPFShippingMethodData populateShippingMethod(AbstractOrderData cartData) {
        OPFShippingMethodData shippingMethodData = new OPFShippingMethodData();
        if (cartData.getDeliveryMode() != null) {
            shippingMethodData.setId(cartData.getDeliveryMode().getCode());
            shippingMethodData.setName(cartData.getDeliveryMode().getName());
            if (cartData.getDeliveryCost() != null && cartData.getDeliveryCost().getValue() != null) {
                double deliveryCost = cartData.getDeliveryCost().getValue().doubleValue();
                shippingMethodData.setCost(deliveryCost);
            }
            shippingMethodData.setShFeeTax(0.0);
            shippingMethodData.setShFeeWithTax(0.0);
            shippingMethodData.setShFeeTaxPercent(0.0);
        }
        return shippingMethodData;
    }
    /**
     * get configuration value for key
     *
     * @param key
     *         key
     * @return {@link String}
     * @see String
     */
    private String getConfigurationValueForKey(final String key) {
        return getConfigurationService().getConfiguration().getString(key, StringUtils.EMPTY);
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public CommerceCommonI18NService getCommerceCommonI18NService() {
        return commerceCommonI18NService;
    }

    public void setCommerceCommonI18NService(CommerceCommonI18NService commerceCommonI18NService) {
        this.commerceCommonI18NService = commerceCommonI18NService;
    }
}
