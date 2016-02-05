package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.Appointment;

@Component
public class AppointmentResourceProvider extends BaseResourceProvider<Appointment> {
	public AppointmentResourceProvider() {
		super(Appointment.class);
	}
}
