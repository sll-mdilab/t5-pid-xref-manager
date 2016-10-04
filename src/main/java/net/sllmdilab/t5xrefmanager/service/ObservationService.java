package net.sllmdilab.t5xrefmanager.service;

import static net.sllmdilab.t5xrefmanager.resourceprovider.ObservationResourceProvider.MODIFIER_MISSING;
import static net.sllmdilab.t5xrefmanager.resourceprovider.ObservationResourceProvider.SP_METHOD;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.DeviceUseStatement;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import net.sllmdilab.commons.exceptions.T5Exception;
import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.t5xrefmanager.converter.SqlObservationToFhirConverter;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;
import net.sllmdilab.t5xrefmanager.dao.ObservationSqlDao;
import net.sllmdilab.t5xrefmanager.dao.ObservationSqlDao.Code;
import net.sllmdilab.t5xrefmanager.resource.T5DeviceUseStatement;
import net.sllmdilab.t5xrefmanager.util.TimeShift;
import net.sllmdilab.t5xrefmanager.util.TimeShift.Interval;

public class ObservationService {
	@Autowired
	private FhirContext fhirContext;

	@Autowired
	private ObservationSqlDao observationDao;

	@Autowired
	private SqlObservationToFhirConverter obsConverter;

	@Autowired
	private FhirbaseResourceDao<T5DeviceUseStatement> deviceUseStatementDao;

	@Autowired
	private FhirbaseResourceDao<Observation> fhirbaseObservationDao;

	private final Interval masterInterval;

	private final List<String> patientIds;

	private enum IdType {
		PATIENT, DEVICE
	}

	/**
	 * Limit the given list of observations to a specified approximate sample
	 * interval. At most one sample will be returned within each interval.
	 * 
	 * No observations are modified. For each interval, the last observation (in
	 * time) within that interval will be returned. No guarantee of the order of
	 * observations is made.
	 * 
	 * @param observations
	 *            A list of observations.
	 * @param start
	 *            Start of the interval for which observations should be
	 *            returned.
	 * @param end
	 *            End of the interval for which observations should be returned.
	 * @param samplingPeriodMilli
	 *            Approximate number of milliseconds between samples. A value of
	 *            0 means this method will just return the original list.
	 * @return A filtered list of observations containing a subset of those
	 *         supplied in the observations parameter.
	 */
	private List<Observation> limitSamplingPeriod(List<Observation> observations, Date start, Date end,
			int samplingPeriodMilli) {
		if (samplingPeriodMilli == 0) {
			return observations;
		}

		observations.sort(new ObservationTimestampComparator());

		List<Observation> result = new ArrayList<>();
		long firstIntervalEnd = (start.toInstant().toEpochMilli() / samplingPeriodMilli + 1) * samplingPeriodMilli;
		for (long currentIntervalEnd = firstIntervalEnd; currentIntervalEnd <= end.toInstant()
				.toEpochMilli(); currentIntervalEnd += samplingPeriodMilli) {
			Observation nearestObs = findNearestObservation(observations, currentIntervalEnd);
			if (nearestObs != null && isAfterPeriodStart(samplingPeriodMilli, currentIntervalEnd, nearestObs)) {
				result.add(nearestObs);
			}
		}

		return result;
	}

	private boolean isAfterPeriodStart(int samplingPeriodMilli, long currentIntervalEnd, Observation nearestObs) {
		return getTimestamp(nearestObs.getEffective()) >= (currentIntervalEnd - samplingPeriodMilli);
	}

	private static class ObservationTimestampComparator implements Comparator<Observation> {
		private final SortOrderEnum sortOrder;

		public ObservationTimestampComparator() {
			sortOrder = SortOrderEnum.ASC;
		}

		public ObservationTimestampComparator(SortOrderEnum sortOrder) {
			this.sortOrder = sortOrder;
		}

		@Override
		public int compare(Observation o1, Observation o2) {
			if (sortOrder == SortOrderEnum.ASC) {
				return Long.compare(getTimestamp(o1.getEffective()), getTimestamp(o2.getEffective()));
			} else {
				return Long.compare(getTimestamp(o2.getEffective()), getTimestamp(o1.getEffective()));
			}
		}
	}

