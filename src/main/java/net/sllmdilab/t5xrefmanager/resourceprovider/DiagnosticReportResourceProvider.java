package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;

@Component
public class DiagnosticReportResourceProvider extends BaseResourceProvider<DiagnosticReport> {
	public DiagnosticReportResourceProvider() {
		super(DiagnosticReport.class);
	}
}
