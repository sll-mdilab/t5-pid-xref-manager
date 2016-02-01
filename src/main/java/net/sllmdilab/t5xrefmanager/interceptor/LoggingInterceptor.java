package net.sllmdilab.t5xrefmanager.interceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.t5xrefmanager.dao.LogDao;
import net.sllmdilab.t5xrefmanager.domain.LogEntry;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;


public class LoggingInterceptor extends InterceptorAdapter {
	private Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
	
	@Autowired
	private FhirContext fhirContext;
	
	@Autowired
	private LogDao logDao;

	@Override
	public boolean outgoingResponse(RequestDetails requestDetails, IBaseResource responseObject,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

		try {
			log(requestDetails, servletRequest, servletResponse, responseObject, null);
		} catch (IOException e) {
			logger.error("Exception when logging: ", e);
		}

		return true;
	}

	@Override
	public boolean handleException(RequestDetails requestDetails, BaseServerResponseException exception,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException,
			IOException {
		log(requestDetails, servletRequest, servletResponse, null, exception);

		return true;
	}

	private void log(RequestDetails requestDetails, HttpServletRequest servletRequest, HttpServletResponse servletResponse, IBaseResource responseObject, Throwable exception) throws IOException {
		LogEntry logEntry = new LogEntry();
		
		logEntry.setRequestUrl(servletRequest.getRequestURL().toString());
		logEntry.setQueryParameter(LogEntry.ValueList.ofMap(servletRequest.getParameterMap()));
		logEntry.setRequestHeader(getRequestHeaderMap(servletRequest));
		logEntry.setResponseHeader(getResponseHeaderMap(servletResponse));
		
		if(responseObject != null) {
			IParser parser = fhirContext.newXmlParser();
			String resourceString = parser.encodeResourceToString(responseObject);
			logEntry.setResponseBody(resourceString);
		}

		if (exception != null) {
			logEntry.setException(exception.getMessage());
			if(exception instanceof BaseServerResponseException) {
				logEntry.setExceptionStatusCode(((BaseServerResponseException)exception).getStatusCode());
			}
		}
		
		logEntry.setTimeStamp(T5FHIRUtils.convertDateToXMLType(new Date()));
		
		logDao.insertAccessLog(logEntry);
	}

	private List<LogEntry.ValueList> getRequestHeaderMap(HttpServletRequest servletRequest) {
		List<LogEntry.ValueList> requestHeaders = new ArrayList<LogEntry.ValueList>();
		for(String header : Collections.list(servletRequest.getHeaderNames())) {
			Collection<String> values = Collections.list(servletRequest.getHeaders(header));
			requestHeaders.add(new LogEntry.ValueList(header, values.toArray(new String[values.size()])));
		}
		
		return requestHeaders;
	}
	
	private List<LogEntry.ValueList> getResponseHeaderMap(HttpServletResponse servletResponse) {
		List<LogEntry.ValueList> responseHeaders = new ArrayList<LogEntry.ValueList>();
		Collection<String> headerNames = servletResponse.getHeaderNames();
		for(String header : headerNames) {
			Collection<String> values = servletResponse.getHeaders(header);
			responseHeaders.add(new LogEntry.ValueList(header, values.toArray(new String[values.size()])));
		}
		
		return responseHeaders;
	}
}
