package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.Practitioner;

@Component
public class PractitionerResourceProvider extends BaseResourceProvider<Practitioner> {
	public PractitionerResourceProvider() {
		super(Practitioner.class);
	}
}
