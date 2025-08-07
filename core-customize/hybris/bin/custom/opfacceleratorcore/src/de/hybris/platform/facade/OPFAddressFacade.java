/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.facade;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.opf.dto.user.AddressWsDTO;

public interface OPFAddressFacade {
    /**
     * Maps an {@link AddressWsDTO} object to an {@link AddressData} object. This method performs a shallow copy of simple properties using
     * {@link org.springframework.beans.BeanUtils} and explicitly maps nested objects such as {@code CountryWsDTO} to {@code CountryData}.
     * It is useful in controller or facade layers where incoming web service DTOs need to be transformed into internal data objects used in
     * the service layer.
     *
     * @param source
     *         the {@link AddressWsDTO} object containing data from the web service request. Must not be {@code null}.
     * @return an {@link AddressData} object populated with values from the given {@code AddressWsDTO}.
     */
    public AddressData mapAddressWsDTOToAddressData(AddressWsDTO source);
}
