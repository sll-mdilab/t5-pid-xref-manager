package net.sllmdilab.t5xrefmanager.resource;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.DeviceUseStatement;

@ResourceDef(name="DeviceUseStatement")
public class T5DeviceUseStatement extends DeviceUseStatement {
	
	private static final long serialVersionUID = 1L;
	
	public static final String EXTENSION_URL_ISSUER = "http://sll-mdilab.net/fhir/DeviceUseStatement#issuer";
	public static final String EXTENSION_URL_BROKERING_RECEIVERS = "http://sll-mdilab.net/fhir/DeviceUseStatement#brokeringReceivers";
	
	public static final String SP_PERIOD = "-period";
	
	@Child(name="issuer") 
    @Extension(url=EXTENSION_URL_ISSUER, definedLocally=true, isModifier=false)
    @Description(shortDefinition="The practitioner having issued this statement.")
    private ResourceReferenceDt myIssuer;
	
	@Child(name="brokeringReceivers", max=Child.MAX_UNLIMITED)
	@Extension(url=EXTENSION_URL_BROKERING_RECEIVERS, definedLocally=true, isModifier=false)
	@Description(shortDefinition="Receivers for HL7 brokering.")
	private List<ResourceReferenceDt> brokeringReceivers;

	public ResourceReferenceDt getIssuer() {
		if(myIssuer == null) {
			myIssuer = new ResourceReferenceDt();
		}
		return myIssuer;
	}
	
	public void setIssuer(ResourceReferenceDt issuer) {
		this.myIssuer = issuer;
	}

	public List<ResourceReferenceDt> getBrokeringReceivers() {
		if(brokeringReceivers == null) {
			brokeringReceivers = new ArrayList<ResourceReferenceDt>();
		}
		
		return brokeringReceivers;
	}

	public void setBrokeringReceivers(List<ResourceReferenceDt> receivers) {
		this.brokeringReceivers = receivers;
	}
}
