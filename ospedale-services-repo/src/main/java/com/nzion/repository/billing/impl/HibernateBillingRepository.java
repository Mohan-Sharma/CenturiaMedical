package com.nzion.repository.billing.impl;

import com.nzion.domain.*;
import com.nzion.domain.billing.*;
import com.nzion.domain.billing.Contract.CONTRACTTYPE;
import com.nzion.domain.billing.Invoice.INSURANCESTATUS;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.lab.LabOrderRequest;
import com.nzion.domain.emr.lab.LabTestPanel;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.pms.InsuranceProvider;
import com.nzion.domain.pms.Product;
import com.nzion.report.search.view.BillingSearchVO;
import com.nzion.repository.billing.BillingRepository;
import com.nzion.repository.impl.HibernateBaseRepository;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("unchecked")
public class HibernateBillingRepository extends HibernateBaseRepository implements BillingRepository {

    @Override
    public List<CptPrice> getCptPricesFor(Collection<Cpt> cpts) {
        Criteria criteria = getSession().createCriteria(CptPrice.class);
        if (UtilValidator.isNotEmpty(cpts)) criteria.add(Restrictions.in("cpt", cpts));
        return criteria.list();
    }

    @Override
    public List<CptPrice> getContractCptPricesFor(Contract contract, Collection<Cpt> cpts) {
        Criteria criteria = getSession().createCriteria(CptPrice.class);
        if (UtilValidator.isNotEmpty(cpts)) criteria.add(Restrictions.in("cpt", cpts));
        if(contract != null)
            criteria.add(Restrictions.eq("contract", contract));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public List<Invoice> getBillingTransactionFor(PatientSoapNote soapNote) {
        Criteria criteria = getSession().createCriteria(Invoice.class);
        criteria.add(Restrictions.eq("itemId", String.valueOf(soapNote.getId())));
        criteria.add(Restrictions.eq("itemType", PatientSoapNote.class.getName()));
        return criteria.list();
    }

    @Override
    public List<InvoiceItem> getTransactionItemsFor(Invoice billingTransaction) {
        Criteria criteria = getSession().createCriteria(InvoiceItem.class);
        criteria.add(Restrictions.eq("txn", billingTransaction));
        return criteria.list();
    }

    @Override
    public List<Consultation> getConsultationBySpeciality(Speciality speciality) {
        Criteria criteria = getSession().createCriteria(Consultation.class);
        criteria.add(Restrictions.eq("speciality", speciality));
        return criteria.list();
    }

    @Override
    public List<Consultation> getConsultationByProvider(Person provider) {
        Criteria criteria = getSession().createCriteria(Consultation.class);
        criteria.add(Restrictions.eq("person", provider));
        return criteria.list();
    }

    @Override
    public List<Consultation> getConsultationChargeFor(Speciality speciality,SoapNoteType soapNoteType) {
        Criteria criteria = getSession().createCriteria(Consultation.class);
        criteria.add(Restrictions.eq("speciality", speciality));
        if(soapNoteType!=null)
            criteria.add(Restrictions.eq("soapNoteType", soapNoteType));
        return criteria.list();
    }

    @Override
    public List<Consultation> getConsultationChargeFor(Employee employee,SoapNoteType soapNoteType) {
        Criteria criteria = getSession().createCriteria(Consultation.class);
        criteria.add(Restrictions.eq("person", employee));
        if(soapNoteType!=null)
            criteria.add(Restrictions.eq("soapNoteType", soapNoteType));
        return criteria.list();
    }

    @Override
    public List<Invoice> getInvoice(List<InvoiceStatusItem> status,Patient patient,Employee emp,Date fromDate,Date thruDate,String ipNumber) {
        Criteria criteria = getSession().createCriteria(Invoice.class);
        criteria.add(Restrictions.isNotNull("patient"));
        if (UtilValidator.isNotEmpty(status))
            criteria.add(Restrictions.in("invoiceStatus", status));
        if(patient!=null)
            criteria.add(Restrictions.eq("patient", patient));
        if(emp!=null)
            criteria.add(Restrictions.eq("consultant", emp));
        if(UtilValidator.isNotEmpty(ipNumber))
            criteria.add(Restrictions.eq("ipNumber", ipNumber));
        if(fromDate != null) criteria.add(Restrictions.ge("invoiceDate",UtilDateTime.getDayStart(fromDate)));
        if(thruDate != null) criteria.add(Restrictions.le("invoiceDate",UtilDateTime.getDayEnd(thruDate)));
        criteria.addOrder(Order.desc("id"));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        //criteria.addOrder(Order.desc("createdTxTimestamp"));
        if(patient==null && emp==null && UtilValidator.isEmpty(ipNumber) && fromDate == null && thruDate == null)
            criteria.setMaxResults(25);
        return criteria.list();
    }
    @Override
    public List<LabOrderRequest> getSearchByLabOrder(List<LabOrderRequest.ORDERSTATUS> status, Patient patient, Provider provider, Referral referral) {
        Criteria criteria = getSession().createCriteria(LabOrderRequest.class);
        if (UtilValidator.isNotEmpty(status))
            criteria.add(Restrictions.in("orderStatus", status));
        if(patient!=null)
            criteria.add(Restrictions.eq("patient", patient));
        if(provider!=null)
            criteria.add(Restrictions.eq("provider", provider));
        if(referral!=null)
            criteria.add(Restrictions.eq("referral", referral));
        criteria.addOrder(Order.desc("createdTxTimestamp"));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public Contract findEffectiveContractFor(String name, CONTRACTTYPE type, Date fromDate, Date thruDate) {
        return null;
    }

    @Override
    public List<Contract> getContractForInsPro(InsuranceProvider insuPro) {
        Criteria criteria = getSession().createCriteria(Contract.class);
        if(insuPro != null){
            criteria.createCriteria("insuranceProviders").add(Restrictions.eq("id", insuPro.getId()));
        }
        return criteria.list();
    }

    @Override
    public List<InsuranceProvider> getInsuranceProviderAttachedToContract() {
        Criteria criteria = getSession().createCriteria(InsuranceProvider.class);
        criteria.add(Restrictions.isNotNull("contract"));
        return criteria.list();
    }

    @Override
    public List<Invoice> searchInvoiceBy(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate, List patientInsuranceIds) {
        Criteria criteria = getSession().createCriteria(Invoice.class);
        Criteria patientCriteria = criteria.createCriteria("patient");
        if(patientInsuranceIds !=null && patientInsuranceIds.size()< 1)
        	return Collections.EMPTY_LIST;
        		
        
        if(billingSearchVO.getPatient() != null)
            criteria.add(Restrictions.eq("patient",billingSearchVO.getPatient()));
	
	
	/*if(billingSearchVO.getPaymentMethod() != null){
		Criteria invPaymentCriteria = criteria.createCriteria("invoicePayments");
		invPaymentCriteria.add(Restrictions.eq("paymentMethod", billingSearchVO.getPaymentMethod()));
	}*/
        if(UtilValidator.isNotEmpty(billingSearchVO.getPatientType())) {
            
            patientCriteria.add(Restrictions.like("patientType", billingSearchVO.getPatientType(), MatchMode.ANYWHERE));
        }
        if(UtilValidator.isNotEmpty(billingSearchVO.getIpNumber()))
            criteria.add(Restrictions.like("ipNumber", billingSearchVO.getIpNumber(), MatchMode.START));

        if(billingSearchVO.getCollectedByUser() != null)
            criteria.add(Restrictions.eq("collectedByUser",billingSearchVO.getCollectedByUser()));

        if(billingSearchVO.getConsultant() != null)
            criteria.add(Restrictions.eq("consultant",billingSearchVO.getConsultant()));
        if(fromDate != null) criteria.add(Restrictions.ge("invoiceDate",UtilDateTime.getDayStart(fromDate)));
        if(thruDate != null) criteria.add(Restrictions.le("invoiceDate",UtilDateTime.getDayEnd(thruDate)));
        if(UtilValidator.isNotEmpty(billingSearchVO.getStatus()) || UtilValidator.isNotEmpty(billingSearchVO.getOrStatus())){
            criteria.add(Restrictions.or(Restrictions.eq("invoiceStatus", billingSearchVO.getStatus()), Restrictions.eq("invoiceStatus", billingSearchVO.getOrStatus())));
        }else if(UtilValidator.isNotEmpty(billingSearchVO.getStatus()))
            criteria.add(Restrictions.eq("invoiceStatus", billingSearchVO.getStatus()));

        if ("EQUAL".equalsIgnoreCase(billingSearchVO.getLowEndAmtQuantifier()))
            criteria.add(Restrictions.eq("totalAmount.amount", billingSearchVO.getLowAmntRange().getAmount()));
        else
        if ("LESS".equalsIgnoreCase(billingSearchVO.getLowEndAmtQuantifier()))
            criteria.add(Restrictions.lt("totalAmount.amount", billingSearchVO.getLowAmntRange().getAmount()));
        else
        if ("Greater".equalsIgnoreCase(billingSearchVO.getLowEndAmtQuantifier()))
            criteria.add(Restrictions.gt("totalAmount.amount", billingSearchVO.getLowAmntRange().getAmount()));
        else
        if ("Between".equalsIgnoreCase(billingSearchVO.getLowEndAmtQuantifier())) {
            criteria.add(Restrictions.gt("totalAmount.amount",billingSearchVO.getLowAmntRange().getAmount()));
            criteria.add(Restrictions.lt("totalAmount.amount",billingSearchVO.getHighAmntRange().getAmount()));
        }
        if(billingSearchVO.getCpt() != null){
            criteria.createCriteria("invoiceItems").add(Restrictions.eq("itemId", billingSearchVO.getCpt().getId()))
                    .add(Restrictions.eq("itemType", InvoiceType.OPD_PROCEDURE));
        }
        if(patientInsuranceIds != null && UtilValidator.isNotEmpty(billingSearchVO.getInsurancePayer())) {
        	criteria.add((Restrictions.in("patientInsuranceId", patientInsuranceIds)));
        }
        
        if(UtilValidator.isNotEmpty(billingSearchVO.getCorporatePayer())) {
        	 Criteria corporateCriteria = patientCriteria.createCriteria("patientCorporate");
        	 corporateCriteria.add(Restrictions.eq("corporateId",billingSearchVO.getCorporatePayer()));
        }
        
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public List<InvoiceItem> searchInvoiceItemBy(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate, List patientInsuranceIds) {
        Criteria criteria = getSession().createCriteria(Invoice.class);
        if(billingSearchVO.getConsultant() != null)
            criteria.add(Restrictions.eq("consultant",billingSearchVO.getConsultant()));
        if(fromDate != null) criteria.add(Restrictions.ge("invoiceDate", UtilDateTime.getDayStart(fromDate)));
        if(thruDate != null) criteria.add(Restrictions.le("invoiceDate", UtilDateTime.getDayEnd(thruDate)));
        Criteria invItmCriteria = criteria.createCriteria("invoiceItems");
        if(UtilValidator.isNotEmpty(billingSearchVO.getServiceType())){
            invItmCriteria.add(Restrictions.eq("itemType",InvoiceType.valueOf(billingSearchVO.getServiceType())));
        }

         if(billingSearchVO.getCpt() != null){
             invItmCriteria.add(Restrictions.eq("cpt", billingSearchVO.getCpt()));
         }

        /*if(billingSearchVO.getSlottype() != null){
            invItmCriteria.add(Restrictions.like("description", "%"+billingSearchVO.getSlottype().getDescription().toUpperCase()+"%"));
        }*/
        if (billingSearchVO.getSlottype() != null){
            String slotDescription = billingSearchVO.getSlottype().getDescription();
            if ((slotDescription != null) && (!slotDescription.equals(""))){
                if ((slotDescription.equals("Home Visit")) || (slotDescription.equals("Premium Visit")) || (slotDescription.equals("Tele Consultation Visit"))){
                    String searchString = billingSearchVO.getSlottype().getDescription();
                    searchString = searchString.substring(0,searchString.indexOf(' '));
                    invItmCriteria.add(Restrictions.like("description", "%" + searchString.toUpperCase() + "%"));
                } else if ((slotDescription.equals("Consult Visit"))) {
                    invItmCriteria.add(Restrictions.or(Restrictions.like("description", "%" + "CONSULT VISIT" + "%"),Restrictions.eq("description","CONSULTATION AFYA SMART SERVICE")));
                } else {
                    invItmCriteria.add(Restrictions.like("description", "%" + slotDescription.toUpperCase() + "%"));
                }
            }
        }

        if(billingSearchVO.getPostalAddressFields() != null){
            criteria.createCriteria("patient").add(Restrictions.eq("contacts.postalAddress.city", billingSearchVO.getPostalAddressFields()));
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<Invoice> invoicesList = criteria.list();
        List<InvoiceItem> invoiceItems = new ArrayList<InvoiceItem>();
        if(invoicesList.size() > 0){
        for(Invoice invoice : invoicesList){
                List<InvoiceItem> invoiceItemList = invoice.getInvoiceItems();
                if(UtilValidator.isNotEmpty(billingSearchVO.getServiceType())) {
                    for (InvoiceItem ii : invoiceItemList) {
                        if (ii.getItemType().equals(InvoiceType.valueOf(billingSearchVO.getServiceType()))) {
                            invoiceItems.add(ii);
                        }
                    }
                }else{
                    invoiceItems.addAll(invoiceItemList);
                }
            }
        }
        // Count code is added for No. of times patient visited
        List<InvoiceItem> groupByinvoiceItems = new ArrayList<InvoiceItem>();
            for (InvoiceItem  invoiceItem : invoiceItems) {
                if(UtilValidator.isEmpty(groupByinvoiceItems)){
                    groupByinvoiceItems.add(invoiceItem);
                }
                else{
                    boolean isAvailable = false;
                    for(InvoiceItem ii : groupByinvoiceItems){
                        if( invoiceItem.getItemType().equals(ii.getItemType()) && invoiceItem.getDescription().equals(ii.getDescription()) && invoiceItem.getInvoice().getConsultant().equals(ii.getInvoice().getConsultant())
                                && invoiceItem.getInvoice().getPatient().equals(ii.getInvoice().getPatient()) ){
                            ii.setInvoiceItemCountForReport( ii.getInvoiceItemCountForReport() + 1);
                            isAvailable = true;
                        }
                    }
                    if(!isAvailable){
                        groupByinvoiceItems.add(invoiceItem) ;
                    }
                }
            }

        return groupByinvoiceItems;

        // return invoiceItems;
    }
    @Override
    public List<InvoiceItem> searchInvoiceItemWithOutCount(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate, List patientInsuranceIds) {
        Criteria criteria = getSession().createCriteria(Invoice.class);
        if(billingSearchVO.getConsultant() != null)
            criteria.add(Restrictions.eq("consultant",billingSearchVO.getConsultant()));
        if(fromDate != null) criteria.add(Restrictions.ge("invoiceDate", UtilDateTime.getDayStart(fromDate)));
        if(thruDate != null) criteria.add(Restrictions.le("invoiceDate", UtilDateTime.getDayEnd(thruDate)));
        Criteria invItmCriteria = criteria.createCriteria("invoiceItems");
        if(UtilValidator.isNotEmpty(billingSearchVO.getServiceType())){
            invItmCriteria.add(Restrictions.eq("itemType",InvoiceType.valueOf(billingSearchVO.getServiceType())));
        }

        if(billingSearchVO.getCpt() != null){
            invItmCriteria.add(Restrictions.eq("cpt", billingSearchVO.getCpt()));
        }

        /*if(billingSearchVO.getSlottype() != null){
            invItmCriteria.add(Restrictions.like("description", "%"+billingSearchVO.getSlottype().getDescription().toUpperCase()+"%"));
        }*/
        if (billingSearchVO.getSlottype() != null){
            String slotDescription = billingSearchVO.getSlottype().getDescription();
            if ((slotDescription != null) && (!slotDescription.equals(""))){
                if ((slotDescription.equals("Home Visit")) || (slotDescription.equals("Premium Visit")) || (slotDescription.equals("Tele Consultation Visit"))){
                    String searchString = billingSearchVO.getSlottype().getDescription();
                    searchString = searchString.substring(0,searchString.indexOf(' '));
                    invItmCriteria.add(Restrictions.like("description", "%" + searchString.toUpperCase() + "%"));
                } else if ((slotDescription.equals("Consult Visit"))) {
                    invItmCriteria.add(Restrictions.or(Restrictions.like("description", "%" + "CONSULT VISIT" + "%"),Restrictions.eq("description","CONSULTATION AFYA SMART SERVICE")));
                } else {
                    invItmCriteria.add(Restrictions.like("description", "%" + slotDescription.toUpperCase() + "%"));
                }
            }
        }

        if(billingSearchVO.getPostalAddressFields() != null){
            criteria.createCriteria("patient").add(Restrictions.eq("contacts.postalAddress.city", billingSearchVO.getPostalAddressFields()));
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<Invoice> invoicesList = criteria.list();
        List<InvoiceItem> invoiceItems = new ArrayList<InvoiceItem>();
        if(invoicesList.size() > 0){
            for(Invoice invoice : invoicesList){
                List<InvoiceItem> invoiceItemList = invoice.getInvoiceItems();
                if(UtilValidator.isNotEmpty(billingSearchVO.getServiceType())) {
                    for (InvoiceItem ii : invoiceItemList) {
                        if (ii.getItemType().equals(InvoiceType.valueOf(billingSearchVO.getServiceType()))) {
                            invoiceItems.add(ii);
                        }
                    }
                }else{
                    invoiceItems.addAll(invoiceItemList);
                }
            }
        }
     return invoiceItems;
    }

    @Override
    public List<InvoiceItem> searchInvoiceItemByCancelStatus(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate) {
        Criteria criteria = getSession().createCriteria(Invoice.class);

        if(fromDate != null) criteria.add(Restrictions.ge("invoiceDate", UtilDateTime.getDayStart(fromDate)));
        if(thruDate != null) criteria.add(Restrictions.le("invoiceDate", UtilDateTime.getDayEnd(thruDate)));
        Criteria invItmCriteria = criteria.createCriteria("invoiceItems");
        invItmCriteria.add(Restrictions.eq("invoiceItemStatus","Cancel"));
        if(UtilValidator.isNotEmpty(billingSearchVO.getServiceType())){
            invItmCriteria.add(Restrictions.eq("itemType",InvoiceType.valueOf(billingSearchVO.getServiceType())));
        }
        if(UtilValidator.isNotEmpty(billingSearchVO.getServiceType())){
            invItmCriteria.add(Restrictions.eq("itemType",InvoiceType.valueOf(billingSearchVO.getServiceType())));
        }
        if(billingSearchVO.getCpt() != null){
            invItmCriteria.add(Restrictions.eq("cpt", billingSearchVO.getCpt()));
        }
        /*if(billingSearchVO.getSlottype() != null){
            invItmCriteria.add(Restrictions.like("description", "%" + billingSearchVO.getSlottype().getDescription().toUpperCase() + "%"));
        }*/
        if (billingSearchVO.getSlottype() != null){
            String slotDescription = billingSearchVO.getSlottype().getDescription();
            if ((slotDescription != null) && (!slotDescription.equals(""))){
                if ((slotDescription.equals("Home Visit")) || (slotDescription.equals("Premium Visit")) || (slotDescription.equals("Tele Consultation Visit"))){
                    String searchString = billingSearchVO.getSlottype().getDescription();
                    searchString = searchString.substring(0,searchString.indexOf(' '));
                    invItmCriteria.add(Restrictions.like("description", "%" + searchString.toUpperCase() + "%"));
                } else if ((slotDescription.equals("Consult Visit"))) {
                    invItmCriteria.add(Restrictions.or(Restrictions.like("description", "%" + "CONSULT VISIT" + "%"),Restrictions.eq("description","CONSULTATION AFYA SMART SERVICE")));
                } else {
                    invItmCriteria.add(Restrictions.like("description", "%" + slotDescription.toUpperCase() + "%"));
                }
            }
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<Invoice> invoicesList = criteria.list();
        List<InvoiceItem> invoiceItems = new ArrayList<InvoiceItem>();
        if(invoicesList.size() > 0){
            for(Invoice invoice : invoicesList){
                List<InvoiceItem> invoiceItemList = invoice.getInvoiceItems();
                if(UtilValidator.isNotEmpty(invoiceItemList)) {
                    for (InvoiceItem invoiceItem : invoiceItemList) {
                        if ((invoiceItem.getInvoiceItemStatus() != null) && (invoiceItem.getInvoiceItemStatus().equals("Cancel"))){
                            invoiceItems.add(invoiceItem);
                        }
                    }
                }
            }
        }

         return invoiceItems;
    }

    @Override
    public List<Invoice> searchPendingInsuranceInvoiceBy(BillingSearchVO billingSearchVO, Date fromDate, Date thruDate, List patientInsuranceIds) {
        Criteria criteria = getSession().createCriteria(Invoice.class);
        if (billingSearchVO.getPatient() != null)
            criteria.add(Restrictions.eq("patient", billingSearchVO.getPatient()));


        criteria.add(Restrictions.eq("insuranceStatus", INSURANCESTATUS.SENT_FOR_CLAIM));
        if (patientInsuranceIds != null) {
            if (patientInsuranceIds.size() == 0) {
                patientInsuranceIds.add((long) 0);
            }
            criteria.add(Restrictions.in("patientInsuranceId", patientInsuranceIds));
        }

        if (billingSearchVO.getConsultant() != null)
            criteria.add(Restrictions.eq("consultant", billingSearchVO.getConsultant()));
        if (fromDate != null)
            criteria.add(Restrictions.ge("invoiceDate", UtilDateTime.getDayStart(fromDate)));
        if (thruDate != null)
            criteria.add(Restrictions.le("invoiceDate", UtilDateTime.getDayEnd(thruDate)));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public List<Invoice> searchReferralInvoiceBy(BillingSearchVO billingSearchVO, Date fromDate, Date thruDate) {
        Criteria criteria = getSession().createCriteria(Invoice.class);

        criteria.add(Restrictions.gt("totalReferralAmountTobePaid", new BigDecimal(0.0)));

        if (billingSearchVO.getConsultant() != null)
            criteria.add(Restrictions.eq("consultant", billingSearchVO.getConsultant()));
        if (fromDate != null)
            criteria.add(Restrictions.ge("invoiceDate", UtilDateTime.getDayStart(fromDate)));
        if (thruDate != null)
            criteria.add(Restrictions.le("invoiceDate", UtilDateTime.getDayEnd(thruDate)));
        if (billingSearchVO.getSelectedReferralDoctor() != null)
            criteria.add(Restrictions.eq("referralDoctorFirstName", billingSearchVO.getSelectedReferralDoctor().getFirstName()));
        if (billingSearchVO.getSelectedReferralDoctor() != null)
            criteria.add(Restrictions.eq("referralDoctorLastName", billingSearchVO.getSelectedReferralDoctor().getLastName()));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public List<InvoiceItem> searchInvoiceItemByConcession(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate) {
        Criteria criteria = getSession().createCriteria(InvoiceItem.class);

       Criteria invoiceCriteria = criteria.createCriteria("invoice");
           		if(fromDate != null) invoiceCriteria.add(Restrictions.ge("invoiceDate",UtilDateTime.getDayStart(fromDate)));
        		if(thruDate != null) invoiceCriteria.add(Restrictions.le("invoiceDate", UtilDateTime.getDayEnd(thruDate)));
        if(billingSearchVO.getConsultant() != null)
            criteria.add(Restrictions.eq("provider",billingSearchVO.getConsultant()));
       
        criteria.add(Restrictions.isNotNull("concessionAmount"));
        
        if(UtilValidator.isNotEmpty(billingSearchVO.getAuthoriser()))
        	criteria.add(Restrictions.eq("concessionAuthoriser", billingSearchVO.getAuthoriser()));
        if(UtilValidator.isNotEmpty(billingSearchVO.getUser()))
        	criteria.add(Restrictions.eq("updatedBy", billingSearchVO.getUser()));
        
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        	return criteria.list();
    }

    
    @Override
    public List<InvoiceItem> searchCancelledInvoiceItem(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate) {
        Criteria criteria = getSession().createCriteria(InvoiceItem.class);
        
        Criteria invoiceCriteria = criteria.createCriteria("invoice");
           if(fromDate != null) invoiceCriteria.add(Restrictions.ge("invoiceDate",UtilDateTime.getDayStart(fromDate)));
           if(thruDate != null) invoiceCriteria.add(Restrictions.le("invoiceDate", UtilDateTime.getDayEnd(thruDate)));
        		
          if(billingSearchVO.getPatient() != null)
        	invoiceCriteria.add(Restrictions.eq("patient", billingSearchVO.getPatient()));
        if(billingSearchVO.getConsultant() != null)
            criteria.add(Restrictions.eq("provider",billingSearchVO.getConsultant() ));
        	
        if(UtilValidator.isNotEmpty(billingSearchVO.getPatientType())) {
             Criteria patientCriteria = invoiceCriteria.createCriteria("patient");
             patientCriteria.add(Restrictions.like("patientType", billingSearchVO.getPatientType(), MatchMode.ANYWHERE));
           } 		
      criteria.add(Restrictions.eq("invoiceItemStatus","Cancel"));
        
      if(UtilValidator.isNotEmpty(billingSearchVO.getUser()))
        	criteria.add(Restrictions.eq("updatedBy",billingSearchVO.getUser()));
        
      criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }
    
    @Override
    public BigDecimal[] getInvoiceTotal(Invoice invoice) {
        Criteria criteria = getSession().createCriteria(InvoiceItem.class, "item");
        criteria.add(Restrictions.eq("invoice",invoice));
        criteria.setProjection(Projections.projectionList().add(Projections.sum("price.amount")));
        BigDecimal totalAmt= (BigDecimal)criteria.uniqueResult();

        criteria = getSession().createCriteria(InvoicePayment.class, "item");
        criteria.add(Restrictions.eq("invoice",invoice));
        criteria.setProjection(Projections.projectionList().add(Projections.sum("amount.amount")));
        BigDecimal totalAdvanceAmt= (BigDecimal)criteria.uniqueResult();

        return new BigDecimal[]{totalAmt,totalAdvanceAmt};
    }

    @Override
    public List<Invoice> getFirstInvoice(Patient patient) {
        Criteria criteria = getSession().createCriteria(Invoice.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.addOrder(Order.asc("id"));
        List<Invoice> invoices = new ArrayList<Invoice>();
        if(criteria.list() != null){
	        Set<Invoice> set = new HashSet<Invoice>(criteria.list());
	        invoices.addAll(set);
        }
        return  invoices;
    }

    @Override
    public List<Invoice> getAllInvoices(Patient patient) {
        Criteria criteria = getSession().createCriteria(Invoice.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.addOrder(Order.asc("id"));

        if(UtilValidator.isEmpty(criteria.list()))
            return null;

        Set set = new HashSet(criteria.list());
        return  new ArrayList(set);
    }

    @Override
    public List<Consultation> getConsultationChargeForPatientCheckedIn(Employee employee,SlotType slotType) {
        Criteria criteria = getSession().createCriteria(Consultation.class);
        criteria.add(Restrictions.eq("person", employee));
        if(slotType!=null)
            criteria.add(Restrictions.eq("soapNoteType", slotType));
        return criteria.list();
    }

    @Override
    public List<Invoice> getBillingTransactionForSchedule(Schedule schedule) {
        Criteria criteria = getSession().createCriteria(Invoice.class);
        criteria.add(Restrictions.eq("itemId", String.valueOf(schedule.getId())));
        return criteria.list();
    }

    @Override
    public LabTestPanel getLabtestPanelByPanelName(String panelName) {
        Criteria criteria = getSession().createCriteria(LabTestPanel.class);
        criteria.add(Restrictions.eq("panelName", panelName));
        return (LabTestPanel) criteria.uniqueResult();
    }

    @Override
    public List<AcctgTransaction> searchAcctgTransactionBy(AcctgTransTypeEnum chargeType,Date fromDate,Date thruDate) {
        Criteria criteria = getSession().createCriteria(AcctgTransaction.class);
        if(UtilValidator.isNotEmpty(chargeType))
            criteria.add(Restrictions.eq("acctgTransTypeEnum",chargeType));
        if(fromDate != null)
            criteria.add(Restrictions.ge("transactionDate",UtilDateTime.getDayStart(fromDate)));
        if(thruDate != null)
            criteria.add(Restrictions.le("transactionDate",UtilDateTime.getDayEnd(thruDate)));
        return criteria.list();
    }

    public List<AcctgTransactionEntry> searchAcctgTransactionEntryForLabReport(Date fromDate,Date thruDate,Object chargeType){

        Criteria criteria = getSession().createCriteria(AcctgTransactionEntry.class);
        if(fromDate != null)
            criteria.add(Restrictions.ge("transactionDate",UtilDateTime.getDayStart(fromDate)));
        if(thruDate != null)
            criteria.add(Restrictions.le("transactionDate",UtilDateTime.getDayEnd(thruDate)));
        else
            criteria.add(Restrictions.eq("transactionType", AcctgTransTypeEnum.OPD_LAB_CHARGES));

        return criteria.list();
    }

    public List<AcctgTransactionEntry> searchAcctgTransactionEntryForLabReportExport(Date fromDate,Date thruDate,Object chargeType,Object provider, Object referral){

        Criteria criteria = getSession().createCriteria(AcctgTransactionEntry.class);
        if(fromDate != null)
            criteria.add(Restrictions.ge("transactionDate",UtilDateTime.getDayStart(fromDate)));
        if(thruDate != null)
            criteria.add(Restrictions.le("transactionDate",UtilDateTime.getDayEnd(thruDate)));
        else
            criteria.add(Restrictions.eq("transactionType",AcctgTransTypeEnum.OPD_LAB_CHARGES));


        if(provider!=null && provider instanceof String)
            criteria.add(Restrictions.isNotNull("doctorId"));
        if(provider!=null && provider instanceof Provider)
            criteria.add(Restrictions.eq("doctorId", ((Provider)provider).getId().toString()));

        if(referral!=null && referral instanceof String)
            criteria.add(Restrictions.isNotNull("referralId"));
        if(referral!=null && referral instanceof Referral)
            criteria.add(Restrictions.eq("referralId", ((Referral)referral).getId().toString()));
        return criteria.list();
    }

    @Override
    public Map<String,Object> getPriceFromMasterPriceConf(String serviceType, String mainGroup, String subGroup, String procedureCode,
                                                  String visitType, String doctor, String tariffCategory,String patientCategory, Date fromDate) {
        String query = "SELECT BILLABLE_AMOUNT as BILLABLE_AMOUNT,BILLABLE_AMOUNT_MIN as BILLABLE_AMOUNT_MIN,BILLABLE_AMOUNT_MAX as BILLABLE_AMOUNT_MAX, COPAY as COPAY, COPAY_TYPE as COPAY_TYPE " +
                                                  " FROM CLINIC_TARIFF WHERE SERVICE_TYPE ='" + serviceType + "' AND SERVICE_MAIN_GROUP ='" + mainGroup +
                "' AND SERVICE_SUB_GROUP ='" + subGroup + "' AND TARIFF_CATEGORY ='" + tariffCategory + "' AND PATIENT_CATEGORY ='" +
                patientCategory + "' AND FROM_DATE <= '" + UtilDateTime.formatToDbDate(fromDate)  + "' AND (THRU_DATE >='" +
                UtilDateTime.formatToDbDate(fromDate) + "' OR THRU_DATE IS NULL)";
        Query q = null;
        if(UtilValidator.isNotEmpty(procedureCode))
            q = getSession().createSQLQuery(query + " AND PROCEDURE_CODE ='" +procedureCode+"' ");
        else
            q = getSession().createSQLQuery(query + " AND VISIT_TYPE ='" +visitType+"' AND DOCTOR='" +doctor+ "' ");
        List l = q.list();

        if(UtilValidator.isNotEmpty(l)){
        	Object[] data = (Object[]) l.get(0);
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("BILLABLE_AMOUNT", (BigDecimal)data[0]);
        	map.put("BILLABLE_AMOUNT_MIN", (BigDecimal)data[1]);
        	map.put("BILLABLE_AMOUNT_MAX", (BigDecimal)data[2]);
        	map.put("COPAY", (BigDecimal)data[3]);
        	map.put("COPAY_TYPE", (String)data[4]);
        	return map;
        }else
            return null;
    }

    @Override
    public String getTariffCodeByTariffName(String tariffName) {
        String query = "SELECT TARIFF_CODE FROM tariff_category where TARIFF = '" + tariffName + "'";
        Query q = getSession().createSQLQuery(query);
        List<String> l = q.list();
        if(UtilValidator.isNotEmpty(l))
            return l.get(0);
        else
            return null;
    }

    @Override
    public String getServiceIdFromMasterPriceConf(String procedureCode,String visitType,String providerId , String tariffCategory , String patientCategory) {
        String query = "SELECT SERVICE_ID FROM CLINIC_TARIFF WHERE FROM_DATE <= '" + UtilDateTime.formatToDbDate(new Date())  + "' AND (THRU_DATE >='" +
                UtilDateTime.formatToDbDate(new Date()) + "' OR THRU_DATE IS NULL) AND TARIFF_CATEGORY='" + tariffCategory + "'" + " AND PATIENT_CATEGORY='" + patientCategory
                + "'";
        Query q = null;
        if(UtilValidator.isNotEmpty(procedureCode))
            q = getSession().createSQLQuery(query + " AND PROCEDURE_CODE ='" +procedureCode+"' ");
        else
            q = getSession().createSQLQuery(query + " AND VISIT_TYPE ='" +visitType+"' AND DOCTOR='" +providerId+ "' ");
        List<String> l = q.list();
        if(UtilValidator.isNotEmpty(l))
            return l.get(0);
        else
            return null;
    }

	@Override
	public ArrayList<InvoicePayment> getInvoicePaymentsByCriteria(Patient patient, String patientType, Date fromDate, Date thruDate) {
		Criteria criteria = getSession().createCriteria(InvoicePayment.class);
		if(fromDate != null)
			criteria.add(Restrictions.ge("paymentDate",UtilDateTime.getDayStart(fromDate)));
		if(thruDate != null)
			criteria.add(Restrictions.le("paymentDate",UtilDateTime.getDayEnd(thruDate)));
		/*if(patient != null  || UtilValidator.isNotEmpty(patientType)){
			if(patient != null)
				criteria.createAlias("invoice.patient", "patient").add(Restrictions.eq("patient", patient));
			if(UtilValidator.isNotEmpty(patientType))
				criteria.createAlias("invoice.patient", "patient").add(Restrictions.eq("patientType", patientType));
		}*/
		ArrayList<InvoicePayment> list = new ArrayList<InvoicePayment>();
		if(UtilValidator.isNotEmpty(criteria.list())){
			Set<InvoicePayment> set = new HashSet<InvoicePayment>(criteria.list());
			list.addAll(set);
		}
		return list;
	}

	@Override
	public Map<String, BigDecimal> getClinicRevenueByDate(Date fromDate, Date thruDate, boolean isMobileOrPatientPortal) {
        // result object
        Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();
        result.put("REVENUE", null);
        result.put("COLLECTABLE", null);

		int i = isMobileOrPatientPortal == true ? 1 : 0;
		String query = "SELECT IF(SUM(AMOUNT) IS NULL, 0, SUM(AMOUNT)) AS TotalAmount" +
                "   , IFNULL(SUM(IFNULL((SELECT invoice.AMOUNT - IFNULL(SUM(ip.AMOUNT),0) FROM invoice_payment ip WHERE ip.INVOICE_ID = invoice.ID),0)),0) as TotalPendingCollectable " +
                " FROM invoice WHERE INVOICE_STATUS != 'CANCELLED' AND  INVOICE_DATE >= '" +
				UtilDateTime.formatToDbDate(fromDate) + "' AND INVOICE_DATE <= '" + UtilDateTime.formatToDbDate(thruDate) + "' AND MOBILE_OR_PATINET_PORTAL = " + i;
		Query q = getSession().createSQLQuery(query);

        List<Object[]> queryRslt = q.list();
        BigDecimal revenue = (BigDecimal)queryRslt.get(0)[0];
        BigDecimal collectable = (BigDecimal)queryRslt.get(0)[1];


        //List<BigDecimal> l = q.list();
        //if(UtilValidator.isNotEmpty(l)) {
        if(UtilValidator.isNotEmpty(revenue)) {
            // kannan - added query to get Value Added Fees through Invoice-Items for the required Invoices
            /*String queryValueAddedFees = "SELECT IFNULL(SUM(invoice_item.AMOUNT), 0) AS TotalValueAdded FROM invoice_item\n" +
                " INNER JOIN invoice ON invoice.ID = invoice_item.invoice_id\n" +
                " WHERE  invoice_item.item_type = 'OPD_VALUE_ADDED' AND invoice.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) +
                    "'  AND invoice.INVOICE_DATE <= '" + UtilDateTime.formatToDbDate(thruDate) + "' AND invoice.MOBILE_OR_PATINET_PORTAL = " + i;*/
            // kannan - modified the  query to get Value Added Fees through invoice and RCM table, for the required Invoices, as a temp fix untill a new column is added to Invoice Table that will have the computed Value-Added amount
            if(isMobileOrPatientPortal == true) {   // kannan 2016-01-04 = condition to subtract value-added-fees only if 'isMobileOrPatientPortal'
                String queryValueAddedFees = "SELECT IFNULL(SUM(getRcmConvenienceFee(st.NAME, invoice.AMOUNT)), 0) AS TotalValueAdded FROM invoice\n" +
                        " INNER JOIN schedule sch ON sch.ID = invoice.schedule_id\n" +
                        " INNER JOIN slot_type st ON st.ID = sch.SOAP_NOTE_TYPE\n" +
                        " WHERE  invoice.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) +
                        "'  AND invoice.INVOICE_DATE <= '" + UtilDateTime.formatToDbDate(thruDate) + "' AND invoice.MOBILE_OR_PATINET_PORTAL = " + i +
                        " AND invoice.INVOICE_STATUS != 'CANCELLED' ";
                Query q2 = getSession().createSQLQuery(queryValueAddedFees);
                List<BigDecimal> l2 = q2.list();
                BigDecimal valueAddedFees = l2.get(0);
                if (UtilValidator.isNotEmpty(valueAddedFees))
                    revenue = revenue.subtract(valueAddedFees);
            }

            result.put("REVENUE", revenue);
            result.put("COLLECTABLE", collectable);
            return result;

            /*if(UtilValidator.isNotEmpty(l2)) {
                return l.get(0).subtract(l2.get(0));
            }else
                return  l.get(0);*/
        }
        else
            return result;
            //return null;
	}
	
	@Override
	public List<Map<String,Object>> getIncomeAnalysisByServiceType(Date fromDate, Date thruDate){
		/*String query = "SELECT SUBSTRING(REPLACE(ii.ITEM_TYPE, '_', ' '), 5) AS description, IF(SUM(ii.AMOUNT) IS NULL, 0, (SUM(ii.AMOUNT - IF(ii.item_type = 'OPD_CONSULTATION', getRcmConvenienceFee(st.NAME, iv.AMOUNT), 0)))) AS totalAmount " +
                        " FROM invoice_item ii JOIN invoice iv, schedule sch, slot_type st WHERE ii.INVOICE_ID = iv.ID AND sch.ID = iv.schedule_id AND st.ID = sch.SOAP_NOTE_TYPE " +
						" AND iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) +
						"' AND iv.INVOICE_DATE <= '" + UtilDateTime.formatToDbDate(thruDate) +
                        "' AND ii.ITEM_TYPE != 'OPD_VALUE_ADDED' " +    // kannan - added to exclude Value Added Items
                        "GROUP BY ii.ITEM_TYPE";*/
        // kannan 2015-11-03 -- modified query to use outer join for schedule table
        String query = "SELECT SUBSTRING(REPLACE(ii.ITEM_TYPE, '_', ' '), 5) AS description, IF(SUM(ii.NET_PRICE) IS NULL, 0, SUM(ii.NET_PRICE)) AS totalAmount -- (SUM(ii.NET_PRICE - IFNULL(IF(ii.item_type = 'OPD_CONSULTATION' && iv.MOBILE_OR_PATINET_PORTAL = true, getRcmConvenienceFee(st.NAME, iv.AMOUNT), 0),0)))) AS totalAmount \n" +
                " FROM invoice_item ii \n" +
                "   INNER JOIN invoice iv ON ii.INVOICE_ID = iv.ID\n" +
                "   LEFT OUTER JOIN `SCHEDULE` sch ON sch.ID = iv.schedule_id \n" +
                "   LEFT OUTER JOIN slot_type st ON st.ID = sch.SOAP_NOTE_TYPE \n" +
                " WHERE  \n" +
                "   iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '" + UtilDateTime.formatToDbDate(thruDate) + "'\n" +
                "   AND ii.ITEM_TYPE != 'OPD_VALUE_ADDED' AND iv.INVOICE_STATUS != 'CANCELLED' \n" +
                "        GROUP BY ii.ITEM_TYPE";
		Query q = getSession().createSQLQuery(query);
		
		List l = q.list();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
    	for(Object obj : l){
        	Object[] data = (Object[]) obj;
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("description", ((String)data[0]).trim());
        	map.put("totalAmount", (BigDecimal)data[1]);
        	list.add(map);
    	}
    	return list;
	}
	
	@Override
	public List<Map<String,Object>> getIncomeAnalysisBySpecialty(Date fromDate, Date thruDate){
		/*String query = "SELECT ps.SPECIALITY_ID, SUM(ii.amount) AS amount FROM invoice_item ii JOIN invoice iv, provider_speciality ps WHERE ii.INVOICE_ID = iv.ID " +
				" AND iv.consultant = ps.PROVIDER_ID AND iv.consultant IS NOT NULL " +
                " AND ii.item_type != 'OPD_VALUE_ADDED' " + // kannan - added to exclude Value Added from the total Amount
				" AND iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '" + UtilDateTime.formatToDbDate(thruDate) +
				"' GROUP BY ps.SPECIALITY_ID ORDER BY ps.SPECIALITY_ID";*/

        // kannan - modified query to derive the data from Invoice table after deducting the RCM Convenience Fees
        /*String query = "SELECT ps.SPECIALITY_ID, SUM(iv.AMOUNT - getRcmConvenienceFee(st.NAME, iv.AMOUNT)) AS amount FROM invoice iv JOIN schedule sch, slot_type st, provider_speciality ps WHERE " +
                " sch.ID = iv.schedule_id AND st.ID = sch.SOAP_NOTE_TYPE " +
                " AND iv.consultant = ps.PROVIDER_ID AND iv.consultant IS NOT NULL " +
                " AND iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '" + UtilDateTime.formatToDbDate(thruDate) +
                "' GROUP BY ps.SPECIALITY_ID ORDER BY ps.SPECIALITY_ID";*/

        // kannan - 2015-11-03 - modified query to use outer join for schedule table
        String query = "SELECT ps.SPECIALITY_ID,  SUM(IFNULL(iv.AMOUNT,0) /*- IFNULL(IF(iv.MOBILE_OR_PATINET_PORTAL = true, getRcmConvenienceFee(st.NAME, iv.AMOUNT),0), 0)*/) AS amount " +
                " FROM invoice iv \n" +
                "       INNER JOIN provider_speciality ps ON iv.consultant = ps.PROVIDER_ID AND iv.consultant IS NOT NULL \n" +
                "       LEFT OUTER JOIN SCHEDULE sch ON sch.ID = iv.schedule_id \n" +
                "       LEFT OUTER JOIN slot_type st ON st.ID = sch.SOAP_NOTE_TYPE \n" +
                " WHERE \n" +
                "       iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '" + UtilDateTime.formatToDbDate(thruDate) + "' \n" +
                "       AND iv.INVOICE_STATUS != 'CANCELLED' " +
                " GROUP BY ps.SPECIALITY_ID ORDER BY ps.SPECIALITY_ID";


		Query q = getSession().createSQLQuery(query);
		
		List l = q.list();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
    	for(Object obj : l){
        	Object[] data = (Object[]) obj;
        	Map<String, Object> map = new HashMap<String, Object>();
        	//map.put("consultant", data[0]);
        	map.put("speciality", data[0]);
        	map.put("totalAmount", data[1]);
        	list.add(map);
    	}
    	return list;
	}

    @Override
    public List<Map<String,Object>> getIncomeAnalysisByDoctor(Date fromDate, Date thruDate){
        /*String query = "SELECT p.FIRST_NAME, SUM(ii.amount) AS amount FROM invoice_item ii JOIN invoice iv, PERSON P WHERE ii.INVOICE_ID = iv.ID " +
                " AND iv.consultant = P.id"+
                " AND iv.consultant IS NOT NULL AND " +
                " ii.item_type != 'OPD_VALUE_ADDED' AND" + // kannan - added to exclude Value Added from the total Amount
                " iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '"+ UtilDateTime.formatToDbDate(thruDate) +
                " '  GROUP BY iv.consultant ORDER BY P.FIRST_NAME";*/

        // kannan - modified query to derive the data from Invoice table after deducting the RCM Convenience Fees
        /*String query = "SELECT p.FIRST_NAME, SUM(iv.amount - getRcmConvenienceFee(st.NAME, iv.AMOUNT)) AS amount FROM invoice iv JOIN schedule sch, slot_type st, PERSON P WHERE  " +
                " sch.ID = iv.schedule_id AND st.ID = sch.SOAP_NOTE_TYPE " +
                " AND iv.consultant = P.id"+
                " AND iv.consultant IS NOT NULL AND " +
                " iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '"+ UtilDateTime.formatToDbDate(thruDate) +
                " '  GROUP BY iv.consultant ORDER BY P.FIRST_NAME";*/

        // kannan - 2015-11-03 -- modified query to use outer join for schedule table
        String query = "SELECT p.FIRST_NAME, SUM(IFNULL(iv.amount,0) /*- IFNULL(IF(iv.MOBILE_OR_PATINET_PORTAL = true, getRcmConvenienceFee(st.NAME, iv.AMOUNT), 0),0)*/) AS amount \n" +
                " FROM invoice iv \n" +
                "       INNER JOIN PERSON P ON iv.consultant = P.id AND iv.consultant IS NOT NULL \n" +
                "       LEFT OUTER JOIN SCHEDULE sch ON sch.ID = iv.schedule_id \n" +
                "       LEFT OUTER JOIN slot_type st ON st.ID = sch.SOAP_NOTE_TYPE\n" +
                " WHERE \n" +
                "       iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '"+ UtilDateTime.formatToDbDate(thruDate) + "'\n" +
                "       AND iv.INVOICE_STATUS != 'CANCELLED' " +
                " GROUP BY iv.consultant \n" +
                " ORDER BY P.FIRST_NAME";

        Query q = getSession().createSQLQuery(query);

        List l = q.list();
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        for(Object obj : l){
            Object[] data = (Object[]) obj;
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("doctor", data[0]);
            map.put("amount", data[1]);
            list.add(map);
        }
        return list;
    }

    @Override
    public List<Map<String,Object>> getIncomeAnalysisByPatientCategory(Date fromDate, Date thruDate){
        /*String query = "SELECT p.patient_type, SUM(ii.amount) AS amount FROM invoice_item ii JOIN invoice iv, patient p WHERE ii.INVOICE_ID = iv.ID " +
                " AND iv.patient = p.id"+
                " AND p.patient_type IS NOT NULL " +
                " AND ii.item_type != 'OPD_VALUE_ADDED' " + // kannan - added to exclude Value Added from the total Amount
                " AND iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '"+ UtilDateTime.formatToDbDate(thruDate) +
                " '  GROUP BY p.patient_type ORDER BY p.patient_type";*/

        // kannan - modified query to derive the data from Invoice table after deducting the RCM Convenience Fees
        /*String query = "SELECT p.patient_type, SUM(iv.amount - getRcmConvenienceFee(st.NAME, iv.AMOUNT)) AS amount FROM invoice iv JOIN schedule sch, slot_type st, patient p WHERE " +
                " sch.ID = iv.schedule_id AND st.ID = sch.SOAP_NOTE_TYPE " +
                " AND iv.patient = p.id"+
                " AND p.patient_type IS NOT NULL " +
                " AND iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '"+ UtilDateTime.formatToDbDate(thruDate) +
                " '  GROUP BY p.patient_type ORDER BY p.patient_type";*/

        // kannan - modified query to use outer join for schedule table
        String query = "SELECT p.patient_type, SUM(IFNULL(iv.amount,0) /*- IFNULL(IF(iv.MOBILE_OR_PATINET_PORTAL = true, getRcmConvenienceFee(st.NAME, iv.AMOUNT),0),0)*/) AS amount \n" +
                " FROM invoice iv \n" +
                "       INNER JOIN patient p ON iv.patient = p.id AND p.patient_type IS NOT NULL \n" +
                "       LEFT OUTER JOIN SCHEDULE sch ON sch.ID = iv.schedule_id \n" +
                "       LEFT OUTER JOIN slot_type st ON st.ID = sch.SOAP_NOTE_TYPE \n" +
                " WHERE \n" +
                "       iv.INVOICE_DATE >= '" + UtilDateTime.formatToDbDate(fromDate) + "' AND iv.INVOICE_DATE <= '"+ UtilDateTime.formatToDbDate(thruDate) + "'\n" +
                "       AND iv.INVOICE_STATUS != 'CANCELLED' " +
                " GROUP BY p.patient_type \n" +
                " ORDER BY p.patient_type\n";

        Query q = getSession().createSQLQuery(query);

        List l = q.list();
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        for(Object obj : l){
            Object[] data = (Object[]) obj;
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("category", data[0]);
            map.put("amount", data[1]);
            list.add(map);
        }
        return list;
    }

    @Override
    public String getServiceCost(Employee provider, String tariffCategory, String visitType, String type){
        String query = "";
        if (type.equals("cpt")) {
             query = "SELECT c.SERVICE_COST FROM clinic_tariff c WHERE PROCEDURE_CODE = '" + visitType + "'  AND TARIFF_CATEGORY = '" + tariffCategory + "' AND DOCTOR = "+provider.getId();
        } else if (type.equals("consult")){
             query = "SELECT c.SERVICE_COST FROM clinic_tariff c WHERE VISIT_TYPE = " + Integer.parseInt(visitType) + "  AND TARIFF_CATEGORY = '" + tariffCategory + "' AND DOCTOR = "+provider.getId();
        }

        Query q = getSession().createSQLQuery(query);

        List l = q.list();
        if ((l.size() > 0) && (l.get(0) != null)){
            return l.get(0).toString();
        }
        else
            return null;
    }

    public String getServiceCostForProduct(Product product){

        String query = "SELECT AVG(DISTINCT(AMOUNT)) FROM invoice_item WHERE product IS NOT NULL AND product="+product.getId();
        Query q = getSession().createSQLQuery(query);
        BigDecimal result = (BigDecimal)q.uniqueResult();

        if (result != null){
            return result.toString();
        } else {
            return BigDecimal.ZERO.toString();
        }
    }

    @Override
    public Boolean checkThruDate(String serviceType, String mainGroup, String subGroup, String procedureCode,
                                                          String visitType, String doctor, String tariffCategory,String patientCategory, Date fromDate) {
        String query = "SELECT BILLABLE_AMOUNT as BILLABLE_AMOUNT,BILLABLE_AMOUNT_MIN as BILLABLE_AMOUNT_MIN,BILLABLE_AMOUNT_MAX as BILLABLE_AMOUNT_MAX, COPAY as COPAY, COPAY_TYPE as COPAY_TYPE, THRU_DATE as THRU_DATE " +
                " FROM CLINIC_TARIFF WHERE SERVICE_TYPE ='" + serviceType + "' AND SERVICE_MAIN_GROUP ='" + mainGroup +
                "' AND SERVICE_SUB_GROUP ='" + subGroup + "' AND TARIFF_CATEGORY ='" + tariffCategory + "' AND PATIENT_CATEGORY ='" +
                patientCategory + "'";
        Query q = null;
        if(UtilValidator.isNotEmpty(procedureCode))
            q = getSession().createSQLQuery(query + " AND PROCEDURE_CODE ='" +procedureCode+"' ");
        else
            q = getSession().createSQLQuery(query + " AND VISIT_TYPE ='" +visitType+"' AND DOCTOR='" +doctor+ "' ");
        List l = q.list();
        if(UtilValidator.isNotEmpty(l)){
            Object[] data = (Object[]) l.get(0);
            Date thruDate = (Date)data[5];
            if (thruDate.before(fromDate)){
                return  true;
            } else {
                return false;
            }
        }else
            return false;
    }

    @Override
    public Object[] getTariffCategoryByTariffName(String tariffName) {
        String query = "SELECT TARIFF_CODE,PATIENT_CATEGORY FROM tariff_category where TARIFF = '" + tariffName + "'";
        Query q = getSession().createSQLQuery(query);
        List<Object[]> l = q.list();
        if(UtilValidator.isNotEmpty(l))
            return l.get(0);
        else
            return null;
    }

    @Override
    public Object[] getTariffCategoryByTariffCode(String tariffCode) {
        String query = "SELECT TARIFF,PATIENT_CATEGORY FROM tariff_category where TARIFF_CODE = '" + tariffCode + "'";
        Query q = getSession().createSQLQuery(query);
        List<Object[]> l = q.list();
        if(UtilValidator.isNotEmpty(l))
            return l.get(0);
        else
            return null;
    }

}
