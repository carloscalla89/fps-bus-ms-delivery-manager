package com.inretailpharma.digital.deliverymanager.canonical.manager;

public class ShoppingCartStatusReasonCanonical {

	private String id;
	private String reason;
	private String customNote;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getCustomNote() {
		return customNote;
	}

	public void setCustomNote(String customNote) {
		this.customNote = customNote;
	}

	@Override
	public String toString() {
		return "ShoppingCartStatusReasonCanonical{" +
			"id='" + id + '\'' +
			", reason='" + reason + '\'' +
			", customNote='" + customNote + '\'' +
			'}';
	}
}

