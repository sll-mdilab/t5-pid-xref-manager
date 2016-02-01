package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.Device;

@Component
public class DeviceResourceProvider extends BaseResourceProvider<Device> {
	public DeviceResourceProvider() {
		super(Device.class);
	}
}
