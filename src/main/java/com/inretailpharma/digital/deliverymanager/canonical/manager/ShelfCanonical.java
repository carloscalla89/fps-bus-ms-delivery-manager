package com.inretailpharma.digital.deliverymanager.canonical.manager;

import java.io.Serializable;

import lombok.Data;

@Data
public class ShelfCanonical implements Serializable {

	private String lockCode;
	private String packCode;
}
