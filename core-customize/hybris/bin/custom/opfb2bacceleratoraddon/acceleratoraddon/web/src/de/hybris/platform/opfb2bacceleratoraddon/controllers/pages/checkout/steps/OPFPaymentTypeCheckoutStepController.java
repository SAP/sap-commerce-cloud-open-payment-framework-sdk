/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfb2bacceleratoraddon.controllers.pages.checkout.steps;

import com.opf.order.data.OPFB2BPaymentTypeData;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorfacades.api.cart.CheckoutFacade;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.b2bcommercefacades.company.B2BCostCenterFacade;
import de.hybris.platform.b2bcommercefacades.company.data.B2BCostCenterData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.facade.OPFAcceleratorFacade;
import de.hybris.platform.facade.OPFCheckoutPaymentFacade;
import de.hybris.platform.opfb2bacceleratoraddon.forms.PaymentTypeForm;
import de.hybris.platform.opfb2bacceleratoraddon.forms.validation.OPFPaymentTypeFormValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping(value = "/checkout/multi/opf-b2b-payment-type")
public class OPFPaymentTypeCheckoutStepController extends AbstractCheckoutStepController {
    private final static String OPF_PAYMENT_TYPE = "opf-payment-type";

    @Resource(name = "b2bCheckoutFacade")
    private CheckoutFacade b2bCheckoutFacade;

    @Resource(name = "opfAcceleratorFacade")
    private OPFAcceleratorFacade opfAcceleratorFacade;

    @Resource(name = "opfCheckoutPaymentFacade")
    private OPFCheckoutPaymentFacade opfCheckoutPaymentFacade;

    @Resource(name = "costCenterFacade")
    private B2BCostCenterFacade costCenterFacade;

    @Resource(name = "opfPaymentTypeFormValidator")
    private OPFPaymentTypeFormValidator opfPaymentTypeFormValidator;

    @Resource(name = "cartFacade")
    private CartFacade cartFacade;

    @ModelAttribute("paymentTypes")
    public Collection<OPFB2BPaymentTypeData> getAllB2BPaymentTypes() {
        return opfAcceleratorFacade.getB2BActiveConfigurations();
    }

    @ModelAttribute("costCenters")
    public List<? extends B2BCostCenterData> getVisibleActiveCostCenters() {
        final List<? extends B2BCostCenterData> costCenterData = costCenterFacade.getActiveCostCenters();
        return costCenterData == null ? Collections.<B2BCostCenterData> emptyList() : costCenterData;
    }

