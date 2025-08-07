/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.facade.impl;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.facade.OPFAddressFacade;
import de.hybris.platform.opf.dto.user.AddressWsDTO;
import de.hybris.platform.opf.dto.user.CountryWsDTO;
import org.springframework.beans.BeanUtils;

import java.util.Optional;

public class DefaultOPFAddressFacade implements OPFAddressFacade {

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
    @Override
    public AddressData mapAddressWsDTOToAddressData(AddressWsDTO source) {
        AddressData target = new AddressData();
        BeanUtils.copyProperties(source, target);

        Optional.ofNullable(source.getCountry()).ifPresent(country -> {
            CountryData countryData = new CountryData();
            BeanUtils.copyProperties(country, countryData);
            target.setCountry(countryData);
        });

        Optional.ofNullable(source.getRegion()).ifPresent(region -> {
            RegionData regionData = new RegionData();
            String regionIso = region.getIsocode();
            String countryIso = Optional.ofNullable(source.getCountry()).map(CountryWsDTO::getIsocode).orElse(null);
            if (regionIso != null && countryIso != null) {
                regionData.setIsocodeShort(regionIso);
                regionData.setCountryIso(countryIso);
                regionData.setIsocode(regionIso.contains("-") ? regionIso : countryIso + "-" + regionIso);
                target.setRegion(regionData);
            }
        });
        return target;
    }
}

