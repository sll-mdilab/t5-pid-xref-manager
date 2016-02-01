package net.sllmdilab.t5xrefmanager.resource;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.model.api.IElement;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;

@ResourceDef(name="BrokeringReceiver")
public class BrokeringReceiver extends BaseResource {

	@Child(name="systemName", type=StringDt.class) 
    @Description(shortDefinition="Name of the system.")
    private StringDt systemName;
	
	@Child(name="address", type=StringDt.class) 
    @Description(shortDefinition="IP or hostname of the system.")
    private StringDt address;
	
	@Child(name="port", type=IntegerDt.class) 
    @Description(shortDefinition="MLLP port of the system.")
    private IntegerDt port;

	public StringDt getSystemName() {
		if(systemName == null) {
			systemName = new StringDt();
		}
		return systemName;
	}
	
	public void setSystemName(StringDt systemName) {
		this.systemName = systemName;
	}

	public StringDt getAddress() {
		if(address == null) {
			address = new StringDt();
		}
		
		return address;
	}

	public void setAddress(StringDt address) {
		this.address = address;
	}

	public IntegerDt getPort() {
		if(port == null) { 
			port = new IntegerDt();
		}
		
		return port;
	}

	public void setPort(IntegerDt port) {
		this.port = port;
	}

	@Override
	public String getResourceName() {
		return "BrokeringReceiver";
	}

	@Override
	public FhirVersionEnum getStructureFhirVersionEnum() {
		return FhirVersionEnum.DSTU2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IElement> List<T> getAllPopulatedChildElementsOfType(Class<T> clazz) {
		List<T> result = new ArrayList<T>();
		
		if(clazz == StringDt.class) {
			if(systemName != null && !systemName.isEmpty()) {
				result.add((T) systemName);
			}
			
			if(address != null && !address.isEmpty()) {
				result.add((T) address);
			}
		} else if (clazz == IntegerDt.class) {
			if(port != null && !port.isEmpty()) {
				result.add((T) port);
			}
		}
		
		return result;
	}

	@Override
	public boolean isEmpty() {
		return systemName == null || systemName.isEmpty();
	}
}
