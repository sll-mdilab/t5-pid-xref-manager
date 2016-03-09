package net.sllmdilab.t5xrefmanager.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import net.sllmdilab.t5xrefmanager.servlet.T5XRefManagerServlet;

public class T5XRefManagerWebAppInitializer implements WebApplicationInitializer {

	private static final String SERVLET_NAME = "dispatcher";

	@Override
	public void onStartup(ServletContext container) {
		// Create the 'root' Spring application context
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.register(SecurityConfig.class);
		rootContext.register(T5XRefManagerApplicationConfig.class);
		
		// Manage the lifecycle of the root application context
		container.addListener(new ContextLoaderListener(rootContext));

		ServletRegistration.Dynamic servlet = container.addServlet(SERVLET_NAME, new T5XRefManagerServlet());
		servlet.setLoadOnStartup(1);
		servlet.addMapping("/fhir/*");
	}
}
