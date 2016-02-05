package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.BodySite;

@Component
public class BodySiteResourceProvider extends BaseResourceProvider<BodySite> {
	public BodySiteResourceProvider() {
		super(BodySite.class);
	}
}
