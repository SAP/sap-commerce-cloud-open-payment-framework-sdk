/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.facade.impl;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.impl.DefaultCheckoutPaymentFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.facade.OPFCheckoutPaymentFacade;
import de.hybris.platform.order.CartService;
import jakarta.annotation.Resource;
import java.util.Optional;


/**
 * Default implementation of {@link OPFCheckoutPaymentFacade}
 */
public class DefaultOPFCheckoutPaymentFacade extends DefaultCheckoutPaymentFacade implements OPFCheckoutPaymentFacade {
    @Resource(name = "cartService")
    private CartService cartService;

    /**
     * Set Billing Address as a Shipping Address
     *
     * @param addressData
     *         Shipping Address Data
     */
    @Override
    public void setBillingAddressFromShipping(AddressData addressData) {
        Optional.ofNullable(this.getCart()).ifPresent(cart -> {
            AddressModel address = cart.getPaymentAddress();
            if (address == null && null != addressData) {
                address = this.getModelService().create(AddressModel.class);
                address.setOwner(cart);
            }
            addressData.setBillingAddress(Boolean.TRUE);
            this.getAddressReversePopulator().populate(addressData, address);
            address.setDuplicate(Boolean.FALSE);
            cart.setPaymentAddress(address);
            this.getModelService().saveAll(address, cart);
            this.getModelService().refresh(cart);
        });
    }

    /**
     * Remove Shipping Address from the cart Payment Address
     *
     * @param billingAddress
     *         Billing Address data
     */
    @Override
    public void removeShippingFromPaymentAddress(AddressData billingAddress) {
        if (billingAddress == null) {
            return;
        }
        Optional.ofNullable(this.getCart()).ifPresent(cart -> {
            AddressModel address = cart.getPaymentAddress();
            if (address != null) {
                address.setBillingAddress(Boolean.FALSE);
                cart.setPaymentAddress(null);
                this.getModelService().saveAll(address, cart);
                this.getModelService().refresh(cart);
            }
        });
    }

    /**
     * Determines whether the user associated with the current session cart is a guest user.
     *
     * @return Boolean flag true/false
     */
    @Override
    public boolean isGuestUser() {
        CartModel cartModel = cartService.getSessionCart();
        if (cartModel == null || cartModel.getUser() == null) {
            return false;
        }
        ItemModel user = cartModel.getUser();
        return (user instanceof CustomerModel customer) && CustomerType.GUEST.equals(customer.getType());
    }

    @Override
    public void setPaymentIDOnCart(String paymentID) {
        CartModel cartModel = cartService.getSessionCart();
        if (cartModel != null) {
            cartModel.setSapPaymentOptionId(paymentID);

            getModelService().save(cartModel);
            getModelService().refresh(cartModel);
        }
    }

    @Override
    public CartData getOPFCheckoutCart() {
        CartModel cartModel = cartService.getSessionCart();
        CartData cartData=super.getCheckoutCart();
        if(cartData!=null){
          cartData.setOpfB2BPaymentId(cartModel.getSapPaymentOptionId());
        }
        return cartData;
    }



}
