/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfb2bacceleratoraddon.forms;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PaymentTypeForm {
    private String paymentType;
    private String costCenterId;
    private String purchaseOrderNumber;
    private Integer paymentId;

    @NotNull(message = "{general.required}")
    @Size(min = 1, max = 255, message = "{general.required}")
    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(final String paymentType) {
        this.paymentType = paymentType;
    }

    public String getCostCenterId() {
        return costCenterId;
    }

    public void setCostCenterId(final String costCenterId) {
        this.costCenterId = costCenterId;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(final String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }
}
