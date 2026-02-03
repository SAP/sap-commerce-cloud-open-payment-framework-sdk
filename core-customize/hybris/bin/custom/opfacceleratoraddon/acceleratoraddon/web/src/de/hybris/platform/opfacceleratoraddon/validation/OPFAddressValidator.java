/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.validation;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * Validator for address forms. Enforces the order of validation
 */

public class OPFAddressValidator implements Validator {
    private static final int MAX_FIELD_LENGTH = 255;
    private static final int MAX_POSTCODE_LENGTH = 10;
    private final I18NService i18NService;
    private final MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> aClass) {
        return AddressData.class.equals(aClass);
    }

    /**
     * Constructs a new OPFAddressValidator with the specified services.
     *
     * @param i18NService The service used for internationalization and locale-specific operations.
     * @param messageSource The source for resolving messages, supporting internationalization.
     */
    public OPFAddressValidator(final I18NService i18NService, final MessageSource messageSource) {
        this.i18NService = i18NService;
        this.messageSource = messageSource;
    }

    /**
     * validate
     *
     * @param object object
     * @param errors errors
     */
    @Override
    public void validate(final Object object, final Errors errors) {
        final AddressData addressData = (AddressData) object;
        validateStandardFields(addressData, errors);
        validateCountrySpecificFields(addressData, errors);
    }

    /**
     * validate standard fields
     *
     * @param addressData addressData
     * @param errors errors
     */
    protected void validateStandardFields(final AddressData addressData, final Errors errors) {
        validateStringField(addressData.getCountry().getIsocode(), AddressField.COUNTRY, MAX_FIELD_LENGTH, errors);
        validateStringField(addressData.getFirstName(), AddressField.FIRSTNAME, MAX_FIELD_LENGTH, errors);
        validateStringField(addressData.getLastName(), AddressField.LASTNAME, MAX_FIELD_LENGTH, errors);
        validateStringField(addressData.getLine1(), AddressField.LINE1, MAX_FIELD_LENGTH, errors);
        validateStringField(addressData.getTown(), AddressField.TOWN, MAX_FIELD_LENGTH, errors);
        validateStringField(addressData.getPostalCode(), AddressField.POSTCODE, MAX_POSTCODE_LENGTH, errors);
    }

    /**
     * validate country specific fields
     *
     * @param addressData addressData
     * @param errors errors
     */
    protected void validateCountrySpecificFields(final AddressData addressData, final Errors errors) {
        final String isoCode = addressData.getCountry().getIsocode();
        final RegionData regionData = addressData.getRegion();
        if (isoCode != null) {
            switch (CountryCode.lookup(isoCode)) {
                case CHINA:
                case CANADA:
                case USA:
                    validateStringFieldLength(addressData.getTitleCode(), AddressField.TITLE, MAX_FIELD_LENGTH, errors);
                    if (regionData != null) {
                        validateFieldNotNull(regionData.getIsocode(), AddressField.REGION, errors);
                    }
                    break;
                case JAPAN:
                    if (regionData != null) {
                        validateFieldNotNull(regionData.getIsocode(), AddressField.REGION, errors);
                    }
                    validateStringField(addressData.getLine2(), AddressField.LINE2, MAX_FIELD_LENGTH, errors);
                    break;
                default:
                    validateStringFieldLength(addressData.getTitleCode(), AddressField.TITLE, MAX_FIELD_LENGTH, errors);
                    break;
            }
        }
    }

    /**
     * validate string field
     *
     * @param addressField addressField
     * @param fieldType fieldType
     * @param maxFieldLength maxFieldLength
     * @param errors errors
     */
    protected void validateStringField(final String addressField, final AddressField fieldType, final int maxFieldLength,
            final Errors errors) {
        if (addressField == null || StringUtils.isEmpty(addressField) || (StringUtils.length(addressField) > maxFieldLength)) {
            String errorMessage = messageSource.getMessage(fieldType.getErrorKey(), null, i18NService.getCurrentLocale());
            errors.rejectValue(fieldType.getFieldKey(), null, errorMessage);
        }
    }

    /**
     * validate string field length
     *
     * @param field field
     * @param fieldType fieldType
     * @param maxFieldLength maxFieldLength
     * @param errors errors
     */
    protected void validateStringFieldLength(final String field, final AddressField fieldType, final int maxFieldLength,
            final Errors errors) {
        if (StringUtils.isNotEmpty(field) && StringUtils.length(field) > maxFieldLength) {
            String errorMessage = messageSource.getMessage(fieldType.getErrorKey(), null, i18NService.getCurrentLocale());
            errors.rejectValue(fieldType.getFieldKey(), null, errorMessage);
        }
    }

    /**
     * validate field not null
     *
     * @param addressField addressField
     * @param fieldType fieldType
     * @param errors errors
     */
    protected void validateFieldNotNull(final String addressField, final AddressField fieldType, final Errors errors) {
        if (addressField == null) {
            String errorMessage = messageSource.getMessage(fieldType.getErrorKey(), null, i18NService.getCurrentLocale());
            errors.rejectValue(fieldType.getFieldKey(), null, errorMessage);
        }
    }

    protected enum CountryCode {
        USA("US"), CANADA("CA"), JAPAN("JP"), CHINA("CN"), BRITAIN("GB"), GERMANY("DE"), DEFAULT("");
        private static Map<String, CountryCode> lookupMap = new HashMap<>();
        private String isoCode;

        static {
            for (final CountryCode code : CountryCode.values()) {
                lookupMap.put(code.getIsoCode(), code);
            }
        }

        private CountryCode(final String isoCodeStr) {
            this.isoCode = isoCodeStr;
        }

        public static CountryCode lookup(final String isoCodeStr) {
            CountryCode code = lookupMap.get(isoCodeStr);
            if (code == null) {
                code = DEFAULT;
            }
            return code;
        }

        public String getIsoCode() {
            return isoCode;
        }
    }

    protected enum AddressField {
        TITLE("titleCode", "address.title.invalid"), FIRSTNAME("firstName", "address.firstName.invalid"), LASTNAME("lastName",
                "address.lastName.invalid"), LINE1("line1", "address.line1.invalid"), LINE2("line2", "address.line2.invalid"), TOWN(
                "townCity", "address.townCity.invalid"), POSTCODE("postcode", "address.postcode.invalid"), REGION("regionIso",
                "address.regionIso.invalid"), COUNTRY("countryIso", "address.country.invalid");

        private String fieldKey;
        private String errorKey;

        private AddressField(final String fieldKey, final String errorKey) {
            this.fieldKey = fieldKey;
            this.errorKey = errorKey;
        }

        public String getFieldKey() {
            return fieldKey;
        }

        public String getErrorKey() {
            return errorKey;
        }
    }
}
