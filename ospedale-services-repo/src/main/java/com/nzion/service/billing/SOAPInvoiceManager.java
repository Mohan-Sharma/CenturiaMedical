package com.nzion.service.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.nzion.domain.*;
import com.nzion.domain.billing.*;
import com.nzion.service.ReferralContractService;
import com.nzion.util.*;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.nzion.domain.billing.Contract.CONTRACTTYPE;
import com.nzion.domain.billing.Invoice.INSURANCESTATUS;
import com.nzion.domain.billing.Invoice.ReferralContractStatus;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.PatientVisit;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.domain.emr.soap.DiagnosisSection;
import com.nzion.domain.emr.soap.PatientCpt;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.pms.Product;
import com.nzion.domain.pms.ProductBillOfMaterial;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.dto.CoPayment;
import com.nzion.dto.CoPaymentDetail;
import com.nzion.exception.TransactionException;
import com.nzion.service.PatientService;
import com.nzion.service.SoapNoteService;
import com.nzion.services.product.ProductService;

public class SOAPInvoiceManager extends AbstractInvoiceManager {

    public static final String SOAP_ITEM_TYPE = PatientSoapNote.class.getName();

    public static final String CONSULATION_ITEM_TYPE = "CONSULATION";

    public static final String CPT_ITEM_TYPE = "CPT";

    public static final String CASUALTY_TYPE = "CASUALTY";

    public static final String CONSULATION_ITEM_DESCRIPTION = "Consulation Charge";

    private SoapNoteService soapNoteService;

    private PatientService patientService;

    private BillingService billingService;
    
    private ProductService productService;


    public BillingService getBillingService() {
        return billingService;
    }

    public void setBillingService(BillingService billingService) {
        this.billingService = billingService;
    }
    