	/**
	 * @param observations
	 *            A list of observations sorted according to
	 * @param periodEndMilli
	 *            A timestamp in the format of Instant.toEpochMilli().
	 * @return The observation with the greatest timestamp <= periodEndMilli or
	 *         null if no such observation exists.
	 */
	private Observation findNearestObservation(List<Observation> observations, long periodEndMilli) {
		if (observations.isEmpty()) {
			return null;
		}

		int left = 0;
		int right = observations.size() - 1;

		while (left < right) {
			if (right - left == 1) {
				if (getTimestamp(observations.get(right).getEffective()) <= periodEndMilli) {
					return observations.get(right);
				} else if (getTimestamp(observations.get(left).getEffective()) <= periodEndMilli) {
					return observations.get(left);
				} else {
					return null;
				}
			}

			int middle = left + (right - left) / 2;
			Observation obs = observations.get(middle);

			if (getTimestamp(obs.getEffective()) > periodEndMilli) {
				right = middle - 1;
			} else {
				left = middle;
			}
		}

		if (getTimestamp(observations.get(left).getEffective()) <= periodEndMilli) {
			return observations.get(left);
		} else {
			return null;
		}
	}

	private static long getTimestamp(IDatatype effective) {
		long timestamp;
		if (effective instanceof DateTimeDt) {
			timestamp = ((DateTimeDt) effective).getValue().toInstant().toEpochMilli();
		} else if (effective instanceof PeriodDt) {
			timestamp = ((PeriodDt) effective).getStart().getTime();
		} else {
			throw new RuntimeException("Incorrect/missing date in observation.");
		}
		return timestamp;
	}

	public ObservationService(Date start, Date end, List<String> patientIds) {
		this.patientIds = patientIds;

		if (start != null && end != null) {
			masterInterval = Interval.of(start.getTime(), end.getTime());
		} else {
			masterInterval = null;
		}
	}

	private List<Observation> performPatientIdentification(String patientId, String observationCode, Date start,
			Date end) {
		List<IResource> deviceUseStatements = deviceUseStatementDao.search(
				Params.of(T5DeviceUseStatement.SP_PERIOD, "gt", start).add(T5DeviceUseStatement.SP_PERIOD, "lt", end)
						.add(T5DeviceUseStatement.SP_PATIENT, patientId));

		List<Observation> result = new ArrayList<Observation>();

		for (IResource resource : deviceUseStatements) {
			DeviceUseStatement deviceUseStatement = (DeviceUseStatement) resource;
			String deviceId = deviceUseStatement.getDevice().getReference().getIdPart();

			PeriodDt whenUsed = deviceUseStatement.getWhenUsed();
			Date maxStart = (whenUsed.getStart().compareTo(start) > 0) ? whenUsed.getStart() : start;
			Date minEnd = (whenUsed.getEnd() == null) || (whenUsed.getEnd().compareTo(end) > 0) ? end
					: whenUsed.getEnd();

			result.addAll(obsConverter.convert(patientId, deviceId,
					observationDao.searchByDevice(deviceId, observationCode, maxStart, minEnd)));
		}

		return result;
	}

	/**
	 * Fetch observation type codes for observations occurring while a patient
	 * is associated to a device according to a DeviceUseStatement in the
	 * database.
	 * 
	 * @param patientId
	 * @param start
	 * @param end
	 * @return Observations containing only the type codes themselves from all
	 *         observations for the given time period and patient. Only one
	 *         observation is returned for each different code.
	 */
	private List<Observation> performPatientIdentificationForSummary(String patientId, Date start, Date end) {
		List<IResource> deviceUseStatements = deviceUseStatementDao.search(
				Params.of(T5DeviceUseStatement.SP_PERIOD, "gt", start).add(T5DeviceUseStatement.SP_PERIOD, "lt", end)
						.add(T5DeviceUseStatement.SP_PATIENT, patientId));

		Set<Code> result = new HashSet<>();
		for (IResource resource : deviceUseStatements) {
			DeviceUseStatement deviceUseStatement = (DeviceUseStatement) resource;
			String deviceId = deviceUseStatement.getDevice().getReference().getIdPart();

			PeriodDt whenUsed = deviceUseStatement.getWhenUsed();
			Date maxStart = (whenUsed.getStart().compareTo(start) > 0) ? whenUsed.getStart() : start;
			Date minEnd = (whenUsed.getEnd() == null) || (whenUsed.getEnd().compareTo(end) > 0) ? end
					: whenUsed.getEnd();

			result.addAll(observationDao.searchCodesByDevice(deviceId, maxStart, minEnd));
		}
		return obsConverter.convertToObservationSummary(result);
	}

