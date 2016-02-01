package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.Medication;

@Component
public class MedicationResourceProvider extends BaseResourceProvider<Medication> {
	public MedicationResourceProvider() {
		super(Medication.class);
	}
}
