/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.opf.data.OPFDynamicScriptData;
import de.hybris.platform.opf.data.OPFInitiatePaymentData;
import de.hybris.platform.opf.data.OPFPaymentAttributeData;
import de.hybris.platform.opf.data.OPFPaymentDestinationData;
import de.hybris.platform.opf.data.OPFPaymentUrlsData;
import de.hybris.platform.opf.dto.OPFDynamicScript;
import de.hybris.platform.opf.dto.OPFInitiatePaymentSessionResponse;
import de.hybris.platform.opf.dto.OPFPaymentAttribute;
import de.hybris.platform.opf.dto.OPFPaymentDestination;
import de.hybris.platform.opf.dto.OPFPaymentUrls;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Populates OPFInitiatePaymentData.
 *
 * This populator is used to map internal payment data into a format suitable for frontend consumption during the payment session flow.
 */
public class OPFAcceleratorPaymentSessionResponsePopulator implements Populator<OPFInitiatePaymentSessionResponse, OPFInitiatePaymentData> {
    @Override
    public void populate(OPFInitiatePaymentSessionResponse response, OPFInitiatePaymentData data) throws ConversionException {
        data.setPattern(response.getPattern());
        data.setPaymentSessionId(response.getPaymentSessionId());
        data.setPaymentIntent(response.getPaymentIntent());
        data.setRelayResultUrl(response.getRelayResultUrl());
        data.setRelayCancelUrl(response.getRelayCancelUrl());

        if (response.getDestination() != null) {
            OPFPaymentDestination destination = response.getDestination();
            OPFPaymentDestinationData destinationData = new OPFPaymentDestinationData();
            destinationData.setUrl(destination.getUrl());
            destinationData.setMethod(destination.getMethod());
            destinationData.setContentType(destination.getContentType());
            destinationData.setBody(destination.getBody());
            destinationData.setForm(convertAttributes(destination.getForm()));
            data.setDestination(destinationData);
        }

        if (response.getDynamicScript() != null) {
            OPFDynamicScript dynamicScript = response.getDynamicScript();
            OPFDynamicScriptData dynamicScriptData = new OPFDynamicScriptData();
            dynamicScriptData.setHtml(dynamicScript.getHtml());
            dynamicScriptData.setCssUrls(convertUrls(dynamicScript.getCssUrls()));
            dynamicScriptData.setJsUrls(convertUrls(dynamicScript.getJsUrls()));
            data.setDynamicScript(dynamicScriptData);
        }
    }

    private List<OPFPaymentUrlsData> convertUrls(List<OPFPaymentUrls> urlDTOs) {
        List<OPFPaymentUrlsData> urlDataList = new ArrayList<>();

        if (urlDTOs != null) {
            for (OPFPaymentUrls url : urlDTOs) {
                if (url != null) {
                    OPFPaymentUrlsData urlData = new OPFPaymentUrlsData();
                    urlData.setUrl(url.getUrl());
                    urlData.setSri(url.getSri());
                    urlData.setAttributes(convertAttributes(url.getAttributes()));
                    urlDataList.add(urlData);
                }
            }
        }
        return urlDataList;
    }

    private List<OPFPaymentAttributeData> convertAttributes(List<OPFPaymentAttribute> attributeDTOs) {
        List<OPFPaymentAttributeData> attributeDataList = new ArrayList<>();

        if (attributeDTOs != null) {
            for (OPFPaymentAttribute attribute : attributeDTOs) {
                if (attribute != null) {
                    OPFPaymentAttributeData attributeData = new OPFPaymentAttributeData();
                    attributeData.setKey(attribute.getKey());
                    attributeData.setValue(attribute.getValue());
                    attributeData.setName(attribute.getName());
                    attributeDataList.add(attributeData);
                }
            }
        }

        return attributeDataList;
    }
}
