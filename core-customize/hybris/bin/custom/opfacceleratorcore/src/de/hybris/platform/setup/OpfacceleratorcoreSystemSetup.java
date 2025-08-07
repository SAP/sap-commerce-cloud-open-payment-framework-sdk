/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.setup;

import static de.hybris.platform.constants.OpfacceleratorcoreConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import de.hybris.platform.constants.OpfacceleratorcoreConstants;
import de.hybris.platform.service.OpfacceleratorcoreService;


@SystemSetup(extension = OpfacceleratorcoreConstants.EXTENSIONNAME)
public class OpfacceleratorcoreSystemSetup
{
	private final OpfacceleratorcoreService opfacceleratorcoreService;

	public OpfacceleratorcoreSystemSetup(final OpfacceleratorcoreService opfacceleratorcoreService)
	{
		this.opfacceleratorcoreService = opfacceleratorcoreService;
	}

	@SystemSetup(process = SystemSetup.Process.ALL, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		opfacceleratorcoreService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return OpfacceleratorcoreSystemSetup.class.getResourceAsStream("/opfacceleratorcore/sap-hybris-platform.png");
	}
}
