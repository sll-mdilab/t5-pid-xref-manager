package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.Procedure;

@Component
public class ProcedureResourceProvider extends BaseResourceProvider<Procedure> {
	public ProcedureResourceProvider() {
		super(Procedure.class);
	}
}
