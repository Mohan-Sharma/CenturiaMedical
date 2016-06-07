package com.nzion.view.component;

import com.nzion.domain.Patient;
import com.nzion.domain.PatientInsurance;
import com.nzion.domain.Provider;
import com.nzion.domain.SoapNoteType;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.domain.pms.Product;
import com.nzion.dto.PatientInvoiceDto;
import com.nzion.dto.PatientInvoiceItem;
import com.nzion.repository.ComponentRepository;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.service.PatientService;
import com.nzion.service.billing.BillingService;
import com.nzion.util.*;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by USER on 22-May-15.
 */
public class BillingAutoSuggestionServiceBox extends LookupBox{

    private String[] searchFields;

    private ComponentRepository componentRepository = (ComponentRepository) Infrastructure.getSpringBean("componentRepository");

    private CommonCrudRepository commonCrudRepository = (CommonCrudRepository) Infrastructure.getSpringBean("commonCrudRepository");

    private PatientInvoiceItem patientInvoiceItem;

    private PatientInvoiceDto dto;

    private Map<String, Object> selectedTariffCatagory = new HashMap<String, Object>();

    private PatientService patientService;

    private BillingService billingService;

    private List<SoapNoteType> sopenNoteTypes;

    private Provider selectedProvider;

    public BillingAutoSuggestionServiceBox(){
        setButtonVisible(false);
        setAutodrop(true);
        setHideListHeader(true);
        setHidePagination(true);
        addEventListener("onChanging", new OnChangingEventListener());
    }


    @Override
    public void setSearchcolumns(String sc) {
        super.setSearchcolumns(sc);
        searchFields = new String[searchcolumns.length];
        for (int i = 0; i < searchcolumns.length; i++)
            searchFields[i] = searchcolumns[i][0];
    }

    private boolean checkSoapNoteTypeSoapModuleMapping(SoapNoteType noteType){
        return commonCrudRepository.findByEquality(VisitTypeSoapModule.class, new String[]{"slotType"}, new Object[]{noteType}).size() > 0;
    }


    private class OnChangingEventListener implements EventListener {

		public void onEvent(Event event) throws Exception {
			String value = ((InputEvent) event).getValue();
			if(UtilValidator.isEmpty(value) || value.length() < 2)
				return;
			if(selectedProvider == null){
				UtilMessagesAndPopups.showError("Please select consulting doctor ");
				return;
			}
			List<?> cptList = componentRepository.searchEntities(value, Cpt.class, new String[]{"id","shortDescription"});
			List<SoapNoteType> sopenNoteTypes = componentRepository.searchEntities(value, SoapNoteType.class, new String[]{"name"});
			List<Product> productList = componentRepository.searchEntities(value, Product.class, new String[]{"tradeName"});
			Map<String, List> toBeDisplayed = new HashMap<>();
			List listToPut = new ArrayList();
			for(SoapNoteType obj : sopenNoteTypes){
				if(checkSoapNoteTypeSoapModuleMapping(obj)) {
					SoapNoteType soapNoteType = (SoapNoteType) obj;
					listToPut.add(soapNoteType);
				}
			}
			toBeDisplayed.put("Consultation",listToPut);
			listToPut = new ArrayList();
			for(Object obj : cptList){
				Cpt cpt = (Cpt) obj;
				listToPut.add(cpt);
			}
			toBeDisplayed.put("Procedure",listToPut);
			listToPut = new ArrayList();
			for(Object obj : productList){
				Product product = (Product) obj;
				listToPut.add(product);
			}
			toBeDisplayed.put("Product",listToPut);
			buildCustomDisplayBox(toBeDisplayed);
		}
	}

