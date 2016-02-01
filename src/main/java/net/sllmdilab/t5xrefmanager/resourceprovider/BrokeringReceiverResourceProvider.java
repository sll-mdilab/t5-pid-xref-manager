package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import net.sllmdilab.t5xrefmanager.resource.BrokeringReceiver;

@Component
public class BrokeringReceiverResourceProvider extends BaseResourceProvider<BrokeringReceiver> {
	
	public BrokeringReceiverResourceProvider() {
		super(BrokeringReceiver.class);
	}
	
}
