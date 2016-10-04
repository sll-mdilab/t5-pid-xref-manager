package net.sllmdilab.t5xrefmanager.cdshooks;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Context {

	private String resourceType;
	private UUID id;
	private Code code;
	private Subject subject;
	private String effectiveDateTime;
	private ValueQuantity valueQuantity;	

	public Context() {
	}

	public String getResourceType() {
		return this.resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Code getCode() {
		return code;
	}

	public void setCode(Code code) {
		this.code = code;
	}

	public Subject getSubject() {
		return this.subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public String getEffectiveDateTime() {
		return this.effectiveDateTime;
	}

	public void setEffectiveDateTime(String effectiveDateTime) {
		this.effectiveDateTime = effectiveDateTime;
	}

	public ValueQuantity getValueQuantity() {
		return this.valueQuantity;
	}

	public void setValueQuantity(ValueQuantity valueQuantity) {
		this.valueQuantity = valueQuantity;
	}
}