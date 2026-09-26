package com.flashledger.flashledgerengine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OrderDetailsDTO {
    private int id;
    private int userId;
    private int productId;
    private String txnRefId;
}
