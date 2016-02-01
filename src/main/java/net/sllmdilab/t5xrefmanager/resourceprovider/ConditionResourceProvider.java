package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.Condition;

@Component
public class ConditionResourceProvider extends BaseResourceProvider<Condition> {

	public ConditionResourceProvider() {
		super(Condition.class);
	}
}
