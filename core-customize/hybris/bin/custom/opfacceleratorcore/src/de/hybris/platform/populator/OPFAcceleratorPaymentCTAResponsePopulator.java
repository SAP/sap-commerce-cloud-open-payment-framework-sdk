/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import com.opf.dto.cta.CTAAttributesDTO;
import com.opf.dto.cta.CTADynamicScriptDTO;
import com.opf.dto.cta.CTAResponseDTO;
import com.opf.dto.cta.CTAValueDTO;

import com.opf.dto.cta.CTAUrlDataDTO;
import de.hybris.platform.converters.Populator;

import de.hybris.platform.cta.response.OPFPaymentCTAAttributes;
import de.hybris.platform.cta.response.OPFPaymentCTAResponse;
import de.hybris.platform.cta.response.OPFPaymentCTAUrlsData;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OPFAcceleratorPaymentCTAResponsePopulator implements Populator<OPFPaymentCTAResponse, CTAResponseDTO> {
    @Override
    public void populate(OPFPaymentCTAResponse source, CTAResponseDTO target) throws ConversionException {
        if (source != null && target != null) {

            target.setValue(source.getValue().stream().map(ctaValue -> {
                CTAValueDTO ctaValueWsDTO = new CTAValueDTO();
                ctaValueWsDTO.setPaymentAccountId(ctaValue.getAccountId());
                CTADynamicScriptDTO dynamicScriptWsDTO = new CTADynamicScriptDTO();
                dynamicScriptWsDTO.setHtml(ctaValue.getDynamicScript().getHtml());
                dynamicScriptWsDTO.setCssUrls(populateUrlData(ctaValue.getDynamicScript().getCssUrls()));
                dynamicScriptWsDTO.setJsUrls(populateUrlData(ctaValue.getDynamicScript().getJsUrls()));
                ctaValueWsDTO.setDynamicScript(dynamicScriptWsDTO);
                return ctaValueWsDTO;
            }).collect(Collectors.toList()));
        }
    }

    private List<CTAUrlDataDTO> populateUrlData(List<OPFPaymentCTAUrlsData> ctaUrlsList) {
        List<CTAUrlDataDTO> ctaUrlDataWsDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(ctaUrlsList)) {
            return ctaUrlDataWsDTOList;
        }
        for (OPFPaymentCTAUrlsData ctaCssUrl : ctaUrlsList) {
            CTAUrlDataDTO ctaUrlDataWsDTO = new CTAUrlDataDTO();
            ctaUrlDataWsDTO.setUrl(ctaCssUrl.getUrl());
            ctaUrlDataWsDTO.setSri(ctaCssUrl.getSri());
            List<OPFPaymentCTAAttributes> ctaAttributesList = ctaCssUrl.getAttributes();
            List<CTAAttributesDTO> attributesList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(ctaAttributesList)) {
                for (OPFPaymentCTAAttributes ctaAttribute : ctaAttributesList) {
                    CTAAttributesDTO attribute = new CTAAttributesDTO();
                    attribute.setKey(ctaAttribute.getKey());
                    attribute.setValue(ctaAttribute.getValue());
                    attributesList.add(attribute);
                }
                ctaUrlDataWsDTO.setAttributes(attributesList);
            }
            ctaUrlDataWsDTOList.add(ctaUrlDataWsDTO);
        }
        return ctaUrlDataWsDTOList;
    }
}
