package com.nzion.external;

import com.nzion.dto.OrderDto;

/**
 * Created by Mohan Sharma on 4/7/2015.
 */
public interface ExternalServiceClient {
	OrderDto handlePrescriptionOrder(PrescriptionDTO prescriptionDTO, String tenantId);

    void handleLabOrder(LabOrdetDto labOrdetDto, String pharmacyTenantId);
    
    String orderPayment(String orderId,String totalAmount, String pharmacyTenanId);
    
    String completeOrder(String orderId,String pharmacyTenanId);
    
    String cancelOrder(String orderId,String pharmacyTenanId);
}
