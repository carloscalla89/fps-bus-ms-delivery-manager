package com.inretailpharma.digital.deliverymanager.canonical.manager;

public class ShoppingCartStatusCanonical {

	private Long id;
	private Long statusId;
	private ShoppingCartStatusReasonCanonical reason;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getStatusId() {
		return statusId;
	}

	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}

	public ShoppingCartStatusReasonCanonical getReason() {
		return reason;
	}

	public void setReason(ShoppingCartStatusReasonCanonical reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		return "ShoppingCartStatusCanonical{" +
			"id=" + id +
			", statusId=" + statusId +
			", reason=" + reason +
			'}';
	}
}
