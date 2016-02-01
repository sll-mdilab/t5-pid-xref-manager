package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;
import ca.uhn.fhir.model.dstu2.resource.Order;

@Component
public class OrderResourceProvider extends BaseResourceProvider<Order> {

	public OrderResourceProvider() {
		super(Order.class);
	}
}