	public List<Observation> searchByPatient(String patientId, String observationTypeCode, Date start, Date end,
			int sampleRateMilli, SortSpec sortSpec, Integer count) {
		List<Observation> result;
		result = searchFromFhirbase(patientId, observationTypeCode, start, end, count);

		if (shouldTimeShift(patientId)) {
			result = timeshiftedSearch(patientId, IdType.PATIENT, observationTypeCode, start, end);
		} else {
			result = performPatientIdentification(patientId, observationTypeCode, start, end);
		}

		result.addAll(searchFromFhirbase(patientId, observationTypeCode, start, end, count));

		return sortAndLimitCount(limitSamplingPeriod(result, start, end, sampleRateMilli), sortSpec, count);
	}

	@SuppressWarnings("unchecked")
	public List<Observation> searchFromFhirbase(String patientId, String observationTypeCode, Date start, Date end,
			Integer count) {
		Params params = Params.of(Observation.SP_SUBJECT, patientId).add(Observation.SP_CODE, observationTypeCode)
				.add(Observation.SP_DATE, "ge", start).add(Observation.SP_DATE, "le", end)
				.add(SP_METHOD + MODIFIER_MISSING, true).add(Observation.SP_VALUE_QUANTITY + MODIFIER_MISSING, false);

		if (count != null) {
			params.add(FhirbaseResourceDao.SP_COUNT, count);
		}
		return (List<Observation>) (List<? extends IResource>) fhirbaseObservationDao.search(params);
	}

	/**
	 * This method changes the order of the incoming arguments.
	 * 
	 * @param observations
	 * @param sortSpec
	 * @param count
	 * @return
	 */
	public List<Observation> sortAndLimitCount(List<Observation> observations, SortSpec sortSpec, Integer count) {

		if (sortSpec != null) {
			observations.sort(new ObservationTimestampComparator(sortSpec.getOrder()));
		}

		if (count != null) {
			return observations.subList(0, Math.min(count, observations.size()));
		}

		return observations;

	}

	public List<Observation> searchByDevice(String deviceId, String observationCode, Date start, Date end,
			int samplingPeriodMilli, SortSpec sortSpec, Integer count) {
		List<Observation> result = searchObservations(deviceId, IdType.DEVICE, observationCode, start, end);
		return sortAndLimitCount(limitSamplingPeriod(result, start, end, samplingPeriodMilli), sortSpec, count);
	}

	private boolean shouldTimeShift(String patientId) {
		return masterInterval != null && (patientIds.isEmpty() || patientIds.contains(patientId));
	}

	private List<Observation> timeshiftedSearch(String id, IdType idType, String observationCode, Date start,
			Date end) {

		Interval originalInterval = Interval.of(start, end);

		List<Interval> intervals = TimeShift.getIntervalsToFetch(masterInterval,
				Interval.of(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli()));
		List<Observation> observations = null;

		if (intervals.size() == 1) {
			observations = timeshiftSingleInterval(id, idType, observationCode, originalInterval, intervals);
		} else {
			observations = timeshiftMultipeIntervals(id, idType, observationCode, start, intervals);
		}

		return observations;
	}

	private List<Observation> timeshiftMultipeIntervals(String id, IdType idType, String observationCode, Date start,
			List<Interval> intervals) {
		List<Observation> observations = new ArrayList<Observation>();
		for (int i = 0; i < intervals.size(); ++i) {
			Interval currInterval = intervals.get(i);

			List<Observation> observationsFromDb = searchObservations(id, idType, observationCode,
					Date.from(Instant.ofEpochMilli(currInterval.start)),
					Date.from(Instant.ofEpochMilli(currInterval.end)));

			observations.addAll(updateTimestamps(cloneBetween(observationsFromDb, currInterval), masterInterval,
					start.toInstant().toEpochMilli(), i));
		}
		return observations;
	}

	private List<Observation> timeshiftSingleInterval(String id, IdType idType, String observationCode,
			Interval originalInterval, List<Interval> intervals) {
		List<Observation> observations = new ArrayList<Observation>();
		Interval interval = intervals.get(0);

		int numIterations = TimeShift.getNumIterations(masterInterval, originalInterval);
		List<Observation> observationsFromDb = searchObservations(id, idType, observationCode,
				Date.from(Instant.ofEpochMilli(interval.start)), Date.from(Instant.ofEpochMilli(interval.end)));

		// Add first interval
		Interval currInterval = Interval
				.of(TimeShift.transformTimestampToMaster(masterInterval, originalInterval.start), masterInterval.end);
		observations.addAll(updateTimestamps(cloneBetween(observationsFromDb, currInterval), masterInterval,
				originalInterval.start, 0));

		// Add intermediate intervals
		for (int iteration = 1; iteration < numIterations; iteration++) {
			observations.addAll(updateTimestamps(cloneBetween(observationsFromDb, masterInterval), masterInterval,
					originalInterval.start, iteration));
		}

		// Add last interval
		if (numIterations > 0) {
			currInterval = Interval.of(masterInterval.start,
					TimeShift.transformTimestampToMaster(masterInterval, originalInterval.end));
			observations.addAll(updateTimestamps(cloneBetween(observationsFromDb, currInterval), masterInterval,
					originalInterval.start, numIterations));
		}

		return observations;
	}

