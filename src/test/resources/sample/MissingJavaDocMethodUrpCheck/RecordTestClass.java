package com.emirates.ocsl.paymentorder.paymentprocess.v1.context;

import com.emirates.ocsl.cpg.client.bean.response.CpgOrderResponseBean;
import com.emirates.ocsl.paymentorder.paymentprocess.v1.milespayment.loyaltyredemption.integration.records.RedemptionResponseDTO;
import com.emirates.ocsl.paymentorder.paymentprocess.v1.milespayment.upgraderedemption.integration.records.UpgradeRedemptionResponseDTO;
import com.emirates.ocsl.paymentorder.paymentprocess.v1.userprofile.integration.userprofile.bean.UserProfileResponseDTO;
import com.emirates.ocsl.shared.dto.paymentorder.v1.OrderDTO;
import com.emirates.ocsl.shared.dto.paymentorder.v1.UpdateOrderDTO;
import java.util.List;
import lombok.Builder;
import org.springframework.http.HttpHeaders;

@Builder(toBuilder = true)
public record PaymentContext(
    String orderId,
    HttpHeaders httpHeaders,
    OrderDTO orderDTO,
    CpgOrderResponseBean cpgOrderResponse,
    UpgradeRedemptionResponseDTO upgradeRedemptionResponseDTO,
    UpdateOrderDTO updateOrderDTO,
    RedemptionResponseDTO redemptionResponseDTO,
    List<RedemptionResponseDTO> redemptionResponses,
    UserProfileResponseDTO userProfileResponseDTO) {

  private void testThis() {
    System.out.println("sfsdfas");
  }
}
