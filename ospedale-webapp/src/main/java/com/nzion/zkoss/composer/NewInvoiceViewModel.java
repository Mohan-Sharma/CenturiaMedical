package com.nzion.zkoss.composer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.nzion.domain.*;

import com.nzion.domain.billing.*;
import com.nzion.domain.pms.Product;
import com.nzion.service.utility.UtilityFinder;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

import com.nzion.domain.billing.Invoice.INSURANCESTATUS;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.domain.pms.ProductBillOfMaterial;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.dto.CoPayment;
import com.nzion.dto.CoPaymentDetail;
import com.nzion.dto.HisModuleDto;
import com.nzion.dto.PatientInvoiceDto;
import com.nzion.dto.PatientInvoiceItem;
import com.nzion.exception.TransactionException;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.PatientService;
import com.nzion.service.ScheduleService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.emr.DiagnosisService;
import com.nzion.services.product.ProductService;
import com.nzion.util.AfyaServiceConsumer;
import com.nzion.util.Infrastructure;
import com.nzion.util.RestServiceConsumer;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;

/**
 * Created with IntelliJ IDEA. User: USER Date: 5/12/15 Time: 12:35 PM To change
 * this template use File | Settings | File Templates.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class NewInvoiceViewModel {

	@Wire("#newInvoiceWin")
	private Window newInvoiceWin;

	@WireVariable
	private CommonCrudService commonCrudService;

	@WireVariable
	private ScheduleService scheduleService;

	@WireVariable
	private BillingService billingService;

	@WireVariable
	private DiagnosisService diagnosisService;

	@WireVariable
	private PatientService patientService;

	@WireVariable
	private ProductService productService;

	private PatientInvoiceDto dto;

	private List<PatientInvoiceItem> patientInvoiceItems = new ArrayList<PatientInvoiceItem>();

	private List<InvoiceItem> cancelledInvoiceItem = new ArrayList<InvoiceItem>();

	private List<Provider> providerList;

	private List<Referral> referralList;

	private List<HisModuleDto> moduleDetailsDtos = new ArrayList();

	private List<SoapNoteType> sopenNoteTypes;

	private List<Invoice> oldInvoices;

	private List<Cpt> cptList;

	private List<Map<String, Object>> tariffCategorys = new ArrayList<Map<String, Object>>();

	private Map<String, Object> selectedTariffCatagory = new HashMap<String, Object>();

	private boolean corporateOrPatient = false;

	private boolean corporate = false;

	private Provider selectedProvider;

	private Referral selectedReferral;

	private List<Provider> referralClinicDoctors;

	private Provider selectedReferralDoctor;

	@Init
	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, true);
		dto = new PatientInvoiceDto();
		providerList = commonCrudService.getAll(Provider.class);
		referralList = commonCrudService.getAll(Referral.class,"clinicName");
		UtilityFinder utilityFinder = Infrastructure.getSpringBean("utilityFinder");
		referralList = utilityFinder.getUpdatedReferral(referralList,Infrastructure.getPractice().getTenantId());

		PatientInvoiceItem patientInvoiceItem = new PatientInvoiceItem();
		patientInvoiceItems.add(patientInvoiceItem);
		sopenNoteTypes = scheduleService.getAllSoapNoteTypes();
		cptList = new ArrayList<>();
		Object invoiceId = newInvoiceWin.getAttribute("invoiceId");

		if (UtilValidator.isNotEmpty(invoiceId)) {
			Invoice invoice = commonCrudService.getById(Invoice.class, Long.valueOf(invoiceId.toString()));

			getDto().setPatient(invoice.getPatient());
			oldInvoices = billingService.getAllInvoices(dto.getPatient());

			// new code start

			Collections.sort(oldInvoices, new Comparator<Invoice>() {
				@Override
				public int compare(Invoice o1, Invoice o2) {
					long firstInt = o1.getId();
					long secondInt = o2.getId();

					int i = firstInt > secondInt ? 1 : firstInt < secondInt ? -1 : 0;
					return i;
				}
			});

			// new code end

			Include patientinfo = (Include) newInvoiceWin.getFellow("patientinfo");
			patientinfo.setDynamicProperty("vm", this);
			patientinfo.setSrc("/portlets/billingPatientInfo.zul");
			patientinfo.invalidate();
			((Div) newInvoiceWin.getFellow("billingDiv")).setVisible(true);
			//((Div) newInvoiceWin.getFellow("invoiceDiv")).setVisible(true);
			getDto().setOldInvoice(invoice);
			if (UtilValidator.isNotEmpty(invoice.getPatient().getPatientInsurances())) {
				getDto().setPatientInsurance(invoice.getPatient().getPatientInsurances().iterator().next());
			}

			//new code start
			if ("CORPORATE".equals(dto.getPatient().getPatientType())) {
				corporateOrPatient = true;
				corporate = true;
				tariffCategorys = patientService.getTariffCategoryByPatientCategory("CASH PAYING");
			} else if ("CASH PAYING".equals(dto.getPatient().getPatientType())) {
				corporateOrPatient = true;
				corporate = false;
				tariffCategorys = patientService.getTariffCategoryByPatientCategory("CASH PAYING");
			} else {
				corporateOrPatient = true;
				tariffCategorys = patientService.getTariffCategoryByPatientCategory("CASH PAYING");
				corporate = false;
			}
			//new code end

			updatePatientInvoice();

			moduleDetailsDtos = RestServiceConsumer.getHISModules();
			BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
			String defaultHisModulde = billingDisplayConfig.getDefaultHisModuleId();
			if ("INSURANCE".equals(dto.getPatient().getPatientType())) {
				for (HisModuleDto hisModuleDto : moduleDetailsDtos) {
					if (hisModuleDto.getHisModuleId().equals(defaultHisModulde))
						dto.setHisModuleDto(hisModuleDto);
				}
			}

		}

	}

	@Command("getCtp")
	@NotifyChange("cptList")
	public void getCtp(@BindingParam("searchParam") String searchParam) {
		cptList = diagnosisService.serachCpt(searchParam);
	}

	@Command("addItem")
	@NotifyChange("patientInvoiceItems")
	public void addItem() {
		PatientInvoiceItem patientInvoiceItem = new PatientInvoiceItem();
		patientInvoiceItems.add(patientInvoiceItem);
	}

	@Command("removeItem")
	@NotifyChange("patientInvoiceItems")
	public void removeItem(@BindingParam("patientInvoiceItem") PatientInvoiceItem patientInvoiceItem) {
		patientInvoiceItems.remove(patientInvoiceItem);
	}

	@Command("refreshItem")
	@NotifyChange("patientInvoiceItems")
	public void refreshItem(@BindingParam("patientInvoiceItem") PatientInvoiceItem patientInvoiceItem) {
		int count = 0;
		if ("OPD_CONSULTATION".equals(patientInvoiceItem.getInvoiceType())) {
			if (selectedProvider == null) {
				UtilMessagesAndPopups.showError("Please select Consulting Doctor");
				patientInvoiceItems.remove(patientInvoiceItem);
				patientInvoiceItems.add(patientInvoiceItems.size(), new PatientInvoiceItem());
				return;
			}
			patientInvoiceItem.setDoctor(true);
			patientInvoiceItem.setProvider(selectedProvider);
			for (PatientInvoiceItem pi : patientInvoiceItems) {
				if ("OPD_CONSULTATION".equals(pi.getInvoiceType())) {
					count++;
					if (count == 2) {
						UtilMessagesAndPopups.showError("Consultation already exists");
						patientInvoiceItems.remove(patientInvoiceItem);
						patientInvoiceItems.add(patientInvoiceItems.size(), new PatientInvoiceItem());
						return;
					}
				}
			}
		}
		int index = patientInvoiceItems.indexOf(patientInvoiceItem);
		patientInvoiceItems.remove(patientInvoiceItem);
		patientInvoiceItems.add(index, patientInvoiceItem);
		updateUnitPriceAndGrossAmount(patientInvoiceItem);

		PatientInvoiceItem lastPatientInvoiceItem = patientInvoiceItems.get(patientInvoiceItems.size() - 1);
		if (UtilValidator.isNotEmpty(lastPatientInvoiceItem.getInvoiceType())) {
			addItem();
		} else if (patientInvoiceItems.size() == 1) {
			addItem();
		}

	}

	@Command("refreshItemQuantity")
	@NotifyChange("patientInvoiceItems")
	public void refreshItemQuantity(@BindingParam("patientInvoiceItem") PatientInvoiceItem patientInvoiceItem, @BindingParam("comp") Decimalbox decimalbox) {
		if ("OPD_CONSULTATION".equals(patientInvoiceItem.getInvoiceType())) {
			decimalbox.setValue(BigDecimal.ONE);
			patientInvoiceItem.setQuantity(BigDecimal.ONE);
		}
		int index = patientInvoiceItems.indexOf(patientInvoiceItem);
		patientInvoiceItems.remove(patientInvoiceItem);
		patientInvoiceItems.add(index, patientInvoiceItem);
		updateUnitPriceAndGrossAmount(patientInvoiceItem);

		PatientInvoiceItem lastPatientInvoiceItem = patientInvoiceItems.get(patientInvoiceItems.size() - 1);
		if (UtilValidator.isNotEmpty(lastPatientInvoiceItem.getInvoiceType())) {
			addItem();
		} else if (patientInvoiceItems.size() == 1) {
			addItem();
		}

	}

	@Command("getPatientOldInvoices")
	@NotifyChange({ "oldInvoices", "patientInvoiceItems" })
	public void getPatientOldInvoices() {
		if ("INSURANCE".equals(dto.getPatient().getPatientType()))
			moduleDetailsDtos = RestServiceConsumer.getHISModules();
		dto.setOldInvoice(null);
		oldInvoices = billingService.getAllInvoices(dto.getPatient());
		if (oldInvoices == null)
			oldInvoices = new ArrayList<>();
		// new code start

		Collections.sort(oldInvoices, new Comparator<Invoice>() {
			@Override
			public int compare(Invoice o1, Invoice o2) {
				long firstInt = o1.getId();
				long secondInt = o2.getId();

				int i = firstInt > secondInt ? 1 : firstInt < secondInt ? -1 : 0;
				return i;
			}
		});

		// new code end
		patientInvoiceItems.clear();
		PatientInvoiceItem patientInvoiceItem = new PatientInvoiceItem();
		patientInvoiceItems.add(patientInvoiceItem);

		if (dto.getPatientInsurance() == null) {
			if (dto.getPatient().getPatientInsurances() != null && dto.getPatient().getPatientInsurances().size() == 1)
				dto.setPatientInsurance(dto.getPatient().getPatientInsurances().iterator().next());
		}
		BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
		String defaultHisModulde = billingDisplayConfig.getDefaultHisModuleId();
		if ("INSURANCE".equals(dto.getPatient().getPatientType())) {
			for (HisModuleDto hisModuleDto : moduleDetailsDtos) {
				if (hisModuleDto.getHisModuleId().equals(defaultHisModulde))
					dto.setHisModuleDto(hisModuleDto);
			}
		}
		if ("CORPORATE".equals(dto.getPatient().getPatientType())) {
			corporateOrPatient = true;
			corporate = true;
			tariffCategorys = patientService.getTariffCategoryByPatientCategory("CASH PAYING");
		} else if ("CASH PAYING".equals(dto.getPatient().getPatientType())) {
			corporateOrPatient = true;
			corporate = false;
			tariffCategorys = patientService.getTariffCategoryByPatientCategory("CASH PAYING");
		} else {
			corporateOrPatient = true;
			tariffCategorys = patientService.getTariffCategoryByPatientCategory("CASH PAYING");
			corporate = false;
		}
	}

	@Command("addCpt")
	@NotifyChange("patientInvoiceItems")
	public void addCpt(@BindingParam("patientInvoiceItem") PatientInvoiceItem patientInvoiceItem) {
		if (patientInvoiceItem.getCpt().getId() != null) {
			patientInvoiceItem.setCpt(patientInvoiceItem.getCpt());
			updateUnitPriceAndGrossAmount(patientInvoiceItem);
		}
	}

	@Command("updatePatientInvoice")
	@NotifyChange("patientInvoiceItems")
	public void updatePatientInvoice() {
		patientInvoiceItems.clear();
		for (InvoiceItem ii : dto.getOldInvoice().getInvoiceItems()) {
			if ("Cancel".equals(ii.getInvoiceItemStatus())) {
				cancelledInvoiceItem.add(ii);
				continue;
			}
			if ("OPD_REGISTRATION".equals(ii.getItemType().name())) {
				dto.setFirstTimeResitration(true);
				continue;
			}
			PatientInvoiceItem patientInvoiceItem = new PatientInvoiceItem();
			patientInvoiceItem.setInvoiceType(ii.getItemType().toString());
			if (ii.getCpt() != null && UtilValidator.isNotEmpty(ii.getCpt().getId())) {
				patientInvoiceItem.setCpt(commonCrudService.getById(Cpt.class, ii.getCpt().getId()));
			}
			if (ii.getProduct() != null && UtilValidator.isNotEmpty(ii.getProduct().getId())) {
				patientInvoiceItem.setProduct(commonCrudService.getById(Product.class, ii.getProduct().getId()));
			}
			if (ii.getProvider() != null) {
				selectedProvider = ii.getProvider();
				patientInvoiceItem.setProvider(commonCrudService.getById(Provider.class, selectedProvider.getId()));
				SoapNoteType soapNoteType = commonCrudService.getById(SoapNoteType.class, Long.valueOf("10005"));
				patientInvoiceItem.setSoapNoteType(soapNoteType);
			}

			if (ii.getInvoice().getConsultant() != null) {
				selectedProvider = commonCrudService.getById(Provider.class, ii.getInvoice().getConsultant().getId());
				patientInvoiceItem.setProvider(selectedProvider);
				SoapNoteType soapNoteType = commonCrudService.getById(SoapNoteType.class, Long.valueOf("10005"));
				if (ii.getInvoice().getSchedule() != null) {
					soapNoteType = (SoapNoteType) ii.getInvoice().getSchedule().getVisitType();
				} else {
					String invoiceItemDescription = ii.getDescription();
					if (invoiceItemDescription.contains("PREMIUM"))
						soapNoteType = commonCrudService.getById(SoapNoteType.class, Long.valueOf("10023"));
					if (invoiceItemDescription.contains("TELE"))
						soapNoteType = commonCrudService.getById(SoapNoteType.class, Long.valueOf("10021"));
					if (invoiceItemDescription.contains("HOME"))
						soapNoteType = commonCrudService.getById(SoapNoteType.class, Long.valueOf("10022"));
				}
				patientInvoiceItem.setSoapNoteType(soapNoteType);
			}

			if (ii.getInvoice().getReferralConsultantId() != null) {
				selectedReferral = commonCrudService.getById(Referral.class, ii.getInvoice().getReferralConsultantId());
				final String selectedReferralDoctorFirstName = ii.getInvoice().getReferralDoctorFirstName();
				final String selectedReferralDoctorListName = ii.getInvoice().getReferralDoctorLastName();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							updateClinicDoctor();
						}catch (Exception e){e.printStackTrace();}
						try {
							TenantIdHolder.setTenantId(selectedReferral.getTenantId());
							selectedReferralDoctor = commonCrudService.findByEquality(Provider.class, new String[]{"firstName", "lastName"}, new Object[]{selectedReferralDoctorFirstName, selectedReferralDoctorListName}).get(0);
							TenantIdHolder.setTenantId(Infrastructure.getPractice().getTenantId());
						}catch (Exception e){
							e.printStackTrace();
						}
					}
				}).start();

			}

			if (ii.getInvoice().getTariffCategory() != null){
				for(Map<String,Object> tariffCategory : tariffCategorys){
					if(ii.getInvoice().getTariffCategory().equals(tariffCategory.get("tariffCode"))){
						selectedTariffCatagory = tariffCategory;
					}
				}
				//selectedTariffCatagory = patientService.getTariffCategoryByTariffCode(ii.getInvoice().getTariffCategory());
			}
			patientInvoiceItem.setQuantity(ii.getQuantity());
			updateUnitPriceAndGrossAmount(patientInvoiceItem);
			patientInvoiceItems.add(patientInvoiceItem);
		}
		PatientInvoiceItem patientInvoiceItem = new PatientInvoiceItem();
		patientInvoiceItems.add(patientInvoiceItem);
	}


	@Command("updateClinicDoctor")
	@NotifyChange("referralClinicDoctors")
	public void updateClinicDoctor() {
		TenantIdHolder.setTenantId(selectedReferral.getTenantId());
		referralClinicDoctors = commonCrudService.getAll(Provider.class);
		TenantIdHolder.setTenantId(Infrastructure.getPractice().getTenantId());
	}

	@Command("Save")
	public void Save() throws TransactionException {
		if (dto.getPatient() == null) {
			UtilMessagesAndPopups.showError("Patient cannot be empty");
			return;
		}
		if (UtilValidator.isEmpty(dto.getPatient().getAfyaId())) {
			UtilMessagesAndPopups.showError("Selected patient registration is not complete. Please complete the patient registration process for proceeding.");
			return;
		}
		if ("INSURANCE".equals(dto.getPatient().getPatientType())) {
			if (dto.getHisModuleDto() == null) {
				UtilMessagesAndPopups.showError("Benefit cannot be empty");
				return;
			}
			if (dto.getPatientInsurance() == null && UtilValidator.isEmpty(selectedTariffCatagory)) {
				UtilMessagesAndPopups.showError("Insurance cannot be empty");
				return;
			}

		}
		if (UtilValidator.isEmpty(patientInvoiceItems) || (patientInvoiceItems.get(0).getCpt() == null && patientInvoiceItems.get(0).getProvider() == null)) {
			UtilMessagesAndPopups.showError("Please add services and then Save");
			return;
		}
		for (PatientInvoiceItem patientInvoiceItem : patientInvoiceItems) {
			BigDecimal quantity = patientInvoiceItem.getQuantity();

			if ((quantity == null) || (quantity.compareTo(BigDecimal.ZERO) <= 0) || (new BigDecimal(quantity.intValue()).compareTo(quantity) < 0)) {

				UtilMessagesAndPopups.showError("Quantity Must be a positive integer");
				return;
			}

			if ("OPD_CONSULTATION".equals(patientInvoiceItem.getInvoiceType()) && patientInvoiceItem.getProvider() == null) {
				UtilMessagesAndPopups.showError("Please select Doctor and then Save");
				return;
			}

			if (patientInvoiceItem.getProvider() != null && patientInvoiceItem.getSoapNoteType() != null) {
				List<VisitTypeSoapModule> visitTypeSoapModules = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[] { "provider.id", "slotType.id" }, new Object[] { patientInvoiceItem.getProvider().getId(), patientInvoiceItem.getSoapNoteType().getId() });

				List<BillingDisplayConfig> displayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
				BillingDisplayConfig billingDisplayConfig = null;
				if (UtilValidator.isNotEmpty(displayConfigs)){
					billingDisplayConfig = displayConfigs.get(0);
				}
				for (VisitTypeSoapModule visitTypeSoapModule : visitTypeSoapModules) {
					if ((visitTypeSoapModule.isSmartService()) && ((billingDisplayConfig == null) || (billingDisplayConfig.getAllowIfSmartServiceToBeBookedFromClinic() == null) || (billingDisplayConfig.getAllowIfSmartServiceToBeBookedFromClinic().equals("no")))) {
						UtilMessagesAndPopups.showError("Manual Invoicing for Smart Service is not allowed");
						return;
					}
				}
			}
		}
		BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
		dto.setPatientInvoiceItems(patientInvoiceItems);
		Invoice invoice = generateDirectPatientInvoice(dto, billingDisplayConfig);
		if (invoice != null)
			Executions.getCurrent().sendRedirect("/billing/billingTxnItem.zul?invoiceId=" + invoice.getId());
	}

	private void updateUnitPriceAndGrossAmount(PatientInvoiceItem patientInvoiceItem) {
		if (UtilValidator.isNotEmpty(patientInvoiceItem.getCpt().getId())) {
			patientInvoiceItem.setUnitPrice(getCptPrice(patientInvoiceItem.getCpt(), dto.getPatient()));
			if (patientInvoiceItem.getQuantity() != null) {
				patientInvoiceItem.setGrossAmount(patientInvoiceItem.getUnitPrice().multiply(patientInvoiceItem.getQuantity()));
			}
		} else if (patientInvoiceItem.getProvider() != null) {
			patientInvoiceItem.setSpeciality(null);
			if (UtilValidator.isNotEmpty(patientInvoiceItem.getProvider().getSpecialities()))
				patientInvoiceItem.setSpeciality(patientInvoiceItem.getProvider().getSpecialities().iterator().next());
			patientInvoiceItem.setUnitPrice(getProviderPrice(patientInvoiceItem.getProvider(), dto.getPatient(), patientInvoiceItem.getSoapNoteType()));
			patientInvoiceItem.setGrossAmount(patientInvoiceItem.getUnitPrice().multiply(patientInvoiceItem.getQuantity()));
		}
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

	void updateReferralAmountForService(Invoice invoice, InvoiceItem item, ReferralContract referralContract, String serviceId, BigDecimal amount) {
		if (referralContract == null)
			return;
		com.nzion.domain.ReferralContractService referralContractService = commonCrudService.findUniqueByEquality(com.nzion.domain.ReferralContractService.class, new String[] { "serviceCode", "referralContract.id" }, new Object[] { new Integer(serviceId), referralContract.getId() });
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

	private Invoice generateDirectPatientInvoice(PatientInvoiceDto patientInvoiceDto, BillingDisplayConfig billingDisplayConfig) {

		ReferralContract referralContract = null;

		Patient patient = patientInvoiceDto.getPatient();

		PatientInsurance patientInsurance = patientInvoiceDto.getPatientInsurance();

		if (UtilValidator.isNotEmpty(selectedTariffCatagory))
			patient.setPatientType((String) selectedTariffCatagory.get("patientCategory"));
		if ("INSURANCE".equals(patient.getPatientType()) && (patient.getPatientInsurances() == null || patient.getPatientInsurances().size() == 0)) {
			com.nzion.util.UtilMessagesAndPopups.showError("Insurance details not added for the Patient, Please add and Continue.");
			return null;
		}
		Long itemId = (System.currentTimeMillis() + patient.getId());
		Invoice invoice = null;
		if (patientInvoiceDto.getOldInvoice() != null) {
			commonCrudService.delete(patientInvoiceDto.getOldInvoice().getInvoiceItems());
			invoice = commonCrudService.getById(Invoice.class, patientInvoiceDto.getOldInvoice().getId());
		} else {
			invoice = new Invoice(itemId.toString(), PatientInvoiceDto.class.getName(), null, patient, Infrastructure.getSelectedLocation());
			invoice.setInvoiceType(InvoiceType.OPD);
		}
		invoice.setTotalAmount(new com.nzion.domain.product.common.Money(BigDecimal.ZERO, convertTo()));

		invoice.setConsultant(selectedProvider);

		if (selectedReferral != null) {
			invoice.setReferralConsultantId(selectedReferral.getId());
		}

		if(selectedReferralDoctor != null){
			invoice.setReferralDoctorFirstName(selectedReferralDoctor.getFirstName());
			invoice.setReferralDoctorLastName(selectedReferralDoctor.getLastName());
		}

		List<Contract> selfContracts = commonCrudService.findByEquality(Contract.class, new String[] { "contractType" }, new Object[] { Contract.CONTRACTTYPE.SELF });
		Contract contract = Contract.findCurrentEffectiveContract(selfContracts);
		invoice.setContract(contract);
		Map<String, List<InvoiceItem>> invoiceItemAndServiceMapping = new HashMap<String, List<InvoiceItem>>();

		List<Invoice> invoices = billingService.getFirstInvoice(patient);
		if (UtilValidator.isEmpty(invoices) || dto.isFirstTimeResitration()) {
			InvoiceItem invItem = new InvoiceItem(invoice, itemId.toString(), InvoiceType.OPD_REGISTRATION, InvoiceType.OPD_REGISTRATION.getDescription(), 1, null, PatientInvoiceItem.class.getName());
			if (billingDisplayConfig.getRegistrationFee() != null) {
				invItem.init(billingDisplayConfig.getRegistrationFee(), billingDisplayConfig.getCurrency().getCode(), new Money(billingDisplayConfig.getRegistrationFee(), convertTo()), new Money(billingDisplayConfig.getRegistrationFee(), convertTo()), 0);
				invItem.setCopayAmount(invItem.getGrossAmount());
				invoice.addInvoiceItem(invItem);
				if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(billingDisplayConfig.getRegistrationFee()), convertTo()));
				else
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(billingDisplayConfig.getRegistrationFee(), convertTo()));
			}

		}

		if ((invoice.getReferralConsultantId() != null) && (invoice.getReferralConsultantId() > 0)) {
        	Referral referral = commonCrudService.getById(Referral.class, invoice.getReferralConsultantId());
            //referralContract = commonCrudService.findUniqueByEquality(ReferralContract.class, new String[]{"refereeClinicId"}, new Object[]{ referral.getTenantId() });

			List referralContractList = commonCrudService.findByEquality(ReferralContract.class, new String[]{"refereeClinicId"}, new Object[]{referral.getTenantId()});
			Iterator iterator = referralContractList.iterator();

			while (iterator.hasNext()){
				ReferralContract activeReferralContract = (ReferralContract)iterator.next();
				if ((activeReferralContract.getExpiryDate().after(new Date())) && (activeReferralContract.getContractDate().before(new Date()))){
					referralContract = activeReferralContract;
					break;
				}
			}

            if (referralContract != null) {
                if (!"ACCEPTED".equals(referralContract.getContractStatus())) {
                    referralContract = null;
                }else{
                	invoice.setReferralContract(referralContract);
                }
            }
        }

		List<PatientInvoiceItem> patientInvoiceItemList = patientInvoiceDto.getPatientInvoiceItems();
		LinkedList<PatientInvoiceItem> patientInvoiceItemsWithOutCpt = new LinkedList<PatientInvoiceItem>();
		LinkedList<PatientInvoiceItem> patientInvoiceItems = new LinkedList<PatientInvoiceItem>();
		for(PatientInvoiceItem patientInvoiceItem:patientInvoiceItemList){
			if(patientInvoiceItem.getCpt().getId() != null){
				patientInvoiceItems.add(patientInvoiceItem);
			}else{
				patientInvoiceItemsWithOutCpt.add(patientInvoiceItem);

				BigDecimal amount = BigDecimal.ZERO;
				if ((patientInvoiceItem.getProduct() != null) && ((patientInvoiceItem.getProduct().getSalesPrice() == null) || (patientInvoiceItem.getProduct().getSalesPrice().equals("")))){
					if ((patientInvoiceItem.getProduct().getSalesPrice() != null) && (!patientInvoiceItem.getProduct().getSalesPrice().equals(""))) {
						amount = new BigDecimal(patientInvoiceItem.getProduct().getSalesPrice());
					}
					if (amount.compareTo(BigDecimal.ZERO) <= 0) {
						UtilMessagesAndPopups.showError("Product price not configured");
						return null;
					}
				}
			}
		}
		patientInvoiceItems.addAll(patientInvoiceItemsWithOutCpt);


		for (PatientInvoiceItem patientInvoiceItem : patientInvoiceItems) {

			String productPrice = patientInvoiceItem.getProduct() != null ? patientInvoiceItem.getProduct().getSalesPrice() : "1";
			if((productPrice==null)|| (productPrice.equals("")) || (new BigDecimal(productPrice).compareTo(BigDecimal.ZERO) <= 0) ){
				UtilMessagesAndPopups.showError("Product price not configured");
				return null;
			}

			Cpt cpt = patientInvoiceItem.getCpt();
			if (UtilValidator.isNotEmpty(cpt.getId())) {
				InvoiceItem item = new InvoiceItem(invoice, cpt.getId(), InvoiceType.OPD_PROCEDURE, cpt.getDescription(), patientInvoiceItem.getQuantity().intValue(), null, PatientInvoiceItem.class.getName());
				item.setCpt(cpt);
				item.setItemOrder(2);
				BigDecimal cptPrice = cpt.getPrice();
				// TODO
				String patientCategory = "01";
				String tariffCategory = "00";

				if ("INSURANCE".equals(patient.getPatientType())) {
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

				Map<String, Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", cpt.getId(), null, null, tariffCategory, patientCategory, new Date());
				if (masterPrice == null) {
					masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", cpt.getId(), null, null, "00", "01", new Date());
					if (masterPrice == null){
						if (billingService.checkThruDate("01", "01", "001", cpt.getId(), null, null, "00", "01", new Date())){
							UtilMessagesAndPopups.showError("Thru Date Reached");
							return null;
						}
					}
				}

				if (masterPrice == null) {
					UtilMessagesAndPopups.showError("Procedure price not configured");
					return null;
				}

				if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null)
					cptPrice = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
				Money price = new Money(cptPrice.multiply(BigDecimal.valueOf(patientInvoiceItem.getQuantity().intValue())), convertTo());
				item.init(cptPrice, billingDisplayConfig.getCurrency().getCode(), price, price, 2);

				boolean isMinMaxPriceAvailable = false;
				if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT_MIN") != null) {
					item.setBillableAmountMin(((BigDecimal) masterPrice.get("BILLABLE_AMOUNT_MIN")).multiply(BigDecimal.valueOf(patientInvoiceItem.getQuantity().intValue())));
					isMinMaxPriceAvailable = true;
				}
				if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT_MAX") != null) {
					item.setBillableAmountMax(((BigDecimal) masterPrice.get("BILLABLE_AMOUNT_MAX")).multiply(BigDecimal.valueOf(patientInvoiceItem.getQuantity().intValue())));
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

				String serviceId = billingService.getServiceIdFromMasterPriceConf(cpt.getId(), null, null, tariffCategory, patientCategory);
				if (UtilValidator.isNotEmpty(serviceId)) {
					List invItemList = (List) invoiceItemAndServiceMapping.get(serviceId);
					if (invItemList == null) {
						invItemList = new ArrayList();
					}
					invItemList.add(item);
					invoiceItemAndServiceMapping.put(serviceId, invItemList);
				}
				if ("CASH PAYING".equals(patient.getPatientType()))
					item.setCopayAmount(item.getGrossAmount());

				if (UtilValidator.isNotEmpty( cpt.getId() )) {
                    updateReferralAmountForService(invoice, item, referralContract, cpt.getId(), cptPrice.multiply(item.getQuantity()));
                }

				invoice.addInvoiceItem(item);

				Cpt newCpt = commonCrudService.getById(Cpt.class, cpt.getId());
				Set<ProductBillOfMaterial> productBillOfMaterials = newCpt.getProductBillOfMaterials();

                for(ProductBillOfMaterial productBillOfMaterial : productBillOfMaterials){
                	productService.reduceProductInventory(productBillOfMaterial.getProduct(), productBillOfMaterial.getQuantity().multiply(item.getQuantity()));
                }

				if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(cptPrice.multiply(item.getQuantity())), convertTo()));
				else
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(cptPrice.multiply(item.getQuantity()), convertTo()));

			}

			// Calculate consultation charges
			Provider provider = patientInvoiceItem.getProvider();
			if (provider != null && (patientInvoiceItem.getInvoiceType().contains("CONSULTATION"))) {
				itemId = (System.currentTimeMillis() + patient.getId());

				itemId = (System.currentTimeMillis() + patient.getId());

				SlotType slotType = patientInvoiceItem.getSoapNoteType();
				String invoiceItemDescription = InvoiceType.OPD_CONSULTATION.getDescription() + " - " + slotType.getName();

				if (invoiceItemDescription.contains("Premium"))
					invoiceItemDescription = "Premium Consultation Afya Smart Service";
				if (invoiceItemDescription.contains("Tele"))
					invoiceItemDescription = "Tele Consultation Afya Smart Service";
				if (invoiceItemDescription.contains("Home"))
					invoiceItemDescription = "Home Visit Afya Smart Service";

				InvoiceItem consultationItem = new InvoiceItem(invoice, itemId.toString(), InvoiceType.OPD_CONSULTATION, invoiceItemDescription, 1, null, PatientInvoiceItem.class.getName());

				consultationItem.setProvider(provider);
				consultationItem.setItemOrder(1);

				BigDecimal amount = BigDecimal.ZERO;

				String patientCategory = "01";
				String tariffCategory = "00";
				if ("INSURANCE".equals(patient.getPatientType())) {
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

				Map<String, Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, slotType.getId().toString(), patientInvoiceItem.getProvider().getId().toString(), tariffCategory, patientCategory, new Date());
				if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null)
					amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");

				VisitTypeSoapModule visitTypeSoapModule = commonCrudService.findUniqueByEquality(VisitTypeSoapModule.class, new String[] { "provider", "slotType" }, new Object[] { patientInvoiceItem.getProvider(), slotType });
				if (visitTypeSoapModule.isVisitPolicy())
					amount = updateFollowUpCharges(patient, amount, patientInvoiceItem.getProvider(), slotType.getName(), tariffCategory, patientCategory);

				if (amount.compareTo(BigDecimal.ZERO) < 0) {
					masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10005", patientInvoiceItem.getProvider().getId().toString(), tariffCategory, patientCategory, new Date());
					if (masterPrice == null) {
						masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10005", patientInvoiceItem.getProvider().getId().toString(), "00", "01", new Date());
					}
					if (masterPrice != null) {
						amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
					}
				}

				//new code start
				if( (invoice.getSchedule() != null) && (invoice.getSchedule().isMobileOrPatinetPortal())) {
					RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
					RCMPreference.RCMVisitType rcmVisitType = null;
					if (slotType.getName().equals("Premium Visit"))
						rcmVisitType = RCMPreference.RCMVisitType.PREMIUM_APPOINTMENT;
					if (slotType.getName().equals("Home Visit"))
						rcmVisitType = RCMPreference.RCMVisitType.HOME_VISIT_APPOINTMENT;
					if (slotType.getName().equals("Tele Consultation Visit"))
						rcmVisitType = RCMPreference.RCMVisitType.TELE_CONSULT_APPOINTMENT;
					if (slotType.getName().equals("Consult Visit") && invoice.getSchedule().isFromMobileApp())
						rcmVisitType = RCMPreference.RCMVisitType.CONSULT_VISIT;

					SchedulingPreference schedulingPreference = commonCrudService.findUniqueByEquality(SchedulingPreference.class,
							new String[]{"rcmPreference", "visitType"}, new Object[]{rcmPreference, rcmVisitType});
					BigDecimal convenienceFee = schedulingPreference != null ? schedulingPreference.getConvenienceFee() : BigDecimal.ZERO;
					amount = amount.add(convenienceFee);
				}
				//new code end

				consultationItem.init(amount, billingDisplayConfig.getCurrency().getCode(), new Money(amount, convertTo()), new Money(amount, convertTo()), 1);

				boolean isMinMaxPriceAvailable = false;

				if (masterPrice == null) {
					if (billingService.checkThruDate("01", "01", "001", null, "10005", patientInvoiceItem.getProvider().getId().toString(), "00", "01", new Date())) {
						UtilMessagesAndPopups.showError("Thru Date Reached");
						return null;
					}
				}

				if (masterPrice == null) {
					UtilMessagesAndPopups.showError("Consultation price not configured");
					return null;
				}

				if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT_MIN") != null) {
					consultationItem.setBillableAmountMin((BigDecimal) masterPrice.get("BILLABLE_AMOUNT_MIN"));
					isMinMaxPriceAvailable = true;
				}
				if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT_MAX") != null) {
					consultationItem.setBillableAmountMax((BigDecimal) masterPrice.get("BILLABLE_AMOUNT_MAX"));
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

				String serviceId = billingService.getServiceIdFromMasterPriceConf(null, patientInvoiceItem.getSoapNoteType().getId().toString(), patientInvoiceItem.getProvider().getId().toString(), tariffCategory, patientCategory);
				if (UtilValidator.isNotEmpty(serviceId)) {
					List invItemList = (List) invoiceItemAndServiceMapping.get(serviceId);
					if (invItemList == null) {
						invItemList = new ArrayList();
					}
					invItemList.add(consultationItem);
					invoiceItemAndServiceMapping.put(serviceId, invItemList);
				}

				if ("CASH PAYING".equals(patient.getPatientType()))
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

			Product product = patientInvoiceItem.getProduct();
			if ((product != null) && UtilValidator.isNotEmpty(product.getId())) {
				itemId = (System.currentTimeMillis() + patient.getId());

				itemId = (System.currentTimeMillis() + patient.getId());

				String invoiceItemDescription = "PRODUCT - "+product.getTradeName();

				InvoiceItem productItem = new InvoiceItem(invoice, itemId.toString(), InvoiceType.OPD_PRODUCT, invoiceItemDescription, 1, null, PatientInvoiceItem.class.getName());
				productItem.setProduct(product);
				BigDecimal amount = BigDecimal.ZERO;
				if ((product.getSalesPrice() != null) && (!product.getSalesPrice().equals(""))){
					amount = new BigDecimal(product.getSalesPrice());
				}
				if (amount.compareTo(BigDecimal.ZERO) <= 0) {
					UtilMessagesAndPopups.showError("Product price not configured");
					return null;
				}
				Money price = new Money(amount.multiply(patientInvoiceItem.getQuantity()));
				productItem.init(amount, billingDisplayConfig.getCurrency().getCode(), price, price, 2);
				productItem.setQuantity(patientInvoiceItem.getQuantity());
				invoice.addInvoiceItem(productItem);

				productService.reduceProductInventory(product, productItem.getQuantity());

				if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(amount.multiply(productItem.getQuantity())), convertTo()));
				else
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(amount.multiply(productItem.getQuantity()), convertTo()));

			}
		}
		invoice.setInsuranceStatus(null);

		if ("INSURANCE".equals(patient.getPatientType())) {
			if (UtilValidator.isNotEmpty(patient.getPatientInsurances())) {
				invoice.setSelectedHisModuleId(patientInvoiceDto.getHisModuleDto().getHisModuleId());
				invoice.setPatientInsuranceId(patientInsurance.getId());
				CoPayment coPayment = AfyaServiceConsumer.getServiceOrModuleDataByServiceId(patientInvoiceDto.getHisModuleDto().getHisModuleId(), patientInsurance.getBenefitId(), patientInsurance.getGroupId(), invoiceItemAndServiceMapping.keySet());
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

							// Override Copay Amount
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
							if (copaymentDetail.getAuthorization() && !INSURANCESTATUS.PENDING_APPROVAL.equals(invoice.getInsuranceStatus())) {
								invoice.setInsuranceStatus(INSURANCESTATUS.PRE_APPROVED);
							} else {
								invoice.setInsuranceStatus(INSURANCESTATUS.PENDING_APPROVAL);
							}
						}
					}
				}

			}

		}

		for (InvoiceItem ii : cancelledInvoiceItem) {
			ii.setId(null);
			invoice.addInvoiceItem(ii);
		}
		if (UtilValidator.isNotEmpty(selectedTariffCatagory)){
			invoice.setTariffCategory((String)selectedTariffCatagory.get("tariffCode"));
		}

		if (invoice.getInvoicePayments() != null) {
			BigDecimal totalInvAmount = BigDecimal.ZERO;
			BigDecimal totalPaid = BigDecimal.ZERO;
			for (InvoiceItem ii : invoice.getInvoiceItems()) {
				totalInvAmount = totalInvAmount.add(ii.getNetPrice());
			}

			for (InvoicePayment ip : invoice.getInvoicePayments()) {
				totalPaid = totalPaid.add(ip.getAmount().getAmount());
			}

			if (totalPaid.compareTo(totalInvAmount) < 0) {
				invoice.setInvoiceStatus(InvoiceStatusItem.INPROCESS.toString());
			} else {
				invoice.setInvoiceStatus(InvoiceStatusItem.RECEIVED.toString());
			}
		}

		commonCrudService.save(invoice);
		/*
		 * try { billingService.doTransactions(soapNote, invoice); } catch
		 * (TransactionException e) { e.printStackTrace(); }
		 */
		return invoice;
	}

	private BigDecimal updateFollowUpCharges(Patient patient, BigDecimal amount, Provider provider, String visitName, String tariffCategory, String patientCategory) {
		Integer followUpVisitDays = null;
		Integer followUpVisits = null;
		BigDecimal followUpVisitCharges = null;
		Map<String, Object> masterPrice = null;
		List<Invoice> invoices = commonCrudService.findByEquality(Invoice.class, new String[] { "patient" }, new Object[] { patient });
		// Revisit
		followUpVisitDays = provider.getRevisitDays();
		followUpVisits = provider.getRevisitVisits();
		followUpVisitCharges = provider.getRevisitCharges();
		masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10024", provider.getId().toString(), tariffCategory, patientCategory, new Date());

		if (invoices != null && invoices.size() != 0 && invoices.size() <= followUpVisits) {
			if (invoices.size() <= followUpVisitDays) {
				if (masterPrice != null) {
					amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
				} else {
					amount = followUpVisitCharges;
				}
			}
		}

		// Followup Visit

		masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10010", provider.getId().toString(), tariffCategory, patientCategory, new Date());

		followUpVisitDays = provider.getFollowUpVisitDays();
		followUpVisits = provider.getFollowUpVisits();
		followUpVisitCharges = provider.getFollowUpVisitCharges();

		if (invoices != null && invoices.size() != 0 && invoices.size() <= followUpVisits) {
			if (invoices.size() <= followUpVisitDays) {
				if (masterPrice != null) {
					amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
				} else {
					amount = followUpVisitCharges;
				}
			}
		}

		// Free Visit
		followUpVisitDays = provider.getFreeVisitDays();
		followUpVisits = provider.getFreeVisits();
		followUpVisitCharges = provider.getFreeVisitCharges();
		Invoice lastInvoice = UtilValidator.isNotEmpty(invoices) ? invoices.get(invoices.size() - 1) : null;

		if (lastInvoice != null && lastInvoice.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) > 0 && followUpVisitDays > 0 && followUpVisits > 0) {
			amount = followUpVisitCharges;
		} else if (lastInvoice != null && lastInvoice.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) == 0) {

			Integer count = 1;
			Invoice lasInv = null;
			for (int i = invoices.size() - 1; i >= 0; i--) {
				Invoice inv = invoices.get(i);
				if (inv.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) == 0) {
					lasInv = inv;
					count++;
				} else {
					break;
				}
			}
			if (count <= followUpVisits) {
				amount = followUpVisitCharges;
			}

		}

		return amount;
	}

	public Currency convertTo() {
		BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
		String currency = billingDisplayConfig.getCurrency().getCode();
		Currency defaultCurrency = Currency.getInstance(currency);
		return defaultCurrency;
	}

	public List<Provider> getProviderList() {
		return providerList;
	}

	public void setProviderList(List<Provider> providerList) {
		this.providerList = providerList;
	}

	public List<Referral> getReferralList() {
		return referralList;
	}

	public void setReferralList(List<Referral> referralList) {
		this.referralList = referralList;
	}

	public BillingService getBillingService() {
		return billingService;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public Window getNewInvoiceWin() {
		return newInvoiceWin;
	}

	public void setNewInvoiceWin(Window newInvoiceWin) {
		this.newInvoiceWin = newInvoiceWin;
	}

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public PatientInvoiceDto getDto() {
		return dto;
	}

	public void setDto(PatientInvoiceDto dto) {
		this.dto = dto;
	}

	public List<PatientInvoiceItem> getPatientInvoiceItems() {
		return patientInvoiceItems;
	}

	public void setPatientInvoiceItems(List<PatientInvoiceItem> patientInvoiceItems) {
		this.patientInvoiceItems = patientInvoiceItems;
	}

	public List getModuleDetailsDtos() {
		return moduleDetailsDtos;
	}

	public void setModuleDetailsDtos(List moduleDetailsDtos) {
		this.moduleDetailsDtos = moduleDetailsDtos;
	}

	public ScheduleService getScheduleService() {
		return scheduleService;
	}

	public void setScheduleService(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	public List<SoapNoteType> getSopenNoteTypes() {
		return sopenNoteTypes;
	}

	public void setSopenNoteTypes(List<SoapNoteType> sopenNoteTypes) {
		this.sopenNoteTypes = sopenNoteTypes;
	}

	public List<Invoice> getOldInvoices() {
		return oldInvoices;
	}

	public void setOldInvoices(List<Invoice> oldInvoices) {
		this.oldInvoices = oldInvoices;
	}

	public List<Cpt> getCptList() {
		return cptList;
	}

	public void setCptList(List<Cpt> cptList) {
		this.cptList = cptList;
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

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public List<Map<String, Object>> getTariffCategorys() {
		return tariffCategorys;
	}

	public void setTariffCategorys(List<Map<String, Object>> tariffCategorys) {
		this.tariffCategorys = tariffCategorys;
	}

	public boolean isCorporateOrPatient() {
		return corporateOrPatient;
	}

	public void setCorporateOrPatient(boolean corporateOrPatient) {
		this.corporateOrPatient = corporateOrPatient;
	}

	public boolean isCorporate() {
		return corporate;
	}

	public void setCorporate(boolean corporate) {
		this.corporate = corporate;
	}

	public Provider getSelectedProvider() {
		return selectedProvider;
	}

	public void setSelectedProvider(Provider selectedProvider) {
		this.selectedProvider = selectedProvider;
	}

	public Referral getSelectedReferral() {
		return selectedReferral;
	}

	public void setSelectedReferral(Referral selectedReferral) {
		this.selectedReferral = selectedReferral;
	}

	public List<Provider> getReferralClinicDoctors() {
		return referralClinicDoctors;
	}

	public void setReferralClinicDoctors(List<Provider> referralClinicDoctors) {
		this.referralClinicDoctors = referralClinicDoctors;
	}

	public Provider getSelectedReferralDoctor() {
		return selectedReferralDoctor;
	}

	public void setSelectedReferralDoctor(Provider selectedReferralDoctor) {
		this.selectedReferralDoctor = selectedReferralDoctor;
	}


}
