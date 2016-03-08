package net.sllmdilab.t5xrefmanager.config;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import net.sllmdilab.t5xrefmanager.servlet.T5XRefManagerServlet;

public class T5XRefManagerWebAppInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext container) {
		Dynamic filter = container.addFilter("CORS", "com.thetransactioncompany.cors.CORSFilter");
		
		filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR), false, "/*");
		filter.setInitParameter("cors.supportedMethods", "GET, PUT, POST, HEAD, OPTIONS, DELETE");
		filter.setInitParameter("cors.exposedHeaders" , "Location");
		
		// Create the 'root' Spring application context
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.register(SecurityConfig.class);
		rootContext.register(T5XRefManagerApplicationConfig.class);
		
		// Manage the lifecycle of the root application context
		container.addListener(new ContextLoaderListener(rootContext));

		ServletRegistration.Dynamic servlet = container.addServlet("dispatcher", new T5XRefManagerServlet());
		servlet.setLoadOnStartup(1);
		servlet.addMapping("/fhir/*");
	}
}