	private List<Observation> timeshiftedSummarySearch(String id, IdType idType, Date start, Date end) {
		List<Interval> intervals = TimeShift.getIntervalsToFetch(masterInterval,
				Interval.of(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli()));
		List<Observation> observations;

		if (intervals.size() == 1) {
			Interval interval = intervals.get(0);

			List<Observation> observationsFromDb = searchSummary(id, idType,
					Date.from(Instant.ofEpochMilli(interval.start)), Date.from(Instant.ofEpochMilli(interval.end)));

			observations = observationsFromDb;

		} else {
			observations = new ArrayList<Observation>();

			for (int i = 0; i < intervals.size(); ++i) {
				Interval currInterval = intervals.get(i);

				observations.addAll(searchSummary(id, idType, Date.from(Instant.ofEpochMilli(currInterval.start)),
						Date.from(Instant.ofEpochMilli(currInterval.end))));
			}
			observations = removeDuplicatesByCode(observations);
		}

		return observations;
	}

	private List<Observation> searchObservations(String id, IdType idType, String observationCode, Date start,
			Date end) {
		switch (idType) {
		case PATIENT:
			return performPatientIdentification(id, observationCode, start, end);
		case DEVICE:
			return obsConverter.convert(null, id, observationDao.searchByDevice(id, observationCode, start, end));
		default:
			throw new T5Exception("Incorrect id type.");
		}
	}

	private List<Observation> searchSummary(String id, IdType idType, Date start, Date end) {
		switch (idType) {
		case PATIENT:
			return performPatientIdentificationForSummary(id, start, end);
		case DEVICE:
			return obsConverter.convertToObservationSummary(observationDao.searchCodesByDevice(id, start, end));
		default:
			throw new T5Exception("Incorrect id type.");
		}
	}

	public List<Observation> searchSummaryByPatient(String patientId, Date start, Date end) {
		if (shouldTimeShift(patientId)) {
			return timeshiftedSummarySearch(patientId, IdType.PATIENT, start, end);
		} else {
			return searchSummary(patientId, IdType.PATIENT, start, end);
		}
	}

	public List<Observation> searchSummaryByDevice(String deviceId, Date start, Date end) {
		return searchSummary(deviceId, IdType.DEVICE, start, end);
	}

	/**
	 * Modifies incoming arguments.
	 * 
	 * @param observations
	 * @return The same reference as the observations parameter.
	 */
	private List<Observation> updateTimestamps(List<Observation> observations, Interval master, long startTimestamp,
			int iteration) {
		for (Observation obs : observations) {
			DateTimeDt applies = (DateTimeDt) obs.getEffective();

			obs.setEffective(new DateTimeDt(new Date(TimeShift.transformTimestampFromMaster(masterInterval,
					startTimestamp, applies.getValue().toInstant().toEpochMilli(), iteration))));

			obs.setId(new IdDt(T5FHIRUtils.generateObservationId()));
		}

		return observations;
	}

	private List<Observation> removeDuplicatesByCode(List<Observation> observations) {
		Set<String> codes = new HashSet<>();
		List<Observation> result = new ArrayList<>();
		for (Observation obs : observations) {
			String code = obs.getCode().getText();
			if (codes.add(code)) {
				result.add(obs);
			}
		}

		return result;
	}

	private List<Observation> cloneBetween(List<Observation> observations, Interval interval) {
		List<Observation> resultObs = new ArrayList<>();
		for (Observation obs : observations) {
			long applies = ((DateTimeDt) obs.getEffective()).getValue().toInstant().toEpochMilli();
			if (applies <= interval.end && applies >= interval.start) {
				resultObs.add(cloneResource(obs));
			}
		}
		return resultObs;
	}

	@SuppressWarnings("unchecked")
	private <T extends BaseResource> T cloneResource(BaseResource resource) {
		IParser parser = fhirContext.newJsonParser();
		String encodedResource = parser.encodeResourceToString(resource);
		return (T) parser.parseResource(encodedResource);
	}
}
