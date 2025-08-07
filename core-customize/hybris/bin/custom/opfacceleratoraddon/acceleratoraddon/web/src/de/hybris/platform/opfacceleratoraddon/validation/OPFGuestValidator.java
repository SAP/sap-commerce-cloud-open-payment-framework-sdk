/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.validation;

import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OPFGuestValidator implements Validator {
    private final I18NService i18NService;
    private final MessageSource messageSource;
    private ConfigurationService configurationService;

    @Override
    public boolean supports(final Class<?> aClass) {
        return CustomerData.class.equals(aClass);
    }

    public OPFGuestValidator(final I18NService i18NService, final MessageSource messageSource,
            final ConfigurationService configurationService) {
        this.i18NService = i18NService;
        this.messageSource = messageSource;
        this.configurationService = configurationService;
    }

    @Override
    public void validate(final Object object, final Errors errors) {
        final CustomerData customerData = (CustomerData) object;
        String email = customerData.getSapGuestUserEmail();
        if (StringUtils.isEmpty(email) || StringUtils.length(email) > 255 || !validateEmailAddress(email)) {
            errors.rejectValue("email", messageSource.getMessage("profile.email.invalid", null, i18NService.getCurrentLocale()));
        }
    }

    protected boolean validateEmailAddress(final String email) {
        try {
            final Matcher matcher = Pattern.compile(configurationService.getConfiguration().getString(WebConstants.EMAIL_REGEX)).matcher(email);
            return matcher.matches();
        } catch (Exception e) {
            return false;
        }
    }
}
