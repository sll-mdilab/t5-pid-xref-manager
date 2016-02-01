package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.ClinicalImpression;

@Component
public class ClinicalImpressionResourceProvider extends BaseResourceProvider<ClinicalImpression> {
	public ClinicalImpressionResourceProvider() {
		super(ClinicalImpression.class);
	}
}
