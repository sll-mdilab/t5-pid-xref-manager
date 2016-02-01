package net.sllmdilab.t5xrefmanager.config;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.ClinicalImpression;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Device;
import ca.uhn.fhir.model.dstu2.resource.DeviceMetric;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.EpisodeOfCare;
import ca.uhn.fhir.model.dstu2.resource.ListResource;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.Order;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import ca.uhn.fhir.model.dstu2.resource.ReferralRequest;
import net.sllmdilab.commons.converter.ObservationToT5XmlConverter;
import net.sllmdilab.commons.converter.T5QueryToFHIRConverter;
import net.sllmdilab.commons.exceptions.RosettaInitializationException;
import net.sllmdilab.commons.t5.validators.RosettaValidator;
import net.sllmdilab.t5xrefmanager.converter.SqlObservationToFhirConverter;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao;
import net.sllmdilab.t5xrefmanager.resource.BrokeringReceiver;
import net.sllmdilab.t5xrefmanager.resource.T5DeviceUseStatement;
import net.sllmdilab.t5xrefmanager.service.ObservationService;

@Configuration
@ComponentScan({ "net.sllmdilab.t5xrefmanager.*" })
@EnableTransactionManagement
public class T5XRefManagerApplicationConfig {

	@Value("${JDBC_CONNECTION_STRING}")
	private String jdbcConnectionString;

	@Value("${T5_XREF_TIMESHIFT_START}")
	private String timeshiftStart;

	@Value("${T5_XREF_TIMESHIFT_END}")
	private String timeshiftEnd;

	@Value("${T5_XREF_TIMESHIFT_PATIENT_IDS}")
	private String timeshiftPatientIds;
	
	@Bean
	public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
		PropertyPlaceholderConfigurer placeholderConfigurer = new PropertyPlaceholderConfigurer();
		placeholderConfigurer.setSystemPropertiesModeName("SYSTEM_PROPERTIES_MODE_OVERRIDE");
		return placeholderConfigurer;
	}

	@Bean
	public RosettaValidator rosettaValidator() throws IOException, RosettaInitializationException {
		return new RosettaValidator();
	}

	@Bean
	public FhirContext fhirContext() {
		return FhirContext.forDstu2();
	}

	@Bean
	public ObservationToT5XmlConverter observationToT5XmlConverter() {
		return new ObservationToT5XmlConverter();
	}

	@Bean
	public T5QueryToFHIRConverter t5QueryToFHIRConverter() throws Exception {
		return new T5QueryToFHIRConverter(rosettaValidator());
	}

	@Bean
	public SqlObservationToFhirConverter sqlObservationToFhirConverter() throws Exception {
		return new SqlObservationToFhirConverter(rosettaValidator());
	}
	
	@Bean
	public ObservationService observationService() {
		if (!StringUtils.isBlank(timeshiftStart) && !StringUtils.isBlank(timeshiftEnd)) {
			List<String> patientIds = Arrays.asList(StringUtils.split(timeshiftPatientIds, ","));
			
			Date start = Date.from(ZonedDateTime.parse(timeshiftStart, DateTimeFormatter.ISO_DATE_TIME).toInstant());
			Date end = Date.from(ZonedDateTime.parse(timeshiftEnd, DateTimeFormatter.ISO_DATE_TIME).toInstant());

			return new ObservationService(start, end, patientIds);
		} else {
			return new ObservationService(null, null, null);
		}
	}
	
	@Bean(destroyMethod="close")
	public DataSource dataSource() throws ClassNotFoundException {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl(jdbcConnectionString);
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setPoolPreparedStatements(true);
		dataSource.setMaxTotal(10);
		dataSource.setMaxIdle(10);
		return dataSource;
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate() throws ClassNotFoundException {
		return new JdbcTemplate(dataSource());
	}
	
	@Bean
	public FhirbaseResourceDao<T5DeviceUseStatement> dusDao() {
		return new FhirbaseResourceDao<>(T5DeviceUseStatement.class);
	}
	
	@Bean
	public FhirbaseResourceDao<Encounter> encounterDao() {
		return new FhirbaseResourceDao<>(Encounter.class);
	}
	
	@Bean
	public FhirbaseResourceDao<Patient> patientDao() {
		return new FhirbaseResourceDao<>(Patient.class);
	}
	
	@Bean
	public FhirbaseResourceDao<Practitioner> practitionerDao() {
		return new FhirbaseResourceDao<>(Practitioner.class);
	}
	
	@Bean
	public FhirbaseResourceDao<Device> deviceDao() {
		return new FhirbaseResourceDao<>(Device.class);
	}
	
	@Bean
	public FhirbaseResourceDao<BrokeringReceiver> brokeringReceiverDao() {
		return new FhirbaseResourceDao<>(BrokeringReceiver.class);
	}
	
	@Bean
	public FhirbaseResourceDao<EpisodeOfCare> episodeOfCareDao() {
		return new FhirbaseResourceDao<>(EpisodeOfCare.class);
	}
	
	@Bean
	public FhirbaseResourceDao<Organization> organizationDao() {
		return new FhirbaseResourceDao<>(Organization.class);
	}
	
	@Bean
	public FhirbaseResourceDao<ReferralRequest> referalRequestDao() {
		return new FhirbaseResourceDao<>(ReferralRequest.class);
	}
	
	@Bean
	public FhirbaseResourceDao<ClinicalImpression> clinicalImpressionDao() {
		return new FhirbaseResourceDao<>(ClinicalImpression.class);
	}
	
	@Bean
	public FhirbaseResourceDao<Medication> medicationDao() {
		return new FhirbaseResourceDao<>(Medication.class);
	}
	
	@Bean
	public FhirbaseResourceDao<MedicationOrder> medicationOrderDao() {
		return new FhirbaseResourceDao<>(MedicationOrder.class);
	}
	
	@Bean
	public FhirbaseResourceDao<Procedure> procedureDao() {
		return new FhirbaseResourceDao<>(Procedure.class);
	}
	
	@Bean
	public FhirbaseResourceDao<DiagnosticReport> diagnosticReportDao() {
		return new FhirbaseResourceDao<>(DiagnosticReport.class);
	}
	
	@Bean
	public FhirbaseResourceDao<Condition> conditionDao() {
		return new FhirbaseResourceDao<>(Condition.class);
	}
	
	@Bean
	public FhirbaseResourceDao<Order> corderDao() {
		return new FhirbaseResourceDao<>(Order.class);
	}
	
	@Bean
	public FhirbaseResourceDao<DeviceMetric> deviceMetricDao() {
		return new FhirbaseResourceDao<>(DeviceMetric.class);
	}
	
	@Bean
	public FhirbaseResourceDao<ListResource> listResouceDao() {
		return new FhirbaseResourceDao<>(ListResource.class);
	}
	
	@Bean
	public PlatformTransactionManager transactionManager() throws ClassNotFoundException  {
		return new DataSourceTransactionManager(dataSource());
	}
}
