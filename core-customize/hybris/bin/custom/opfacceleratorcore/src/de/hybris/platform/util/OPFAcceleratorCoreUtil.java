/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.util;

import de.hybris.platform.constants.OpfacceleratorcoreConstants;
import org.apache.commons.lang3.StringUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public final class OPFAcceleratorCoreUtil
{
	private OPFAcceleratorCoreUtil(){
	}

	/**
	 * Validate if payment integration is Quick Buy
	 * @param paymentMethod
	 * @return
	 */
	public static boolean isQuickBuy(String paymentMethod){
		return StringUtils.equalsAnyIgnoreCase(paymentMethod, OpfacceleratorcoreConstants.GOOGLE_PAY,
				OpfacceleratorcoreConstants.APPLE_PAY);
	}
}