    public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}



	private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void setSessionFactory(SessionFactory sessionFactory) {
        SOAPInvoiceManager.sessionFactory = sessionFactory;
    }

    void updateReferralAmountForService(Invoice invoice, InvoiceItem item,
                                        ReferralContract referralContract,
                                        String serviceId, BigDecimal amount) {
        if (referralContract == null) return;
        com.nzion.domain.ReferralContractService referralContractService = commonCrudService.findUniqueByEquality(com.nzion.domain.ReferralContractService.class,
                new String[]{"serviceCode", "referralContract.id"}, new Object[]{new Integer(serviceId), referralContract.getId()});
        if (referralContractService != null) {
            if (referralContract.getPaymentMode().equals(ReferralContract.PAYMENT_MODE_ENUM.PERCENTAGE_SERVICE_ITEM.toString())) {
                BigDecimal percentage = new BigDecimal(referralContractService.getPaymentPercentage());
                BigDecimal referralAmount = amount.multiply(percentage).divide(new BigDecimal(100.0));
                referralAmount = referralAmount.setScale(3, RoundingMode.HALF_UP);
                item.setReferral_amountTobePaid(referralAmount);
                if (invoice.getTotalReferralAmountTobePaid() != null)
                    invoice.setTotalReferralAmountTobePaid(invoice.getTotalReferralAmountTobePaid().add(referralAmount));
                else
                    invoice.setTotalReferralAmountTobePaid(referralAmount);
            } else if (referralContract.getPaymentMode().equals(ReferralContract.PAYMENT_MODE_ENUM.FIX_AMOUNT_PER_SERVICE.toString())) {
                BigDecimal paymentAmount = new BigDecimal(referralContractService.getPaymentAmount());
                item.setReferral_amountTobePaid(paymentAmount);
                if (invoice.getTotalReferralAmountTobePaid() != null)
                    invoice.setTotalReferralAmountTobePaid(invoice.getTotalReferralAmountTobePaid().add(paymentAmount));
                else
                    invoice.setTotalReferralAmountTobePaid(paymentAmount);
            }else if(referralContract.getPaymentMode().equals(ReferralContract.PAYMENT_MODE_ENUM.PERCENTAGE_OF_BILL.toString()) ){
				BigDecimal percentage = new BigDecimal(referralContract.getPercentageOnBill());
				BigDecimal referralAmount = amount.multiply(percentage).divide(new BigDecimal(100.0));
				referralAmount = referralAmount.setScale(3, RoundingMode.HALF_UP);
				item.setReferral_amountTobePaid(referralAmount);
				if (invoice.getTotalReferralAmountTobePaid() != null)
					invoice.setTotalReferralAmountTobePaid(invoice.getTotalReferralAmountTobePaid().add(referralAmount));
				else
					invoice.setTotalReferralAmountTobePaid(referralAmount);
			}
        }
    }

    @Override
    public Invoice generateInvoice(Object object) throws TransactionException {
        ReferralContract referralContract = null;
        BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
        if (billingDisplayConfig == null || billingDisplayConfig.getCurrency() == null)
            throw new RuntimeException("Please Configure The Currency Used for Billing in Preference Section of Admin");

        PatientSoapNote soapNote = (PatientSoapNote) object;
        PatientInsurance patientInsurance = soapNote.getPatientInsurance();

        Invoice invoice = null;
        Schedule schedule1 = soapNote.getSchedule();

        List<Invoice> invoiceList = commonCrudService.findByEquality(Invoice.class, new String[]{"schedule"}, new Object[]{schedule1});
        if ((UtilValidator.isNotEmpty(invoiceList)) && (billingDisplayConfig.getAllowIfSmartServiceToBeBookedFromClinic().equals("yes"))){
            invoice = invoiceList.get(0);
        } else {
            invoice = new Invoice(soapNote.getId().toString(), SOAP_ITEM_TYPE, soapNote.getProvider(), soapNote.getPatient(), soapNote.getSchedule().getLocation());
        }
        if ((soapNote.getSchedule() != null) && (soapNote.getSchedule().getReferral() != null)) {
            invoice.setReferralConsultantId(soapNote.getSchedule().getReferral().getId());
        }

        Schedule schedule = soapNote.getSchedule();
        if(schedule != null){
            invoice.setSchedule(schedule);
            invoice.setReferralDoctorFirstName(schedule.getReferralDoctorFirstName());
            invoice.setReferralDoctorLastName(schedule.getReferralDoctorLastName());
        }

        if ((invoice.getReferralConsultantId() != null) && (invoice.getReferralConsultantId() > 0)) {
        	Referral referral = commonCrudService.getById(Referral.class, invoice.getReferralConsultantId());
            referralContract = commonCrudService.findUniqueByEquality(ReferralContract.class, new String[]{"refereeClinicId"}, new Object[]{ referral.getTenantId() });
            if (referralContract != null) {
                if (!"ACCEPTED".equals(referralContract.getContractStatus())) {
                    referralContract = null;
                }else{
                	invoice.setReferralContract(referralContract);
                }
            }
        }
        invoice.setInvoiceType(InvoiceType.OPD);
        List<Contract> selfContracts = commonCrudService.findByEquality(Contract.class, new String[]{"contractType"}, new Object[]{CONTRACTTYPE.SELF});
        Contract contract = Contract.findCurrentEffectiveContract(selfContracts);
        invoice.setContract(contract);

        Map<String, List<InvoiceItem>> invoiceItemAndServiceMapping = new HashMap<String, List<InvoiceItem>>();

        DiagnosisSection diagnosisSection = (DiagnosisSection) soapNoteService.getSoapSection(soapNote, DiagnosisSection.class);
        if ((diagnosisSection == null || UtilValidator.isEmpty(diagnosisSection.getCpts())) && "patientVisit".equals(billingDisplayConfig.getIsConsultationPriceTriggered()))
            return null;
        if (diagnosisSection != null) {
            for (PatientCpt patientCpt : diagnosisSection.getCpts()) {
                InvoiceItem item = new InvoiceItem(invoice, patientCpt.getCpt().getId(), InvoiceType.OPD_PROCEDURE, patientCpt.getCpt().getDescription(),
                        patientCpt.getUnit(), null, PatientSoapNote.class.getName());
                item.setCpt(patientCpt.getCpt());
                item.setItemOrder(2);
                BigDecimal cptPrice = patientCpt.getCpt().getPrice();
                //TODO
                String patientCategory = "01";
                String tariffCategory = "00";
                if ("CASH PAYING".equals(soapNote.getPatient().getPatientType())) {
                    tariffCategory = soapNote.getPatient().getTariffCode();
                } else if ("INSURANCE".equals(soapNote.getPatient().getPatientType())) {
                    patientCategory = "02";
                    
                    String insuranceName = patientInsurance.getInsuranceName();
                    String groupId = UtilValidator.isNotEmpty(patientInsurance.getGroupId()) ? patientInsurance.getGroupId() : new String() ;
                    String healthPolicyId = UtilValidator.isNotEmpty(patientInsurance.getHealthPolicyId()) ? patientInsurance.getHealthPolicyId() : new String();
                    
                    List<Map<String, Object>> tariffCategorys = patientService.getTariffCategoryByPatientCategory("INSURANCE");
                    for (Map<String, Object> map : tariffCategorys) {
                    	String tariff = map.get("tariff") == null ? new String() : ((String) map.get("tariff"));
                    	String group = map.get("groupId") == null ? new String() : ((String) map.get("groupId"));
                    	String healthPolicy = map.get("healthPolicyId") == null ? new String() : ((String) map.get("healthPolicyId"));
                    	if(tariff.equals(insuranceName) && UtilValidator.isEmpty(healthPolicy) && UtilValidator.isEmpty(group) ) {
                            tariffCategory = map.get("tariffCode").toString();
                        }
                    	if( tariff.equals(insuranceName) && groupId.equals(group) && healthPolicyId.equals(healthPolicy) ){
                    		tariffCategory = map.get("tariffCode").toString();
                    		break;
                    	}else if(tariff.equals(insuranceName) && healthPolicyId.equals(healthPolicy) && UtilValidator.isEmpty(groupId) ){
                    		tariffCategory = map.get("tariffCode").toString();
                    		break;
                    	}else if(tariff.equals(insuranceName) && healthPolicyId.equals(healthPolicy) && !groupId.equals(group) ){
                    		if(UtilValidator.isEmpty(group)){
	                    		tariffCategory = map.get("tariffCode").toString();
	                    		break;
                    		}
                    	}
                    }
                } else if ("CORPORATE".equals(soapNote.getPatient().getPatientType())) {
                    patientCategory = "03";
                    tariffCategory = soapNote.getPatient().getPatientCorporate().getTariffCategoryId();
                }

                Map<String, Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", patientCpt.getCpt().getId(), null, null, tariffCategory, patientCategory, new Date());
                if(masterPrice == null){
                    masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", patientCpt.getCpt().getId(), null, null, "00", "01", new Date());
                    if (masterPrice == null){
                        if (billingService.checkThruDate("01", "01", "001", patientCpt.getCpt().getId(), null, null, "00", "01", new Date())){
                            throw new RuntimeException("You have selected a procedure for which there is no active price definded.");
                        }
                    }
                }
                if (masterPrice == null) {
                    throw new RuntimeException("Procedure price not configured");
                }

                if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null)
                    cptPrice = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
                if(cptPrice == null){
                    cptPrice = BigDecimal.ZERO;
                }
                Money price = new Money(cptPrice.multiply(BigDecimal.valueOf(patientCpt.getUnit())), convertTo());
                item.init(cptPrice, billingDisplayConfig.getCurrency().getCode(), price, price, 2);
                boolean isMinMaxPriceAvailable = false;
                if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT_MIN") != null) {
                    item.setBillableAmountMin(((BigDecimal) masterPrice.get("BILLABLE_AMOUNT_MIN")).multiply(BigDecimal.valueOf(patientCpt.getUnit())));
                    isMinMaxPriceAvailable = true;
                }
                if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT_MAX") != null) {
                    item.setBillableAmountMax(((BigDecimal) masterPrice.get("BILLABLE_AMOUNT_MAX")).multiply(BigDecimal.valueOf(patientCpt.getUnit())));
                    isMinMaxPriceAvailable = true;
                }
                item.setMinMaxPriceAvailable(isMinMaxPriceAvailable);

                if (masterPrice != null && "AMOUNT".equals(masterPrice.get("COPAY_TYPE"))) {
                    item.setCopayAmount((BigDecimal) masterPrice.get("COPAY"));
                    item.setComputeType("GROSS");
                    item.setAuthorization(true);
                }

                if (masterPrice != null && "PERCENT".equals(masterPrice.get("COPAY_TYPE"))) {
                    item.setCopayPercentage((BigDecimal) masterPrice.get("COPAY"));
                    item.setComputeType("NET");
                    item.setAuthorization(true);
                }


                String serviceId = billingService.getServiceIdFromMasterPriceConf(patientCpt.getCpt().getId(), null, null, tariffCategory, patientCategory);
                if (UtilValidator.isNotEmpty(serviceId)) {
                    List invItemList = (List) invoiceItemAndServiceMapping.get(serviceId);
                    if (invItemList == null) {
                        invItemList = new ArrayList();
                    }
                    invItemList.add(item);
                    invoiceItemAndServiceMapping.put(serviceId, invItemList);
                }
                if ("CASH PAYING".equals(soapNote.getPatient().getPatientType()))
                    item.setCopayAmount(item.getGrossAmount());

                if (UtilValidator.isNotEmpty( patientCpt.getCpt().getId() )) {
                    updateReferralAmountForService(invoice, item, referralContract, patientCpt.getCpt().getId(), cptPrice.multiply(item.getQuantity()));
                }
                invoice.addInvoiceItem(item);

                invoice.setInvoiceStatus(InvoiceStatusItem.INPROCESS.toString());

                Cpt newCpt = commonCrudService.getById(Cpt.class, patientCpt.getCpt().getId());
                Set<ProductBillOfMaterial> productBillOfMaterials = newCpt.getProductBillOfMaterials();
                
                
                for(ProductBillOfMaterial productBillOfMaterial : productBillOfMaterials){
                	if(productService == null)
                		productService = Infrastructure.getSpringBean("productService");
                	productService.reduceProductInventory(productBillOfMaterial.getProduct(), productBillOfMaterial.getQuantity().multiply(item.getQuantity()) );
                }

                if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
                    invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(cptPrice.multiply(item.getQuantity())), convertTo()));
                else
                    invoice.setTotalAmount(new com.nzion.domain.product.common.Money(cptPrice.multiply(item.getQuantity()), convertTo()));
            }
        }

        /**
         *  Check if this is the first Invoice for the Patient. If Yes, then add to this Invoice the registration
         *  Amount as a separate line item in the invoice. The Price has to come from Billing Preference.
         *  Invoice Item Type would be Registration Charges.
         *
         */
        boolean mobileOrPatinetPortal = soapNote.getSchedule() == null ? false : soapNote.getSchedule().isMobileOrPatinetPortal();
        invoice.setMobileOrPatinetPortal(mobileOrPatinetPortal);

        boolean isConsultationInvoiceGenerated = soapNote.getSchedule() == null ? false : soapNote.getSchedule().isConsultationInvoiceGenerated();
        if ( "general".equals(billingDisplayConfig.getIsConsultationPriceTriggered()) && !isConsultationInvoiceGenerated ) {
            List<Invoice> invoices = billingService.getFirstInvoice(soapNote.getPatient());
            if (UtilValidator.isEmpty(invoices)) {
                InvoiceItem invItem = new InvoiceItem(invoice, soapNote.getId().toString(), InvoiceType.OPD_REGISTRATION, InvoiceType.OPD_REGISTRATION.getDescription(), 1, null,
                        PatientSoapNote.class.getName());
                if (billingDisplayConfig.getRegistrationFee() != null) {
                    invItem.init(billingDisplayConfig.getRegistrationFee(), billingDisplayConfig.getCurrency().getCode(), new Money(billingDisplayConfig.getRegistrationFee(), convertTo()),
                            new Money(billingDisplayConfig.getRegistrationFee(), convertTo()), 0);
                    invItem.setCopayAmount(invItem.getGrossAmount());
                    invoice.addInvoiceItem(invItem);
                    if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
                        invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(billingDisplayConfig.getRegistrationFee()), convertTo()));
                    else
                        invoice.setTotalAmount(new com.nzion.domain.product.common.Money(billingDisplayConfig.getRegistrationFee(), convertTo()));
                }

            }
            String invoiceItemDescription = InvoiceType.OPD_CONSULTATION.getDescription() + " - " +soapNote.getSchedule().getVisitType().getName();
            
            if(invoiceItemDescription.contains("Premium")){
            	invoiceItemDescription = "Premium Consultation Afya Smart Service";
            }else if(invoiceItemDescription.contains("Tele")){
            	invoiceItemDescription = "Tele Consultation Afya Smart Service";
            }else if(invoiceItemDescription.contains("Home")){
            	invoiceItemDescription = "Home Visit Afya Smart Service";
            }else if(invoiceItemDescription.contains("Consultation") && schedule.isFromMobileApp())
            	invoiceItemDescription = "Consultation Afya Smart Service";
            
            
            InvoiceItem consultationItem = new InvoiceItem(invoice, soapNote.getId().toString(), InvoiceType.OPD_CONSULTATION, invoiceItemDescription, 1, null
                    , PatientSoapNote.class.getName());
            BigDecimal amount = BigDecimal.ZERO;
            consultationItem.setProvider(soapNote.getProvider());
            consultationItem.setItemOrder(1);
            String patientCategory = "01";
            String tariffCategory = "00";
            if ("CASH PAYING".equals(soapNote.getPatient().getPatientType())) {
                tariffCategory = soapNote.getPatient().getTariffCode();
            } else if ("INSURANCE".equals(soapNote.getPatient().getPatientType())) {
                patientCategory = "02";
                
                String insuranceName = patientInsurance.getInsuranceName();
                String groupId = UtilValidator.isNotEmpty(patientInsurance.getGroupId()) ? patientInsurance.getGroupId() : new String() ;
                String healthPolicyId = UtilValidator.isNotEmpty(patientInsurance.getHealthPolicyId()) ? patientInsurance.getHealthPolicyId() : new String();
                
                List<Map<String, Object>> tariffCategorys = patientService.getTariffCategoryByPatientCategory("INSURANCE");
                for (Map<String, Object> map : tariffCategorys) {
                	String tariff = map.get("tariff") == null ? new String() : ((String) map.get("tariff"));
                	String group = map.get("groupId") == null ? new String() : ((String) map.get("groupId"));
                	String healthPolicy = map.get("healthPolicyId") == null ? new String() : ((String) map.get("healthPolicyId"));
                	if(tariff.equals(insuranceName) && UtilValidator.isEmpty(healthPolicy) && UtilValidator.isEmpty(group) ) {
                        tariffCategory = map.get("tariffCode").toString();
                    }
                	if( tariff.equals(insuranceName) && groupId.equals(group) && healthPolicyId.equals(healthPolicy) ){
                		tariffCategory = map.get("tariffCode").toString();
                		break;
                	}else if(tariff.equals(insuranceName) && healthPolicyId.equals(healthPolicy) && UtilValidator.isEmpty(groupId) ){
                		tariffCategory = map.get("tariffCode").toString();
                		break;
                	}else if(tariff.equals(insuranceName) && healthPolicyId.equals(healthPolicy) && !groupId.equals(group) ){
                		if(UtilValidator.isEmpty(group)){
                    		tariffCategory = map.get("tariffCode").toString();
                    		break;
                		}
                	}

                }
            } else if ("CORPORATE".equals(soapNote.getPatient().getPatientType())) {
                patientCategory = "03";
                tariffCategory = soapNote.getPatient().getPatientCorporate().getTariffCategoryId();
            }

            SlotType slotType = soapNote.getSchedule().getVisitType();

            Map<String, Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, slotType.getId().toString(), soapNote.getProvider().getId().toString(), tariffCategory, patientCategory, new Date());
            if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null)
                amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
            
            VisitTypeSoapModule visitTypeSoapModule = commonCrudService.findUniqueByEquality(VisitTypeSoapModule.class,new String[]{"provider","slotType"},
            		new Object[]{soapNote.getProvider(), slotType });
            if(visitTypeSoapModule.isVisitPolicy() && !visitTypeSoapModule.isSmartService())
            	amount = updateFollowUpCharges(soapNote.getPatient(),amount, soapNote.getProvider(), slotType.getName(), tariffCategory, patientCategory );

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10005", soapNote.getProvider().getId().toString(), "00", "01", new Date());
                if(masterPrice == null){
                    masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10005", soapNote.getProvider().getId().toString(), "00", "01", new Date());
                }
                if(masterPrice != null)
                    amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
            }

            consultationItem.init(amount, billingDisplayConfig.getCurrency().getCode(), new Money(amount, convertTo()), new Money(amount, convertTo()), 1);

            boolean isMinMaxPriceAvailable = false;

            if (masterPrice == null) {
                if (billingService.checkThruDate("01", "01", "001", null, "10005", soapNote.getProvider().getId().toString(), "00", "01", new Date())) {
                    throw new RuntimeException("You have selected a consultation for which there is no active price definded.");
                }
            }
            if (masterPrice == null) {
                throw new RuntimeException("Consultation price not configured");
            }
            if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT_MIN") != null) {
                consultationItem.setBillableAmountMin(((BigDecimal) masterPrice.get("BILLABLE_AMOUNT_MIN")));
                isMinMaxPriceAvailable = true;
            }
            if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT_MAX") != null) {
                consultationItem.setBillableAmountMax(((BigDecimal) masterPrice.get("BILLABLE_AMOUNT_MAX")));
                isMinMaxPriceAvailable = true;
            }

            consultationItem.setMinMaxPriceAvailable(isMinMaxPriceAvailable);


            if (masterPrice != null && "AMOUNT".equals(masterPrice.get("COPAY_TYPE"))) {
                consultationItem.setCopayAmount((BigDecimal) masterPrice.get("COPAY"));
                consultationItem.setComputeType("GROSS");
                consultationItem.setAuthorization(true);
            }

            if (masterPrice != null && "PERCENT".equals(masterPrice.get("COPAY_TYPE"))) {
                consultationItem.setCopayPercentage((BigDecimal) masterPrice.get("COPAY"));
                consultationItem.setComputeType("NET");
                consultationItem.setAuthorization(true);
            }

            String serviceId = billingService.getServiceIdFromMasterPriceConf(null, "10005", soapNote.getProvider().getId().toString(), tariffCategory, patientCategory);
            if (UtilValidator.isNotEmpty(serviceId)) {
                List invItemList = (List) invoiceItemAndServiceMapping.get(serviceId);
                if (invItemList == null) {
                    invItemList = new ArrayList();
                }
                invItemList.add(consultationItem);
                invoiceItemAndServiceMapping.put(serviceId, invItemList);
            }

            if ("CASH PAYING".equals(soapNote.getPatient().getPatientType()))
                consultationItem.setCopayAmount(consultationItem.getGrossAmount());
            if (UtilValidator.isNotEmpty(slotType.getId())) {
                updateReferralAmountForService(invoice, consultationItem, referralContract, slotType.getId().toString(), amount);
            }
            invoice.addInvoiceItem(consultationItem);
            if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
                invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(amount), convertTo()));
            else
                invoice.setTotalAmount(new com.nzion.domain.product.common.Money(amount, convertTo()));

        }

        invoice.setInsuranceStatus(null);

        if ("INSURANCE".equals(soapNote.getPatient().getPatientType())) {
            if (UtilValidator.isNotEmpty(soapNote.getPatient().getPatientInsurances())) {
                invoice.setSelectedHisModuleId(soapNote.getSelectedHisModuleId());
                invoice.setPatientInsuranceId(patientInsurance.getId());
                CoPayment coPayment = AfyaServiceConsumer.getServiceOrModuleDataByServiceId(soapNote.getSelectedHisModuleId(), patientInsurance.getBenefitId(),patientInsurance.getGroupId(), invoiceItemAndServiceMapping.keySet());
                CoPaymentDetail moduleDetail = coPayment.getModuleDetails();
                Map<String, CoPaymentDetail> serviceToCopaymentDetailMapping = new HashMap();

                for (CoPaymentDetail detail : coPayment.getServiceDetails()) {
                    serviceToCopaymentDetailMapping.put(detail.getServiceId(), detail);
                }

                if (serviceToCopaymentDetailMapping != null) {
                    Iterator<String> serviceIter = serviceToCopaymentDetailMapping.keySet().iterator();
                    while (serviceIter.hasNext()) {
                        String serviceId = serviceIter.next();
                        CoPaymentDetail copaymentDetail = serviceToCopaymentDetailMapping.get(serviceId);
                        List<InvoiceItem> invoiceItems = (List) invoiceItemAndServiceMapping.get(serviceId);
                        Iterator<InvoiceItem> invoiceItemListIter = invoiceItems.iterator();
                        boolean copayApplied = false;
                        boolean deductibleApplied = false;
                        while (invoiceItemListIter.hasNext()) {
                            InvoiceItem invoiceItem = invoiceItemListIter.next();
                            invoiceItem.setCopayAmount(BigDecimal.ZERO);
                            invoiceItem.setActualCopayAmount(BigDecimal.ZERO);
                            invoiceItem.setDeductablePercentage(BigDecimal.ZERO);
                            invoiceItem.setDeductableAmount(BigDecimal.ZERO);
                            invoiceItem.setCopayPercentage(BigDecimal.ZERO);
                            invoiceItem.setActualCopayPercentage(BigDecimal.ZERO);
                            invoiceItem.setServiceId(serviceId);

                            //Override Copay Amount
                            if (!copayApplied) {
                                if (copaymentDetail.getCopayAmount().compareTo(BigDecimal.ZERO) == 0 && coPayment.getTotalCopayAmount().compareTo(BigDecimal.ZERO) == 1) {

                                	invoiceItem.setCopayAmount(coPayment.getTotalCopayAmount());
                                    invoiceItem.setActualCopayAmount(coPayment.getTotalCopayAmount());
                                    coPayment.setTotalCopayAmount(BigDecimal.ZERO);
                                    
                                } else {
                                	
                                	BigDecimal totalCopayAmount = copaymentDetail.getCopayAmount();
                                    invoiceItem.setCopayAmount(totalCopayAmount);
                                    invoiceItem.setActualCopayAmount(totalCopayAmount);
                                }
                                System.out.println(" Copay Amount Applied " + invoiceItem.getCopayAmount());
                                copayApplied = true;
                            }
                            
                            BigDecimal maxAmount = copaymentDetail.getMaxAmount();
                        	invoiceItem.setMaxAmount(maxAmount);
                        	
                            invoiceItem.setCopayPercentage(copaymentDetail.getCopayPercentage());
                            invoiceItem.setActualCopayPercentage(copaymentDetail.getCopayPercentage());
                            invoiceItem.setDeductablePercentage(copaymentDetail.getDeductablePercentage());
                            if (!deductibleApplied) {
                                if (copaymentDetail.getDeductableAmount().compareTo(BigDecimal.ZERO) == 0 && coPayment.getTotalDeductableAmount().compareTo(BigDecimal.ZERO) == 1) {
                                    invoiceItem.setDeductableAmount(coPayment.getTotalDeductableAmount());
                                    coPayment.setTotalDeductableAmount(BigDecimal.ZERO);
                                } else
                                    invoiceItem.setDeductableAmount(copaymentDetail.getDeductableAmount());
                                deductibleApplied = true;
                                System.out.println(" Deductible Amount Applied " + invoiceItem.getDeductableAmount());
                            }
                            if (copaymentDetail.getComputeType() != null)
                                invoiceItem.setComputeType(copaymentDetail.getComputeType());
                            else
                                invoiceItem.setComputeType(moduleDetail != null ? moduleDetail.getComputeType() : "");

                            invoiceItem.setAuthorization(copaymentDetail.getAuthorization());
                            invoiceItem.setPreauthorized(copaymentDetail.getAuthorization());
                            if(copaymentDetail.getAuthorization() && !INSURANCESTATUS.PENDING_APPROVAL.equals(invoice.getInsuranceStatus())){
                                invoice.setInsuranceStatus(INSURANCESTATUS.PRE_APPROVED);
                            }else{
                                invoice.setInsuranceStatus(INSURANCESTATUS.PENDING_APPROVAL);
                            }
                        }
                    }
                }

            }

        }
        if ((invoice.getReferralConsultantId() != null) && (invoice.getReferralConsultantId() > 0)) {
            if ((referralContract != null) &&
                    (ReferralContract.PAYMENT_MODE_ENUM.PERCENTAGE_OF_BILL.toString().equals(referralContract.getPaymentMode()))) {
                BigDecimal percentage = new BigDecimal(referralContract.getPercentageOnBill());
                BigDecimal referralAmount = invoice.getTotalAmount().getAmount().multiply(percentage).divide(new BigDecimal(100.0));
                invoice.setTotalReferralAmountTobePaid(referralAmount.setScale(3, RoundingMode.HALF_UP));
                if (ReferralContract.PAYPOINT_ENUM.ON_BILL.toString().equals(referralContract.getPaypoint())) {
                    invoice.setTotalReferralAmountPayable(referralAmount.setScale(3, RoundingMode.HALF_UP));
                    invoice.setReferralContractStatus(ReferralContractStatus.INPROCESS);
                }
            }
        }
        
        if(invoice.getInvoiceItems() != null && invoice.getInvoiceItems().size() > 0){
	        commonCrudService.save(invoice);
	        try {
	
	            billingService.doTransactions(soapNote, invoice);
	        } catch (TransactionException e) {
	            e.printStackTrace();
	        }
        }
        return invoice;
    }

    private BigDecimal updateFollowUpCharges(Patient patient, BigDecimal amount, Provider provider, String visitName,
                                             String tariffCategory, String patientCategory) {
        Integer followUpVisitDays = null;
        Integer followUpVisits = null;
        BigDecimal followUpVisitCharges = null;
        Map<String, Object> masterPrice = null;
        List<Invoice> invoices = commonCrudService.findByEquality(Invoice.class, new String[]{"patient"}, new Object[]{patient});
        //Revisit
        followUpVisitDays = provider.getRevisitDays();
        followUpVisits = provider.getRevisitVisits();
        followUpVisitCharges = provider.getRevisitCharges();
        masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10024", provider.getId().toString(), tariffCategory, patientCategory, new Date());

        if (invoices != null && invoices.size() != 0 && invoices.size() <= followUpVisits) {
            if (invoices.size() <= followUpVisitDays){
                if(masterPrice != null){
                    amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
                }else{
                    amount = followUpVisitCharges;
                }
            }
        }

        //Followup Visit
        masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10010", provider.getId().toString(), tariffCategory, patientCategory, new Date());
        followUpVisitDays = provider.getFollowUpVisitDays();
        followUpVisits = provider.getFollowUpVisits();
        followUpVisitCharges = provider.getFollowUpVisitCharges();

        if (invoices != null && invoices.size() != 0 && invoices.size() <= followUpVisits) {
            if (invoices.size() <= followUpVisitDays){
                if(masterPrice != null){
                    amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
                }else{
                    amount = followUpVisitCharges;
                }
            }
        }

      //Free Visit
        followUpVisitDays = provider.getFreeVisitDays();
        followUpVisits = provider.getFreeVisits();
        followUpVisitCharges = provider.getFreeVisitCharges();
        Invoice lastInvoice = UtilValidator.isNotEmpty(invoices) ? invoices.get(invoices.size() - 1) : null;
        
        if(lastInvoice != null && lastInvoice.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) > 0 
        		&& followUpVisitDays > 0 && followUpVisits > 0){
	       amount = followUpVisitCharges;
        }else if(lastInvoice != null && lastInvoice.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) == 0){
        	
        	Integer count = 1;
        	Invoice lasInv = null;
        	for(int i= invoices.size() - 1; i <= invoices.size(); i--){
         	   Invoice inv = invoices.get(i);
         	   if( inv.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) == 0 ){
         		  lasInv = inv;
         		  count ++;
         	   }else{
         		  break;
         	   }
            }
        	if(count <= followUpVisits){
        		amount = followUpVisitCharges;
        	}
        	
        }


        return amount;
    }


    @Override
    public List<String> getItemTypeOrder() {
        return Arrays.asList(InvoiceType.OPD_CONSULTATION.name(), InvoiceType.OPD_PROCEDURE.name(), InvoiceType.SERVICE_TAX.name(),
                InvoiceType.CASUALTY.name());
    }

    public SoapNoteService getSoapNoteService() {
        return soapNoteService;

    }

    @Resource
    @Required
    public void setSoapNoteService(SoapNoteService soapNoteService) {
        this.soapNoteService = soapNoteService;
    }

    public PatientService getPatientService() {
        return patientService;
    }

    @Resource
    @Required
    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    @Override
    public Map<String, Object> viewInvoiceFor(Object object) {
        return null;
    }

    public Currency convertTo() {
        BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
        String currency = billingDisplayConfig.getCurrency().getCode();
        Currency defaultCurrency = Currency.getInstance(currency);
        return defaultCurrency;
    }
}