    protected void buildCustomDisplayBox(Map<String, List> toBeDisplayed) {
        Component div = (Component)getFirstChild().getChildren().get(1);
        div.getChildren().clear();
        Listbox listbox = new Listbox();
        listbox.setParent(div);
        listbox.addEventListener("onSelect", new OnCustomSelectListener());
        if(!isHidePagination()){
            Paging paging = new Paging();
            paging.setPageSize(Constants.PAGE_SIZE);
            paging.setPageIncrement(1);
            paging.setParent(div);
            listbox.setPaginal(paging);
            listbox.setMold("paging");
        }
        if(!isHideListHeader()){
            Listhead listhead = new Listhead();
            listhead.setParent(listbox);
            for(String[] column : getDisplaycolumns()){
                Listheader listheader = new Listheader(isUseLabels() ? column[1] : UtilDisplay.camelcaseToUiString(column[0]));
                listheader.setStyle("min-width:200px;");
                listheader.setParent(listhead);
            }
        }
        for(String key : toBeDisplayed.keySet()){
            Listitem li = new Listitem();
            li.setParent(listbox);
            Listcell lc = new Listcell();
            lc.setParent(li);
            lc.setLabel(key);
            lc = new Listcell();
            lc.setParent(li);
            lc = new Listcell();
            lc.setParent(li);
            for(Object data : toBeDisplayed.get(key)){
                Listitem listitem = new Listitem();
                listitem.setParent(listbox);
                listitem.setAttribute("object", data);

                Listcell listcel1 = new Listcell();
                listcel1.setLabel("");
                Listcell listcel2 = new Listcell();
                Listcell listcel3 = new Listcell();

				Label label = new Label("");
				Label priceLbl = new Label("0.000 KD");
				if(data instanceof SoapNoteType){
					label = new Label( ((SoapNoteType)data).getName());
					priceLbl.setValue(getProviderPrice(selectedProvider,dto.getPatient(),((SoapNoteType)data)) + " KD");
				}
				if(data instanceof Cpt){
					label = new Label( ((Cpt)data).getShortDescription());
					priceLbl.setValue(getCptPrice(((Cpt) data), dto.getPatient()) + " KD");
				}
				if(data instanceof Product){
					label = new Label( ((Product)data).getTradeName());
					priceLbl.setValue(getProductPrice((Product) data)+ " KD");
				}
				label.setParent(listcel2);
				priceLbl.setParent(listcel3);
				listcel1.setParent(listitem);
				listcel2.setParent(listitem);
				listcel3.setParent(listitem);
			}
		}
	}


    protected class OnCustomSelectListener implements EventListener{

		public void onEvent(Event event) throws Exception {
			Listbox listbox = (Listbox) event.getTarget();
			Object value = listbox.getSelectedItem().getAttribute("object");
			if(value instanceof SoapNoteType) {
				patientInvoiceItem.setInvoiceType("OPD_CONSULTATION");
				patientInvoiceItem.setSoapNoteType((SoapNoteType) value);
			}
			if(value instanceof Cpt){
				patientInvoiceItem.setInvoiceType("OPD_PROCEDURE");
				patientInvoiceItem.setCpt((Cpt) value);
			}
			if(value instanceof Product){
				patientInvoiceItem.setInvoiceType("OPD_PRODUCT");
				patientInvoiceItem.setProduct((Product) value);
			}
			setValue(patientInvoiceItem.getFormattedServiceName());
			listbox.detach();
			close();
			Events.postEvent("onChange",BillingAutoSuggestionServiceBox.this, patientInvoiceItem);
			Events.postEvent("onLookedUp",BillingAutoSuggestionServiceBox.this, patientInvoiceItem);
		}
	}


