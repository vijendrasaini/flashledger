package com.flashledger.flashledgerengine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class OrderDetailsDTO implements Serializable {
    private static final Long serialVersionUID = 1L;

    private int id;
    private int userId;
    private int productId;
    private String txnRefId;
}
