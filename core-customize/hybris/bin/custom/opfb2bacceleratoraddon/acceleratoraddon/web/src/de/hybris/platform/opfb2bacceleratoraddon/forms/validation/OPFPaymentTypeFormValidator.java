/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfb2bacceleratoraddon.forms.validation;

import de.hybris.platform.b2b.enums.CheckoutPaymentType;

import de.hybris.platform.opfb2bacceleratoraddon.forms.PaymentTypeForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for {@link PaymentTypeForm}.
 */
@Component("opfPaymentTypeFormValidator")
public class OPFPaymentTypeFormValidator implements Validator {

    @Override
    public boolean supports(final Class<?> clazz) {
        return PaymentTypeForm.class.equals(clazz);
    }

    @Override
    public void validate(final Object object, final Errors errors) {
        if (object instanceof PaymentTypeForm) {
            final PaymentTypeForm paymentTypeForm = (PaymentTypeForm) object;

            if (null == paymentTypeForm.getPaymentType()){
                errors.rejectValue("paymentType", "general.required");
            }
            if (CheckoutPaymentType.ACCOUNT.getCode().equals(paymentTypeForm.getPaymentType()) && StringUtils.isBlank(
                    paymentTypeForm.getCostCenterId())) {
                errors.rejectValue("costCenterId", "general.required");
            }
        }
    }
}
