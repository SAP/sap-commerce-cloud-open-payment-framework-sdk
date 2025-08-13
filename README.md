[![REUSE status](https://api.reuse.software/badge/github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk)](https://api.reuse.software/info/github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk)

# SAP Commerce Cloud, open payment framework SDK

## About this project

Development of custom addon(s) on the SAP Commerce Accelerator storefront that can be shipped/delivered to customers in a standalone mode so that implementation partners/customers can apply these addons to their existing storefront application with minimal customization/configurations to replace the legacy payment integration approach and use the OPF based payment providers. The addon shall be delivered with the integration capabilities to all OPF APIs that are necessary for the end-end payment flow. Customers need to perform an implementation of the addon on their storefronts which shall mostly be configuration and testing effort* to replace the existing payment flows via custom developments. A new checkout flow shall be introduced for the OPF integration such that A/B testing shall be possible to validate the new OPF integration in terms of payment authorization/capture and order placement.

## Requirements and Setup
1. Install addon using below command
    ant addoninstall -Daddonnames="opfacceleratoraddon" -DaddonStorefront.yacceleratorstorefront="mystorestorefront"
2. Enable OPF on the basestore by running the impex opfacceleratorcore/resources/opfacceleratorcore/impex/opf.impex
3. Create the folder named as security in the config folder , create files named ccadapter-security.txt(contains client_id and secret for ccadapter ) and opf-acc-sdk-security.txt(contains client_id and secret for OPF payment gateway) in the security folder created .
4. Add below tenant configs in the hcs_commons since urls are not part of the code
     ccAdapter.api.url=
     ccAdapter.oauth.client-secret.file=ccadapter-security.txt
     ccAdapter.oauth.token.url=
     opf.base.url=
     opf.oauth.client-secret.file=opf-acc-sdk-security.txt
     opf.oauth.client-secret.file.location=${HYBRIS_CONFIG_DIR}/security
     opf.oauth.token.url=
5. For CTA script rendering , add opfacceleratoraddon/acceleratoraddon/web/webroot/WEB-INF/tags/responsive/cart/opfCartCTAScript.tag file in cartPage.jsp of the mystorefront extension and add    opfacceleratoraddon/acceleratoraddon/web/webroot/WEB-INF/tags/responsive/product/opfProductCTAScript.tag in checkoutDisplay.jsp
6. To integrate OPF order process refer to the link https://help.sap.com/docs/OPEN_PAYMENT_FRAMEWORK/3580ff1b17144b8780c055bbb7c2bed3/f77e5d4c4a984d6c8e3cc7882bf79194.html?locale=en-US,depending upon the order       integrations used(SAP OMS or Asynchronous order management ), follow the steps provided in the link.
7. To integrate OPF return process refer to the link https://help.sap.com/docs/OPEN_PAYMENT_FRAMEWORK/3580ff1b17144b8780c055bbb7c2bed3/cd2a9b34b8d54336be9737c220ca5095.html?locale=en-US , depending upon the order integration used(SAP   OMS or Asynchronous order management), follow the specific steps provide in the link.
## Support, Feedback, Contributing

This project is open to feature requests/suggestions, bug reports etc. via [GitHub issues](https://github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk/issues). Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](CONTRIBUTING.md).

## Security / Disclosure
If you find any bug that may be a security problem, please follow our instructions at [in our security policy](https://github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk/security/policy) on how to report it. Please do not create GitHub issues for security-related doubts or problems.

## Code of Conduct

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone. By participating in this project, you agree to abide by its [Code of Conduct](https://github.com/SAP/.github/blob/main/CODE_OF_CONDUCT.md) at all times.

## Licensing

Copyright 2025 SAP SE or an SAP affiliate company and sap-commerce-cloud-open-payment-framework-sdk contributors. Please see our [LICENSE](LICENSE) for copyright and license information. Detailed information including third-party components and their licensing/copyright information is available [via the REUSE tool](https://api.reuse.software/info/github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk).
