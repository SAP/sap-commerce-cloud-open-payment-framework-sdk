/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.client;

import de.hybris.platform.client.impl.DefaultOPFHttpClient;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.retry.support.RetryTemplate;

import static org.junit.Assert.assertNotNull;

public class DefaultOPFHttpClientTest {

    @InjectMocks
    private RetryTemplate retryTemplate;
    @InjectMocks
    private ConfigurationService configurationService;

    @Test
    public void testConstructorShouldInitializeObject() {
        String tokenUrlKey = "opf.oauth.token.url";
        String securityFileKey = "opf.oauth.client-secret.file";
        String securityFileLocationKey = "opf.oauth.client-secret.file.location";

        DefaultOPFHttpClient client = new DefaultOPFHttpClient(tokenUrlKey, securityFileKey, securityFileLocationKey, retryTemplate,
                configurationService);

        assertNotNull(client);
    }
}

