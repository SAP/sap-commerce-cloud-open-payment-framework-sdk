/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfb2bacceleratoraddon.forms;

import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.cronjob.enums.DayOfWeek;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.AssertTrue;

/**
 * Pojo for 'place order' form.
 */
public class PlaceOrderForm {
    private String securityCode;
    private boolean termsCheck;
    private boolean replenishmentOrder;
    private Date replenishmentStartDate;
    private String nDays;
    private String nWeeks;
    private String nthDayOfMonth;
    private B2BReplenishmentRecurrenceEnum replenishmentRecurrence;
    private List<DayOfWeek> nDaysOfWeek;

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(final String securityCode) {
        this.securityCode = securityCode;
    }

    @AssertTrue(message = "")
    public boolean isTermsCheck() {
        return termsCheck;
    }

    public void setTermsCheck(final boolean termsCheck) {
        this.termsCheck = termsCheck;
    }

    public Date getReplenishmentStartDate() {
        return replenishmentStartDate;
    }

    public void setReplenishmentStartDate(final Date replenishmentStartDate) {
        this.replenishmentStartDate = replenishmentStartDate;
    }

    public String getnDays() {
        return nDays;
    }

    public void setnDays(final String nDays) {
        this.nDays = nDays;
    }

    public String getnWeeks() {
        return nWeeks;
    }

    public void setnWeeks(final String nWeeks) {
        this.nWeeks = nWeeks;
    }

    public String getNthDayOfMonth() {
        return nthDayOfMonth;
    }

    public void setNthDayOfMonth(final String nthDayOfMonth) {
        this.nthDayOfMonth = nthDayOfMonth;
    }

    public B2BReplenishmentRecurrenceEnum getReplenishmentRecurrence() {
        return replenishmentRecurrence;
    }

    public void setReplenishmentRecurrence(final B2BReplenishmentRecurrenceEnum replenishmentRecurrence) {
        this.replenishmentRecurrence = replenishmentRecurrence;
    }

    public List<DayOfWeek> getnDaysOfWeek() {
        return nDaysOfWeek;
    }

    public void setnDaysOfWeek(final List<DayOfWeek> nDaysOfWeek) {
        this.nDaysOfWeek = nDaysOfWeek;
    }

    public boolean isReplenishmentOrder() {
        return replenishmentOrder;
    }

    public void setReplenishmentOrder(final boolean replenishmentOrder) {
        this.replenishmentOrder = replenishmentOrder;
    }
}
