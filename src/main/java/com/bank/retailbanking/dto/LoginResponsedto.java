package com.bank.retailbanking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponsedto {
	private String message;
	private Integer statusCode;
	private Long customerId;
}
