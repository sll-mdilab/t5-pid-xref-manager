package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.Organization;

@Component
public class OrganizationResourceProvider extends BaseResourceProvider<Organization> {

	public OrganizationResourceProvider() {
		super(Organization.class);
	}

}
