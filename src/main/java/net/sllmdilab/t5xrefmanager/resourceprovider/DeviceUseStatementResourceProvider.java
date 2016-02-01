package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Device;
import ca.uhn.fhir.model.dstu2.resource.DeviceUseStatement;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;
import net.sllmdilab.t5xrefmanager.resource.T5DeviceUseStatement;

@Component
public class DeviceUseStatementResourceProvider extends BaseResourceProvider<T5DeviceUseStatement> {
	
	public DeviceUseStatementResourceProvider() {
		super(T5DeviceUseStatement.class);
	}

	@Search
	public List<IResource> search(
			@OptionalParam(name = DeviceUseStatement.SP_PATIENT, targetTypes = {
					Patient.class }) ReferenceParam patient,
			@OptionalParam(name = DeviceUseStatement.SP_SUBJECT, targetTypes = {
					Patient.class }) ReferenceParam subject,
			@RequiredParam(name = "end:missing") StringParam whenUsedEnd) {

		Params params = Params.empty();

		if (patient != null) {
			params.add(DeviceUseStatement.SP_PATIENT, patient.toTokenParam().getValue());
		} else if (subject != null) {
			params.add(DeviceUseStatement.SP_SUBJECT, subject.toTokenParam().getValue());
		} else {
			throw new InvalidRequestException("subject or patient parameters must be present.");
		}

		params.add("end:missing", whenUsedEnd.getValue());

		return getResourceDao().search(params);
	}

	@Search
	public List<IResource> search(
			@RequiredParam(name = Observation.SP_DEVICE, targetTypes = {
					Device.class }) ReferenceParam device,
			@OptionalParam(name = T5DeviceUseStatement.SP_PERIOD) DateRangeParam dateRange) {

		Date start = T5FHIRUtils.getStartTimeFromNullableRange(dateRange);
		Date end = T5FHIRUtils.getEndTimeFromNullableRange(dateRange);
		
		return getResourceDao().search(
				Params.of("device", device.toTokenParam().getValue())
				.add(T5DeviceUseStatement.SP_PERIOD, "le", end)
				.add(T5DeviceUseStatement.SP_PERIOD, "ge", start));
	}
}
