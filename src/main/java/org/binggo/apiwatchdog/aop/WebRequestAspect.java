package org.binggo.apiwatchdog.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class WebRequestAspect {
	
	private static final Logger logger = LoggerFactory.getLogger(WebRequestAspect.class);
	
	private ThreadLocal<Long> startTime = new ThreadLocal<>();
	
	@Pointcut("execution(public * org.binggo.apiwatchdog.controller.StatisController.*(..))")
	public void webRequest() {}
	
	@Before("webRequest()")
	public void doBefore(JoinPoint joinpoint) {
		startTime.set(System.currentTimeMillis());
		
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		logger.info("receive a request to {}: {}", request.getRequestURL().toString(), 
				Arrays.toString(joinpoint.getArgs()));
	}
	
	@AfterReturning(returning="ret", pointcut="webRequest()")
	public void doAfterReturning() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		logger.debug("response time of accessing {}: {}s", request.getRequestURL().toString(), (System.currentTimeMillis() - startTime.get())/1000);
	}
	
	@AfterThrowing("webRequest()")
	public void doAfterThrowing() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		logger.error("accessing {} failed", request.getRequestURL().toString());
	}
	
	//@Around("webRequest()")
	public void checkResponseTime(ProceedingJoinPoint joinpoint) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		
		try {
			logger.info("receive a request to {}: {}", request.getRequestURL().toString(), 
					Arrays.toString(joinpoint.getArgs()));
			long start = System.currentTimeMillis();
			joinpoint.proceed();
			long end = System.currentTimeMillis();
			logger.debug("response time of accessing {}: {}s", request.getRequestURL().toString(), (end - start)/1000);	
		} catch (Throwable t) {
			logger.error("accessing {} failed", request.getRequestURL().toString());
		}
	}
}
