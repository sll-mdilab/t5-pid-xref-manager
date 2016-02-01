package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;
import ca.uhn.fhir.model.dstu2.resource.DeviceMetric;

@Component
public class DeviceMetricResourceProvider extends BaseResourceProvider<DeviceMetric> {

	public DeviceMetricResourceProvider() {
		super(DeviceMetric.class);
	}
}
