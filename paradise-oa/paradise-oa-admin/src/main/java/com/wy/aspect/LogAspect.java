package com.wy.aspect;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import com.alibaba.fastjson.JSON;
import com.wy.annotation.Log;
import com.wy.config.security.TokenService;
import com.wy.enums.HttpMethod;
import com.wy.enums.TipEnum;
import com.wy.manager.AsyncTask;
import com.wy.model.OperateLog;
import com.wy.model.User;
import com.wy.util.IpUtils;
import com.wy.util.ServletUtils;
import com.wy.util.SpringContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志记录 FIXME
 * 
 * @author 飞花梦影
 * @date 2021-01-13 14:21:24
 * @git {@link https://github.com/mygodness100}
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

	@Autowired
	private AsyncTask asyncTask;

	// 配置织入点
	@Pointcut("@annotation(com.wy.annotation.Log)")
	public void logPointCut() {}

	/**
	 * 处理完请求后执行
	 * 
	 * @param joinPoint 切点
	 */
	@AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
	public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
		handleLog(joinPoint, null, jsonResult);
	}

	/**
	 * 拦截异常操作
	 * 
	 * @param joinPoint 切点
	 * @param e 异常
	 */
	@AfterThrowing(value = "logPointCut()", throwing = "e")
	public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
		handleLog(joinPoint, e, null);
	}

	protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
		try {
			// 获得注解
			Log controllerLog = getAnnotationLog(joinPoint);
			if (controllerLog == null) {
				return;
			}

			// 获取当前的用户
			User loginUser = SpringContextUtils.getBean(TokenService.class)
					.getLoginUser(ServletUtils.getHttpServletRequest());
			// *========数据库日志=========*//
			OperateLog operLog = new OperateLog();
			operLog.setState(TipEnum.TIP_RESPONSE_SUCCESS.getCode());
			// 请求的地址
			String ip = IpUtils.getIpByRequest(ServletUtils.getHttpServletRequest());
			operLog.setOperateIp(ip);

			operLog.setOperateUrl(ServletUtils.getHttpServletRequest().getRequestURI());
			if (loginUser != null) {
				operLog.setOperateName(loginUser.getUsername());
			}

			if (e != null) {
				operLog.setState(TipEnum.TIP_RESPONSE_FAIL.getCode());
				operLog.setErrorMsg(e.getMessage().substring(0, 2000));
			}
			// 设置方法名称
			String className = joinPoint.getTarget().getClass().getName();
			String methodName = joinPoint.getSignature().getName();
			operLog.setMethod(className + "." + methodName + "()");
			// 设置请求方式
			operLog.setRequestMethod(ServletUtils.getHttpServletRequest().getMethod());
			// 处理设置注解上的参数
			getControllerMethodDescription(joinPoint, controllerLog, operLog);
			// 保存数据库
			asyncTask.recordOperation(operLog);
			// 拿到保存的结果,将日志的返回值存入日志中,返回的结果可能比较大,存在数据库浪费空间 FIXME
			log.info(JSON.toJSONString(jsonResult));
			// AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
		} catch (Exception exp) {
			// 记录本地异常日志
			log.error("==前置通知异常==");
			log.error("异常信息:{}", exp.getMessage());
			exp.printStackTrace();
		}
	}

	/**
	 * 获取注解中对方法的描述信息 用于Controller层注解
	 * 
	 * @param log 日志
	 * @param operLog 操作日志
	 * @throws Exception
	 */
	public void getControllerMethodDescription(JoinPoint joinPoint, Log log, OperateLog operLog) throws Exception {
		// 设置action动作
		operLog.setBusinessType(log.businessType().ordinal());
		// 设置标题
		operLog.setTitle(log.title());
		// 设置操作人类别
		operLog.setOperateType(log.operatorType().ordinal());
		// 是否需要保存request，参数和值
		if (log.isSaveRequestData()) {
			// 获取参数的信息，传入到数据库中。
			setRequestValue(joinPoint, operLog);
		}
	}

	/**
	 * 获取请求的参数，放到log中
	 * 
	 * @param operLog 操作日志
	 * @throws Exception 异常
	 */
	private void setRequestValue(JoinPoint joinPoint, OperateLog operLog) throws Exception {
		String requestMethod = operLog.getRequestMethod();
		if (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
			String params = argsArrayToString(joinPoint.getArgs());
			operLog.setOperateParam(params.substring(0, 2000));
		} else {
			Map<?, ?> paramsMap = (Map<?, ?>) ServletUtils.getHttpServletRequest()
					.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
			operLog.setOperateParam(paramsMap.toString().substring(0, 2000));
		}
	}

	/**
	 * 是否存在注解，如果存在就获取
	 */
	private Log getAnnotationLog(JoinPoint joinPoint) throws Exception {
		Signature signature = joinPoint.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;
		Method method = methodSignature.getMethod();

		if (method != null) {
			return method.getAnnotation(Log.class);
		}
		return null;
	}

	/**
	 * 参数拼装
	 */
	private String argsArrayToString(Object[] paramsArray) {
		String params = "";
		if (paramsArray != null && paramsArray.length > 0) {
			for (int i = 0; i < paramsArray.length; i++) {
				if (!isFilterObject(paramsArray[i])) {
					Object jsonObj = JSON.toJSON(paramsArray[i]);
					params += jsonObj.toString() + " ";
				}
			}
		}
		return params.trim();
	}

	/**
	 * 判断是否需要过滤的对象。
	 * 
	 * @param o 对象信息。
	 * @return 如果是需要过滤的对象，则返回true；否则返回false。
	 */
	public boolean isFilterObject(final Object o) {
		return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse;
	}
}