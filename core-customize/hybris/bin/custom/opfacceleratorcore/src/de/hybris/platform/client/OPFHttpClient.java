/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.client;

import de.hybris.platform.opfservices.client.CCAdapterHttpClient;
import org.apache.commons.lang3.tuple.Pair;

public interface OPFHttpClient extends CCAdapterHttpClient {
    /**
     * Method is to return the security properties
     * @return Pair
     */
    Pair<String, String> getSecurityProperties();
}