    @Override
    @RequestMapping(value = "/choose", method = RequestMethod.GET)
    @RequireHardLogIn
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = OPF_PAYMENT_TYPE)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes)
            throws CMSItemNotFoundException, CommerceCartModificationException {
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute("cartData", cartData);
        model.addAttribute("paymentTypeForm", preparePaymentTypeForm(cartData));
        prepareDataForPage(model);
        final ContentPageModel multiCheckoutSummaryPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, multiCheckoutSummaryPage);
        setUpMetaDataForContentPage(model, multiCheckoutSummaryPage);
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentType.breadcrumb"));
        model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
        setCheckoutStepLinksForModel(model, getCheckoutStep());

        return Opfb2bacceleratoraddonControllerConstants.Views.Pages.MultiStepCheckout.ChoosePaymentTypePage;
    }

    @RequestMapping(value = "/choose", method = RequestMethod.POST)
    @RequireHardLogIn
    public String choose(@ModelAttribute final PaymentTypeForm paymentTypeForm, final BindingResult bindingResult, final Model model)
            throws CMSItemNotFoundException, CommerceCartModificationException {
        String selectedPaymentId = paymentTypeForm.getPaymentType();

        Collection<OPFB2BPaymentTypeData> paymentTypeData = (Collection<OPFB2BPaymentTypeData>) model.getAttribute("paymentTypes");

        //Swap the paymentId and paymentType to set the correct values in the form
        paymentTypeData.stream().forEach(type -> {
            if (type.getId().toString().equals(selectedPaymentId)) {
                paymentTypeForm.setPaymentId(Integer.parseInt(selectedPaymentId));
                paymentTypeForm.setPaymentType(type.getCode());
            }
        });
        opfPaymentTypeFormValidator.validate(paymentTypeForm, bindingResult);

        if (bindingResult.hasErrors()) {
            GlobalMessages.addErrorMessage(model, "checkout.error.paymenttype.formentry.invalid");
            model.addAttribute("paymentTypeForm", paymentTypeForm);
            prepareDataForPage(model);
            final ContentPageModel multiCheckoutSummaryPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
            storeCmsPageInModel(model, multiCheckoutSummaryPage);
            setUpMetaDataForContentPage(model, multiCheckoutSummaryPage);
            model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                    getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentType.breadcrumb"));
            model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
            setCheckoutStepLinksForModel(model, getCheckoutStep());
            return Opfb2bacceleratoraddonControllerConstants.Views.Pages.MultiStepCheckout.ChoosePaymentTypePage;
        }

        updateCheckoutCart(paymentTypeForm);

        checkAndSelectDeliveryAddress(paymentTypeForm);

        return getCheckoutStep().nextStep();
    }

    protected void updateCheckoutCart(final PaymentTypeForm paymentTypeForm) throws CommerceCartModificationException {
        final CartData cartData = new CartData();

        // set payment type
        final B2BPaymentTypeData paymentTypeData = new B2BPaymentTypeData();
        paymentTypeData.setCode(paymentTypeForm.getPaymentType());

        cartData.setPaymentType(paymentTypeData);

        // set cost center
        if (CheckoutPaymentType.ACCOUNT.getCode().equals(cartData.getPaymentType().getCode())) {
            final B2BCostCenterData costCenter = new B2BCostCenterData();
            costCenter.setCode(paymentTypeForm.getCostCenterId());

            cartData.setCostCenter(costCenter);
        }

        // set purchase order number
        cartData.setPurchaseOrderNumber(paymentTypeForm.getPurchaseOrderNumber());

        //Unset sapPaymentOptionId from cart before setting new payment info on cart
        opfAcceleratorFacade.clearSapPaymentOptionId();

        b2bCheckoutFacade.updateCheckoutCart(cartData);
        opfCheckoutPaymentFacade.setPaymentIDOnCart(String.valueOf(paymentTypeForm.getPaymentId()));
        if (CheckoutPaymentType.ACCOUNT.getCode().equals(cartData.getPaymentType().getCode())) {
            opfAcceleratorFacade.setPaymentInfoOnCartForAccount();
        }
    }

    protected void checkAndSelectDeliveryAddress(final PaymentTypeForm paymentTypeForm) {
        if (CheckoutPaymentType.ACCOUNT.getCode().equals(paymentTypeForm.getPaymentType())) {
            final List<? extends AddressData> deliveryAddresses = getCheckoutFacade().getSupportedDeliveryAddresses(true);
            if (deliveryAddresses.size() == 1) {
                getCheckoutFacade().setDeliveryAddress(deliveryAddresses.get(0));
            }
        }
    }

    @RequestMapping(value = "/next", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    @RequestMapping(value = "/back", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    protected PaymentTypeForm preparePaymentTypeForm(final CartData cartData) {
        final PaymentTypeForm paymentTypeForm = new PaymentTypeForm();

        // set payment type
        if (cartData.getPaymentType() != null && StringUtils.isNotBlank(cartData.getPaymentType().getCode())) {
            paymentTypeForm.setPaymentType(cartData.getPaymentType().getCode());
        } else {
            paymentTypeForm.setPaymentType(CheckoutPaymentType.ACCOUNT.getCode());
        }

        // set cost center
        if (cartData.getCostCenter() != null && StringUtils.isNotBlank(cartData.getCostCenter().getCode())) {
            paymentTypeForm.setCostCenterId(cartData.getCostCenter().getCode());
        } else if (!CollectionUtils.isEmpty(getVisibleActiveCostCenters()) && getVisibleActiveCostCenters().size() == 1) {
            paymentTypeForm.setCostCenterId(getVisibleActiveCostCenters().get(0).getCode());
        }

        // set purchase order number
        paymentTypeForm.setPurchaseOrderNumber(cartData.getPurchaseOrderNumber());
        return paymentTypeForm;
    }

    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(OPF_PAYMENT_TYPE);
    }

}
