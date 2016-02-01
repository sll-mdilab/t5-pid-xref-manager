package net.sllmdilab.t5xrefmanager.servlet;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import net.sllmdilab.t5xrefmanager.config.T5XRefManagerApplicationConfig;
import net.sllmdilab.t5xrefmanager.conformance.ConformanceProvider;
import net.sllmdilab.t5xrefmanager.interceptor.ApiKeyAuthenticationInterceptor;

@Import(T5XRefManagerApplicationConfig.class)
@Component
public class T5XRefManagerServlet extends RestfulServer {
	private Logger logger = LoggerFactory.getLogger(T5XRefManagerServlet.class);

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private ApiKeyAuthenticationInterceptor authenticationInterceptor;

	@Autowired
	private IResourceProvider[] resourceProviders;
	
	@Autowired
	private ConformanceProvider conformanceProvider;
	
	@Override
	protected void initialize() throws ServletException {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		setFhirContext(FhirContext.forDstu2());

		setResourceProviders(resourceProviders);
		setInterceptors(authenticationInterceptor);
		setServerConformanceProvider(conformanceProvider);
		logger.info("Initialized servlet.");
	}
}
