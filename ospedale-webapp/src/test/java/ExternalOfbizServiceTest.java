import com.nzion.external.ExternalServiceClientImpl;
import com.nzion.external.PrescriptionDTO;
import com.nzion.external.PrescriptionLineItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pradyumna on 04-04-2015.
 */
public class ExternalOfbizServiceTest {

    @Test
    public void callOfbizService(){
        ExternalServiceClientImpl client=new ExternalServiceClientImpl();
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setClinicId("100000");
        dto.setAfyaId("20000");
        dto.setClinicName("Clinic Name");
        dto.setDoctorName("Doctor Name");
        dto.setFirstName("FirstName");
        dto.setLastName("LastName");
        dto.setMobile("9343044175");
        dto.setPatientType("CASH");
        List<PrescriptionLineItem> rows = new ArrayList<PrescriptionLineItem>();
        /*rows.add(new PrescriptionLineItem("Curam Tablets 1gm 14 Tab",new BigDecimal(10),
                "1-0-1 After Food",true,"Paracetamol"));
        rows.add(new PrescriptionLineItem("Enhancin Tablets 1gm 12 Tab",new BigDecimal(10),
                "1-1-1 Before Food",false,"Paracetamol"));*/
        dto.setRows(rows);

        /*GenericEventMessage<PrescriptionDTO> eventMessage = new GenericEventMessage<PrescriptionDTO>(dto);
        Map metadata = new HashMap();
        metadata.put("tenantId","TENANT1");
        eventMessage=eventMessage.withMetaData(metadata);
        client.handlePrescriptionOrder(eventMessage);*/
    }
}
