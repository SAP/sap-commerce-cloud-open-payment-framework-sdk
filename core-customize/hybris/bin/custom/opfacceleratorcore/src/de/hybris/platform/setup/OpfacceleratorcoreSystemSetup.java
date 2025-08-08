/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.setup;

import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.constants.OpfacceleratorcoreConstants;


@SystemSetup(extension = OpfacceleratorcoreConstants.EXTENSIONNAME)
public class OpfacceleratorcoreSystemSetup
{


	@SystemSetup(process = SystemSetup.Process.ALL, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{

	}

}
