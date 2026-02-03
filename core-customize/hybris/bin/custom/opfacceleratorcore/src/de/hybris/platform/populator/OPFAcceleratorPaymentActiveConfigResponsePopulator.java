/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.populator;

import com.opf.dto.cta.OPFActiveConfigDTO;
import com.opf.dto.cta.OPFActiveConfigDigitalWalletDTO;
import com.opf.dto.cta.OPFActiveConfigValueDTO;
import com.opf.dto.cta.OPFPageableDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.data.response.OPFActiveConfigDigitalWallet;
import de.hybris.platform.data.response.OPFActiveConfigResponse;
import de.hybris.platform.data.response.OPFPageableData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections4.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Populator implementation for converting OPFActiveConfigResponse to OPFActiveConfigDTO.
 * This class handles the mapping of data between the source and target objects.
 */
public class OPFAcceleratorPaymentActiveConfigResponsePopulator implements Populator<OPFActiveConfigResponse, OPFActiveConfigDTO> {
    private static final String OPF_ACTIVE_CONFIG_PAYMENT_TYPE = "opf.active.config.payment.type";
    private static final String CARD_PAYMENT_TYPE = "CARD";
    @Resource(name = "configurationService")
    private ConfigurationService configurationService;

    @Override
    public void populate(OPFActiveConfigResponse source, OPFActiveConfigDTO target) throws ConversionException {
        if (source != null && target != null) {

            target.setValue(source.getValue().stream().map(activeConfigValue -> {
                OPFActiveConfigValueDTO activeConfigValueWsDTO = new OPFActiveConfigValueDTO();
                Boolean considerActiveFlag = configurationService.getConfiguration()
                        .getBoolean(OPF_ACTIVE_CONFIG_PAYMENT_TYPE, Boolean.FALSE);
                if ((considerActiveFlag && activeConfigValue.isActive()) || !considerActiveFlag) {
                    activeConfigValueWsDTO.setId(activeConfigValue.getId());
                    activeConfigValueWsDTO.setDisplayName(activeConfigValue.getDisplayName());
                    activeConfigValueWsDTO.setProviderType(activeConfigValue.getProvider());
                    activeConfigValueWsDTO.setLogoUrl(activeConfigValue.getLogoUrl());
                    activeConfigValueWsDTO.setMerchantId(activeConfigValue.getMerchantId());
                    activeConfigValueWsDTO.setPaymentType(CARD_PAYMENT_TYPE);
                    activeConfigValueWsDTO.setDigitalWalletQuickBuy(populateDigitalWalletDTO(activeConfigValue.getDigitalWalletQuickBuy()));
                }
                return activeConfigValueWsDTO;
            }).collect(Collectors.toList()));
            target.setPageable(populatePageableData(source.getPageable()));
        }
    }

    /**
     * populate digital wallet d t o
     *
     * @param activeConfigDigitalWalletList activeConfigDigitalWalletList
     * @return {@link List}
     * @see List
     * @see OPFActiveConfigDigitalWalletDTO
     */
    private List<OPFActiveConfigDigitalWalletDTO> populateDigitalWalletDTO(
            List<OPFActiveConfigDigitalWallet> activeConfigDigitalWalletList) {
        List<OPFActiveConfigDigitalWalletDTO> activeConfigDigitalWalletWsDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(activeConfigDigitalWalletList)) {
            for (OPFActiveConfigDigitalWallet activeConfigDigitalWallet : activeConfigDigitalWalletList) {
                OPFActiveConfigDigitalWalletDTO activeConfigDigitalWalletWsDTO = new OPFActiveConfigDigitalWalletDTO();
                activeConfigDigitalWalletWsDTO.setMerchantId(activeConfigDigitalWallet.getMerchantId());
                activeConfigDigitalWalletWsDTO.setProvider(activeConfigDigitalWallet.getProvider());
                activeConfigDigitalWalletWsDTO.setEnabled(activeConfigDigitalWallet.isEnabled());
                activeConfigDigitalWalletWsDTO.setCountryCode(activeConfigDigitalWallet.getCountryCode());
                activeConfigDigitalWalletWsDTO.setGooglePayGateway(activeConfigDigitalWallet.getGooglePayGateway());

                activeConfigDigitalWalletWsDTOList.add(activeConfigDigitalWalletWsDTO);
            }
        }

        return activeConfigDigitalWalletWsDTOList;
    }

    /**
     * populate pageable data
     *
     * @param opfPageableData opfPageableData
     * @return {@link OPFPageableDTO}
     * @see OPFPageableDTO
     */
    private OPFPageableDTO populatePageableData(OPFPageableData opfPageableData) {
        OPFPageableDTO opfPageableWsDTO = new OPFPageableDTO();
        if (opfPageableData != null) {
            opfPageableWsDTO.setNumber(opfPageableData.getNumber());
            opfPageableWsDTO.setSize(opfPageableData.getSize());
            opfPageableWsDTO.setTotalElements(opfPageableData.getTotalElements());
            opfPageableWsDTO.setTotalPages(opfPageableData.getTotalPages());
        }
        return opfPageableWsDTO;
    }
}
