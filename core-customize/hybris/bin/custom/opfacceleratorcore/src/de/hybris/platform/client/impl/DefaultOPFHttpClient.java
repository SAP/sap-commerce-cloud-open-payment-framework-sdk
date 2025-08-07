/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.client.impl;

import de.hybris.platform.client.OPFHttpClient;
import de.hybris.platform.opfservices.client.OAuth2TokenException;
import de.hybris.platform.opfservices.client.impl.DefaultCCAdapterHttpClient;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.retry.support.RetryTemplate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static de.hybris.platform.constants.OpfacceleratorcoreConstants.OPF_CLIENT_ID;
import static de.hybris.platform.constants.OpfacceleratorcoreConstants.OPF_PUBLIC_KEY;

public class DefaultOPFHttpClient extends DefaultCCAdapterHttpClient implements OPFHttpClient {

    public DefaultOPFHttpClient(String tokenUrlKey, String securityFileKey, String securityFileLocationKey, RetryTemplate retryTemplate,
            ConfigurationService configurationService) {
        super(tokenUrlKey, securityFileKey, securityFileLocationKey, retryTemplate, configurationService);
    }

    /**
     * To get CLIENT ID and PUBLIC KEY from the security file
     * @return
     */
    public Pair<String, String> getSecurityProperties()
    {
        final Configuration configuration = this.getConfigurationService().getConfiguration();
        final String securityFile = configuration.getString(this.getSecurityFileLocationKey()) + File.separator
                + configuration.getString(this.getSecurityFileKey());
        try (final FileReader reader = new FileReader(securityFile, StandardCharsets.UTF_8))
        {
            final Properties properties = new Properties();
            properties.load(reader);
            return Pair.of(properties.getProperty(OPF_CLIENT_ID), properties.getProperty(OPF_PUBLIC_KEY));
        }
        catch (final IOException e)
        {
            throw new OAuth2TokenException("Cannot load OPF tenant client id & public key", e);
        }
    }
}
