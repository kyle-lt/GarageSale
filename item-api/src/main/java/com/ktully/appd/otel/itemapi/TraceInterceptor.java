package com.ktully.appd.otel.itemapi;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.ktully.appd.otel.itemapi.Controller.ItemController;

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.ContextUtils;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

/*
 * This is an attempt to handle context propagation at the request intercepter, 
 * rather than at each Controller.
 */

@Component
public class TraceInterceptor implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(TraceInterceptor.class);

	@Autowired
	Tracer tracer;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		/*
		 * Configuration for Context Propagation to be done via HttpServletRequest
		 * extraction
		 */
		HttpTextFormat.Getter<HttpServletRequest> getter = new HttpTextFormat.Getter<HttpServletRequest>() {
			@Override
			public String get(HttpServletRequest carrier, String key) {
				logger.debug("Key = " + key);
				logger.debug("Key found! " + key);
				logger.debug("Value = " + carrier.getHeader(key));
				return carrier.getHeader(key);
			}
		};

		Span serverSpan = null;
		Context extractedContext = null;
		try {
			logger.debug("Trying to extact Context Propagation Headers");
			extractedContext = OpenTelemetry.getPropagators().getHttpTextFormat().extract(Context.current(), request,
					getter);
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators, using Context.current()", e);
			extractedContext = Context.current();
		}

		try (Scope scope = ContextUtils.withScopedContext(extractedContext)) {
			// Automatically use the extracted SpanContext as parent.
			logger.debug("Trying to build Span and then make DB call.");
			serverSpan = tracer.spanBuilder("/item-api").setSpanKind(Span.Kind.SERVER).startSpan();
			// Add the attributes defined in the Semantic Conventions
			serverSpan.setAttribute("http.method", "GET");
			serverSpan.setAttribute("http.scheme", "http");
			serverSpan.setAttribute("http.host", "item-api:8081");
			serverSpan.setAttribute("handler", "pre");

			tracer.withSpan(serverSpan);

			logger.debug("Trace preHandle called.");

			return true;
		} catch (Exception e) {
			logger.error("Exception caught attempting to create Span", e);
			tracer.withSpan(serverSpan);
			return true;
		}
	}
	
    @Override
    public void postHandle(
       HttpServletRequest request, HttpServletResponse response, Object handler,
       ModelAndView modelAndView) throws Exception {
    	
		/*
		 * Configuration for Context Propagation to be done via HttpServletRequest
		 * extraction
		 */
		HttpTextFormat.Setter<HttpServletResponse> setter = new HttpTextFormat.Setter<HttpServletResponse>() {
			@Override
			public void set(HttpServletResponse carrier, String key, String value) {
				logger.debug("Key = " + key);
				logger.debug("Value = " + value);
				carrier.addHeader(key, value);
			}
		};
		
		Span currentSpan = tracer.getCurrentSpan();
		currentSpan.setAttribute("handler", "post");
    	
		// Execute the header injection that we defined above in the Setter and
		// create HttpEntity to hold the headers (and pass to RestTemplate)
		OpenTelemetry.getPropagators().getHttpTextFormat().inject(Context.current(), response, setter);
		
		currentSpan.end();
		logger.debug("Trace postHandle called.");
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
       Object handler, Exception exception) throws Exception {
       }

}
