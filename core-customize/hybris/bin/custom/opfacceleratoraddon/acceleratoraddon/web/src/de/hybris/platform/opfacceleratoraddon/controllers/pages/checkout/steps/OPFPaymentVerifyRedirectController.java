package de.hybris.platform.opfacceleratoraddon.controllers.pages.checkout.steps;

import com.opf.dto.verify.OPFPaymentVerifyRequestDTO;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.controllers.OpfacceleratoraddonControllerConstants;
import de.hybris.platform.facade.OPFAcceleratorPaymentFacade;
import de.hybris.platform.opf.dto.OPFPaymentAttribute;
import de.hybris.platform.opf.dto.OPFPaymentVerifyResponse;
import de.hybris.platform.opfacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfacceleratoraddon.util.OPFAcceleratorUtil;
import de.hybris.platform.opfservices.client.CCAdapterClientException;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.Map;

import static de.hybris.platform.util.Sanitizer.sanitize;

@Controller
@RequestMapping(value = "/opf/payment-verification-redirect")
public class OPFPaymentVerifyRedirectController extends AbstractCheckoutController {
    private static final Logger LOG = Logger.getLogger(OPFPaymentVerifyRedirectController.class);
    protected static final String MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL = "multiStepCheckoutSummary";
    private static final String REDIRECT_OPF_PAYMENT = "redirect:/checkout/multi/opf-payment/choose";
    @Resource(name = "opfAcceleratorPaymentFacade")
    private OPFAcceleratorPaymentFacade opfAcceleratorPaymentFacade;

    /**
     * Handles GET requests to the /result endpoint. This method processes payment verification results by extracting parameters from the
     * request and adding necessary attributes to the model. The user must be logged in to access this endpoint.
     *
     * @param requestMap
     *         a map of request parameters received from the payment gateway
     * @param model
     *         the model to which attributes are added for rendering the view
     * @return the name of the view to be rendered
     * @throws CMSItemNotFoundException
     *         if the required CMS item is not found
     */
    @GetMapping(value = "/result")
    @RequireHardLogIn
    public String getPaymentVerificationResult(@RequestParam Map<String, String> requestMap, final Model model)
            throws CMSItemNotFoundException {
        model.addAttribute("requestMap", requestMap);
        final ContentPageModel opfVerifyPaymentPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, opfVerifyPaymentPage);
        return OpfacceleratoraddonControllerConstants.Views.Pages.MultiStepCheckout.OpfVerifyPaymentPage;
    }

    @GetMapping(value = "/verify")
    public ResponseEntity<?> redirectPayment(@RequestParam Map<String, String> requestMap) {
        try {
            OPFPaymentVerifyRequestDTO opfPaymentVerifyRequestDTO = new OPFPaymentVerifyRequestDTO();
            opfPaymentVerifyRequestDTO.setResponseMap(requestMap.entrySet().stream().map(entry -> {
                OPFPaymentAttribute opfPaymentAttribute = new OPFPaymentAttribute();
                opfPaymentAttribute.setKey(entry.getKey());
                opfPaymentAttribute.setValue(entry.getValue());
                return opfPaymentAttribute;
            }).toList());
            OPFPaymentVerifyResponse response = opfAcceleratorPaymentFacade.verifyPayment(opfPaymentVerifyRequestDTO);
            return ResponseEntity.ok(response);
        } catch (CCAdapterClientException exception) {
            LOG.error("Error while verifying payment for OPF payments", exception);
            throw new OPFAcceleratorException("Payment verification failed", exception);
        }
    }

    @GetMapping(value = "/cancel")
    public String getPaymentVerificationCancel(final Model model, final RedirectAttributes redirectAttributes) {
        return REDIRECT_OPF_PAYMENT;
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler({ OPFAcceleratorException.class })
    public ErrorListWsDTO handleOpfCheckoutException(final Throwable ex) {
        LOG.error(sanitize(ex.getMessage()), ex);
        return OPFAcceleratorUtil.handleErrorInternal(ex);
    }
}

