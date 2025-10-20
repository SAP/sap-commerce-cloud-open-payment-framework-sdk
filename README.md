[![REUSE status](https://api.reuse.software/badge/github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk)](https://api.reuse.software/info/github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk)
 
# SAP Commerce Cloud, open payment framework SDK
 
## About this project
 
Development of custom addon(s) on the SAP Commerce Accelerator storefront that can be shipped/delivered to customers in a standalone mode so that implementation partners/customers can apply these addons to their existing storefront application with minimal customization/configurations to replace the legacy payment integration approach and use the OPF based payment providers. The addon shall be delivered with the integration capabilities to all OPF APIs that are necessary for the end-end payment flow. Customers need to perform an implementation of the addon on their storefronts which shall mostly be **configuration and testing effort** to replace the existing payment flows via custom developments. A new checkout flow has been introduced for the OPF integration such that A/B testing shall be possible to validate the new OPF integration in terms of payment authorization/capture and order placement.
 
## Requirements and Setup
 
Depending on the accelerator storefront scenario you are using (B2C or B2B), follow the corresponding steps
 
Firstly clone this repository into your projects core-customize folder. Below extensions shall be available
 
    1. opfacceleratoraddon - B2C Addon
    2. opfb2bacceleratoraddon - B2B Addon
    3. opfacceleratorcore - Core extension providing integration with OPF and Commerce Cloud Accelerator Storefront
 
### B2C Accelerator
1. Install addon using below command
    ```bash
    ant addoninstall -Daddonnames="opfacceleratoraddon" -DaddonStorefront.yacceleratorstorefront="<your storefront extension name>"
    ```
    The above command shall modify the ``project.properties`` file of the opfacceleratoraddon extension to include the below property:
   
    ```bash
    <yourcustomstorefront>.additionalWebSpringConfigs.opfacceleratoraddon=classpath\:/opfacceleratoraddon/web/spring/opfb2bacceleratoraddon-web-spring.xml,classpath\:/opfacceleratoraddon/web/spring/multi-step-checkout-config.xml
    ```
 
2. Enable OPF on the basestore by adjusting the store and site id and running the impex ``opfacceleratoraddon/resources/opfacceleratoraddon/impex/opf.impex``
 
3. Create the folder named as security in the config folder , create txt files containing client_id and secret for ccadapter and contains client_id and secret for OPF payment gateway.(Note: the filenames created here should be defined in the **ccAdapter.oauth.client-secret.file** and **opf.oauth.client-secret.file**)
 
4. Add below tenant configs in the hcs_commons since urls are not part of the code
    | Property                 | Value                                           | Mandatory                         |
    | ------------------------ | ----------------------------------------------- | --------------------------------- |
    | `ccAdapter.api.url` | Base URL of your OPF Cloud Connector    | **Yes**                           |
    | `ccAdapter.oauth.client-secret.file`        | ccadapter txt file name from step 3                               | **Yes**                           |
    | `ccAdapter.oauth.token.url`    | Token URL for Cloud Adapter | **Yes** |
    | `opf.base.url`    | OPF tenant Base URL | **Yes** |
    | `opf.oauth.client-secret.file`    | OPF payment gateway txt filename from step3 | **Yes** |
    | `opf.oauth.client-secret.file.location`    | `${HYBRIS_CONFIG_DIR}/security` | **Yes** |
    | `opf.oauth.token.url`    | Token URL for OPF | **Yes** |
 
   
5. For CTA script rendering(for example Karlna) which is optional to be shown on cartPage and PDP page ,add ``opfacceleratoraddon/acceleratoraddon/web/webroot/WEB-INF/tags/responsive/cart/opfCartCTAScript.tag`` file in ``cartPage.jsp`` of the mystorefront extension and add ``opfacceleratoraddon/acceleratoraddon/web/webroot/WEB-INF/tags/responsive/product/opfProductCTAScript.tag`` in ``productDetailsPanel.jsp``
 
6. For CTA scripting on Order confirmation page refer to https://help.sap.com/docs/SAP_COMMERCE_COMPOSABLE_STOREFRONT/962112809f9a48f9b36aa05b208b3731/a5202788df55400eafad3ebe7a5a7895.html?state=DRAFT&locale=en-US#loio6b869b96ce8b4571a697b8ff9940402c
 
7. For QuickBuy(GooglePay) integration add ``opfQuickBuy.tag`` in ``checkoutDisplay.jsp``
 
8. To integrate OPF order process refer to the link https://help.sap.com/docs/OPEN_PAYMENT_FRAMEWORK/3580ff1b17144b8780c055bbb7c2bed3/f77e5d4c4a984d6c8e3cc7882bf79194.html?locale=en-US, depending upon the order integrations used(SAP OMS or Asynchronous order management ), follow the steps provided in the link.
 
9. To integrate OPF return process refer to the link https://help.sap.com/docs/OPEN_PAYMENT_FRAMEWORK/3580ff1b17144b8780c055bbb7c2bed3/cd2a9b34b8d54336be9737c220ca5095.html?locale=en-US , depending upon the order integration used(SAP OMS or Asynchronous order management), follow the specific steps provide in the link.
 
10. Customers have to provide implementation for taxes and shipping taxes prices depending upon the requirements .Refer to below class for the customization
     /custom/opfacceleratorcore/src/de/hybris/platform/populator/OPFPaymentOrderPopulator.java        
 
           1. populateOrderLines()
              line.setLineDiscount(0.0);//Set Line discount
              line.setLineShFeeTax(0.0);//Set line item shipping fee tax
              line.setLineShFeeTaxPercent(0.0);//Set line item shipping fee tax percentage
              line.setLineTaxPercent(0.0);//Set line item tax percentage
              line.setLineTax(0.0);//Set line item tax
   
           2. populateOrderDetails()
              order.setShFeeWithTax(0.0);//Set shipping fee with tax according to custom implementation
              order.setShFeeTax(0.0);//Set shipping fee tax
   
          3. populateShippingMethod()
             shippingMethodData.setShFeeTax(0.0);// Set shipping method shipping fee tax
             shippingMethodData.setShFeeWithTax(0.0);//Set shipping method shipping fee with tax
             shippingMethodData.setShFeeTaxPercent(0.0);//Set shipping method shipping fee tax percentage
 
### B2B Accelerator
 
For the B2B Accelerator storefront, the OPF integration is applicable only on the checkout steps. CTA scripting on product, cart and order confirmation pages are not included as part of this sample integration. You will need to customize these if necessary for your project.
 
1. Install addon using below command
    ```bash
    ant addoninstall -Daddonnames="opfb2bacceleratoraddon" -DaddonStorefront.yacceleratorstorefront="<your storefront extension name>"
    ```
    The above command shall modify the ``project.properties`` file of the opfb2bacceleratoraddon extension to include the below property:
 
    ```bash
    <yourcustomstorefront>.additionalWebSpringConfigs.opfb2bacceleratoraddon=classpath\:/opfb2bacceleratoraddon/web/spring/opfb2bacceleratoraddon-web-spring.xml,classpath\:/opfb2bacceleratoraddon/web/spring/multi-step-checkout-config.xml
    ```
 
2. Enable OPF on the basestore by adjusting the store and site id and running the impex ``opfb2bacceleratoraddon/resources/opfb2bacceleratoraddon/impex/b2bopf.impex``
 
3. Create the folder named as security in the config folder, create txt files containing client_id and secret for ccadapter and contains client_id and secret for OPF payment gateway.(Note: the filenames created here should be defined in the **ccAdapter.oauth.client-secret.file** and **opf.oauth.client-secret.file**)
 
4. Add below tenant configs in the hcs_commons since urls are not part of the code
 
    | Property                 | Value                                           | Mandatory                         |
    | ------------------------ | ----------------------------------------------- | --------------------------------- |
    | `ccAdapter.api.url` | Base URL of your OPF Cloud Connector    | **Yes**                           |
    | `ccAdapter.oauth.client-secret.file`        | ccadapter txt file name from step 3                               | **Yes**                           |
    | `ccAdapter.oauth.token.url`    | Token URL for Cloud Adapter | **Yes** |
    | `opf.base.url`    | OPF tenant Base URL | **Yes** |
    | `opf.oauth.client-secret.file`    | OPF payment gateway txt filename from step3 | **Yes** |
    | `opf.oauth.client-secret.file.location`    | `${HYBRIS_CONFIG_DIR}/security` | **Yes** |
    | `opf.oauth.token.url`    | Token URL for OPF | **Yes** |
 
5. To integrate OPF order process refer to the link https://help.sap.com/docs/OPEN_PAYMENT_FRAMEWORK/3580ff1b17144b8780c055bbb7c2bed3/f77e5d4c4a984d6c8e3cc7882bf79194.html?locale=en-US, depending upon the order integrations used(SAP OMS or Asynchronous order management ), follow the steps provided in the link.
 
6. To integrate OPF return process refer to the link https://help.sap.com/docs/OPEN_PAYMENT_FRAMEWORK/3580ff1b17144b8780c055bbb7c2bed3/cd2a9b34b8d54336be9737c220ca5095.html?locale=en-US, depending upon the order integration used(SAP   OMS or Asynchronous order management), follow the specific steps provide in the link.
 
7. Customers have to provide implementation for taxes and shipping taxes prices depending upon the requirements. Refer to below class for the customization
     ``/custom/opfacceleratorcore/src/de/hybris/platform/populator/OPFPaymentOrderPopulator.java``
 
           1. populateOrderLines()
              line.setLineDiscount(0.0);//Set Line discount
              line.setLineShFeeTax(0.0);//Set line item shipping fee tax
              line.setLineShFeeTaxPercent(0.0);//Set line item shipping fee tax percentage
              line.setLineTaxPercent(0.0);//Set line item tax percentage
              line.setLineTax(0.0);//Set line item tax
   
           2. populateOrderDetails()
              order.setShFeeWithTax(0.0);//Set shipping fee with tax according to custom implementation
              order.setShFeeTax(0.0);//Set shipping fee tax
   
          3. populateShippingMethod()
             shippingMethodData.setShFeeTax(0.0);// Set shipping method shipping fee tax
             shippingMethodData.setShFeeWithTax(0.0);//Set shipping method shipping fee with tax
             shippingMethodData.setShFeeTaxPercent(0.0);//Set shipping method shipping fee tax percentage
 
## Support, Feedback, Contributing
 
This project is open to feature requests/suggestions, bug reports etc. via [GitHub issues](https://github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk/issues). Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](CONTRIBUTING.md).
 
## Security / Disclosure
If you find any bug that may be a security problem, please follow our instructions at [in our security policy](https://github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk/security/policy) on how to report it. Please do not create GitHub issues for security-related doubts or problems.
 
## Code of Conduct
 
We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone. By participating in this project, you agree to abide by its [Code of Conduct](https://github.com/SAP/.github/blob/main/CODE_OF_CONDUCT.md) at all times.
 
## Licensing
 
Copyright 2025 SAP SE or an SAP affiliate company and sap-commerce-cloud-open-payment-framework-sdk contributors. Please see our [LICENSE](LICENSE) for copyright and license information. Detailed information including third-party components and their licensing/copyright information is available [via the REUSE tool](https://api.reuse.software/info/github.com/SAP/sap-commerce-cloud-open-payment-framework-sdk).