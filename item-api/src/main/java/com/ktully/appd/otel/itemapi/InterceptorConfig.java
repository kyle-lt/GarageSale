package com.ktully.appd.otel.itemapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Component
public class InterceptorConfig extends WebMvcConfigurationSupport {
	
	// This is commented out for now - all of my Otel context propagation code	
	// resides in the Controllers.  Eventually, I will move it to this interceptor.
	
	//@Autowired
	//TraceInterceptor traceInterceptor;
	
    //@Override
    //public void addInterceptors(InterceptorRegistry registry){
    //    registry.addInterceptor(traceInterceptor);
    //}

}
