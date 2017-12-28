package com.june.aspect;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 监控方法的统计时间
 * 
 * @author wuchaoqun
 *
 */
@Aspect
@Component
public class MonitorAspect {

	private static Logger logger = Logger.getLogger(MonitorAspect.class);

	@Around("execution(* com.qili.*..*.*(..))")
	public Object timeAround(ProceedingJoinPoint joinPoint) {
		// 定义返回对象、得到方法需要的参数
		Object obj = null;
		Object[] args = joinPoint.getArgs();
		long startTime = System.currentTimeMillis();//记录方法执行起始时间
		try {
			obj = joinPoint.proceed(args);//执行方法
		} catch (Throwable e) {
			logger.error("统计某方法执行耗时环绕通知出错", e);
		}
		long endTime = System.currentTimeMillis();//记录方法执行结束时间
		// 获取执行的方法名
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
		// 打印耗时的信息
		this.printExecTime(methodName, startTime, endTime);
		return obj;

	}

	private void printExecTime(String methodName, long startTime, long endTime) {
		long diffTime = endTime - startTime;
		logger.info("-----" + methodName + " 方法执行耗时：" + diffTime + " ms");
	}

}
