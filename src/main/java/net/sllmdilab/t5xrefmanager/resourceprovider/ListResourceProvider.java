package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.ListResource;

@Component
public class ListResourceProvider extends BaseResourceProvider<ListResource> {

	public ListResourceProvider() {
		super(ListResource.class);
	}
}