	private BigDecimal getProviderPrice(Provider provider, Patient patient, SoapNoteType soapNoteType) {
		BigDecimal amount = BigDecimal.ZERO;

		PatientInsurance patientInsurance = dto.getPatientInsurance();
		if ("INSURANCE".equals(patient.getPatientType()) && (patientInsurance == null || UtilValidator.isEmpty(patientInsurance.getInsuranceName())) && UtilValidator.isEmpty(selectedTariffCatagory)) {
			com.nzion.util.UtilMessagesAndPopups.showError("Insurance cannot be empty, Please select insurance");
			return BigDecimal.ZERO;
		}

		String patientCategory = "01";
		String tariffCategory = "00";

		if ("INSURANCE".equals(patient.getPatientType()) && UtilValidator.isEmpty(selectedTariffCatagory)) {
			patientCategory = "02";
			if ("INSURANCE".equals(patient.getPatientType()) && (patient.getPatientInsurances() == null || patient.getPatientInsurances().size() == 0)) {
				com.nzion.util.UtilMessagesAndPopups.showError("Insurance details not added for the Patient, Please add and Continue");
				return BigDecimal.ZERO;
			}

			String insuranceName = patientInsurance.getInsuranceName();
			String groupId = UtilValidator.isNotEmpty(patientInsurance.getGroupId()) ? patientInsurance.getGroupId() : new String();
			String healthPolicyId = UtilValidator.isNotEmpty(patientInsurance.getHealthPolicyId()) ? patientInsurance.getHealthPolicyId() : new String();

			List<Map<String, Object>> tariffCategorys = patientService.getTariffCategoryByPatientCategory("INSURANCE");
			for (Map<String, Object> map : tariffCategorys) {
				String tariff = map.get("tariff") == null ? new String() : ((String) map.get("tariff"));
				String group = map.get("groupId") == null ? new String() : ((String) map.get("groupId"));
				String healthPolicy = map.get("healthPolicyId") == null ? new String() : ((String) map.get("healthPolicyId"));
				if (tariff.equals(insuranceName) && UtilValidator.isEmpty(healthPolicy) && UtilValidator.isEmpty(group)) {
					tariffCategory = map.get("tariffCode").toString();
				}
				if (tariff.equals(insuranceName) && groupId.equals(group) && healthPolicyId.equals(healthPolicy)) {
					tariffCategory = map.get("tariffCode").toString();
					break;
				} else if (tariff.equals(insuranceName) && healthPolicyId.equals(healthPolicy) && UtilValidator.isEmpty(groupId)) {
					tariffCategory = map.get("tariffCode").toString();
					break;
				} else if (tariff.equals(insuranceName) && healthPolicyId.equals(healthPolicy) && !UtilValidator.isEmpty(groupId)) {
					if (UtilValidator.isEmpty(group)) {
						tariffCategory = map.get("tariffCode").toString();
						break;
					}
				}
			}

		} else if ("CORPORATE".equals(patient.getPatientType())) {
			patientCategory = "03";
			tariffCategory = patient.getPatientCorporate().getTariffCategoryId();
		} else if ("CASH PAYING".equals(patient.getPatientType())) {
			tariffCategory = patient.getTariffCode();
		}

		if (UtilValidator.isNotEmpty(selectedTariffCatagory))
			tariffCategory = (String) selectedTariffCatagory.get("tariffCode");

		String visitType = soapNoteType != null ? soapNoteType.getId().toString() : "10005";
		Map<String, Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, visitType, provider.getId().toString(), tariffCategory, patientCategory, new Date());
		if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null)
			amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
		return amount;
	}


    private BigDecimal getCptPrice(Cpt cpt, Patient patient) {
		PatientInsurance patientInsurance = dto.getPatientInsurance();

		if ("INSURANCE".equals(patient.getPatientType()) && (patientInsurance == null || UtilValidator.isEmpty(patientInsurance.getInsuranceName())) && UtilValidator.isEmpty(selectedTariffCatagory)) {
			com.nzion.util.UtilMessagesAndPopups.showError("Insurance cannot be empty, Please select insurance");
			return BigDecimal.ZERO;
		}

		String patientCategory = "01";
		String tariffCategory = "00";

		if ("INSURANCE".equals(patient.getPatientType()) && UtilValidator.isEmpty(selectedTariffCatagory)) {
			patientCategory = "02";

			String insuranceName = patientInsurance.getInsuranceName();
			String groupId = UtilValidator.isNotEmpty(patientInsurance.getGroupId()) ? patientInsurance.getGroupId() : new String();
			String healthPolicyId = UtilValidator.isNotEmpty(patientInsurance.getHealthPolicyId()) ? patientInsurance.getHealthPolicyId() : new String();

			List<Map<String, Object>> tariffCategorys = patientService.getTariffCategoryByPatientCategory("INSURANCE");
			for (Map<String, Object> map : tariffCategorys) {
				String tariff = map.get("tariff") == null ? new String() : ((String) map.get("tariff"));
				String group = map.get("groupId") == null ? new String() : ((String) map.get("groupId"));
				String healthPolicy = map.get("healthPolicyId") == null ? new String() : ((String) map.get("healthPolicyId"));
				if (tariff.equals(insuranceName) && UtilValidator.isEmpty(healthPolicy) && UtilValidator.isEmpty(group)) {
					tariffCategory = map.get("tariffCode").toString();
				}
				if (tariff.equals(insuranceName) && groupId.equals(group) && healthPolicyId.equals(healthPolicy)) {
					tariffCategory = map.get("tariffCode").toString();
					break;
				} else if (tariff.equals(insuranceName) && healthPolicyId.equals(healthPolicy) && UtilValidator.isEmpty(groupId)) {
					tariffCategory = map.get("tariffCode").toString();
					break;
				} else if (tariff.equals(insuranceName) && healthPolicyId.equals(healthPolicy) && !UtilValidator.isEmpty(groupId)) {
					if (UtilValidator.isEmpty(group)) {
						tariffCategory = map.get("tariffCode").toString();
						break;
					}
				}
			}
		} else if ("CORPORATE".equals(patient.getPatientType())) {
			patientCategory = "03";
			tariffCategory = patient.getPatientCorporate().getTariffCategoryId();
		} else if ("CASH PAYING".equals(patient.getPatientType())) {
			tariffCategory = patient.getTariffCode();
		}

		if (UtilValidator.isNotEmpty(selectedTariffCatagory))
			tariffCategory = (String) selectedTariffCatagory.get("tariffCode");

		BigDecimal cptPrice = cpt.getPrice();
		Map<String, Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", cpt.getId(), null, null, tariffCategory, patientCategory, new Date());
		if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null)
			cptPrice = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
		return cptPrice != null ? cptPrice : BigDecimal.ZERO;
	}

	public BigDecimal getProductPrice(Product product){
		List<Product> productList = componentRepository.getAll(Product.class);
		BigDecimal price = BigDecimal.ZERO;
		Iterator iterator = productList.iterator();
		while(iterator.hasNext()){
			Product product1 = (Product)iterator.next();
			if(product1.getId().equals(product.getId())){
				if ((product1.getSalesPrice() != null) && (!product1.getSalesPrice().equals(""))) {
					price = new BigDecimal(product1.getSalesPrice());
					break;
				} else {
					price = BigDecimal.ZERO;
					break;
				}
			}
		}
		return price;
	}

	public PatientInvoiceItem getPatientInvoiceItem() {
		return patientInvoiceItem;
	}

    public void setPatientInvoiceItem(PatientInvoiceItem patientInvoiceItem) {
        this.patientInvoiceItem = patientInvoiceItem;
    }

    public List<SoapNoteType> getSopenNoteTypes() {
        return sopenNoteTypes;
    }

    public void setSopenNoteTypes(List<SoapNoteType> sopenNoteTypes) {
        this.sopenNoteTypes = sopenNoteTypes;
    }


	public PatientInvoiceDto getDto() {
		return dto;
	}


	public void setDto(PatientInvoiceDto dto) {
		this.dto = dto;
	}


	public String[] getSearchFields() {
		return searchFields;
	}


	public void setSearchFields(String[] searchFields) {
		this.searchFields = searchFields;
	}


	public ComponentRepository getComponentRepository() {
		return componentRepository;
	}


	public void setComponentRepository(ComponentRepository componentRepository) {
		this.componentRepository = componentRepository;
	}


	public CommonCrudRepository getCommonCrudRepository() {
		return commonCrudRepository;
	}


	public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
		this.commonCrudRepository = commonCrudRepository;
	}


	public Map<String, Object> getSelectedTariffCatagory() {
		return selectedTariffCatagory;
	}


	public void setSelectedTariffCatagory(Map<String, Object> selectedTariffCatagory) {
		this.selectedTariffCatagory = selectedTariffCatagory;
	}


	public PatientService getPatientService() {
		return patientService;
	}


	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}


	public BillingService getBillingService() {
		return billingService;
	}


	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}


	public Provider getSelectedProvider() {
		return selectedProvider;
	}


	public void setSelectedProvider(Provider selectedProvider) {
		this.selectedProvider = selectedProvider;
	}


}
