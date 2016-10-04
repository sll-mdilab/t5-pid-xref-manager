package net.sllmdilab.t5xrefmanager.cdshooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Coding {

	private String system;
	private String code;

	public String getSystem() {
		return this.system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	// @Override
	// public String toString() {
	// return "Value{" +
	// "id=" + id +
	// ", quote='" + quote + '\'' +
	// '}';
	// }
}
