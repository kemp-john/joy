package org.joy.plugin.support;

import com.google.code.kaptcha.Producer;
import org.activiti.engine.*;
import org.apache.shiro.mgt.SecurityManager;
import org.joy.core.spring.utils.CoreBeanFactory;
import org.joy.core.spring.utils.SpringBeanTool;
import org.joy.plugin.image.captcha.service.ICaptchaService;
import org.joy.plugin.monitor.jdbc.jwebap.service.ISysSqlLogService;
import org.joy.plugin.persistence.mybatis.service.IEntityMappingHolder;
import org.joy.plugin.persistence.mybatis.service.IMapperManager;
import org.joy.plugin.schedule.quartz.service.IQuartzJobRegistry;
import org.joy.plugin.schedule.quartz.service.IQuartzTriggersHolder;
import org.joy.plugin.security.user.service.IUserLoginLogService;
import org.joy.plugin.security.user.service.IUserLoginService;
import org.joy.plugin.security.user.service.IUserLogoutLogService;
import org.joy.plugin.security.user.service.IUserLogoutService;
import org.joy.plugin.seqgen.service.ISequenceGenerator;
import org.quartz.Scheduler;
import org.springframework.core.task.TaskExecutor;

/**
 * 各种组件Bean的工厂
 * 
 * @since 1.0.0
 * @author Kevice
 * @time 2012-6-7 下午10:33:48
 */
public class PluginBeanFactory extends CoreBeanFactory {

	public static ISequenceGenerator getSequenceGenerator() {
		return (ISequenceGenerator) SpringBeanTool.getBean("sequenceGenerator");
	}
	
//	public static EhCacheManager getShiroCacheManager() {
//		return (EhCacheManager) SpringBeanUtils.getBean("shiroCacheManager");
//	}

	public static Scheduler getScheduler() {
		return (Scheduler) SpringBeanTool.getBean("schedulerFactory");
	}

	public static IQuartzJobRegistry getQuartzJobRegistry() {
		return (IQuartzJobRegistry) SpringBeanTool.getBean("quartzJobRegistry");
	}

	public static IQuartzTriggersHolder getQuartzTriggersHolder() {
		return (IQuartzTriggersHolder) SpringBeanTool.getBean("quartzTriggersHolder");
	}
	
	public static SecurityManager getSecurityManager() {
		return (SecurityManager) SpringBeanTool.getBean("securityManager");
	}
	
	public static RepositoryService getWfRepositoryService() {
		return (RepositoryService) SpringBeanTool.getBean("wfRepositoryService");
	}
	
	public static  RuntimeService getWfRuntimeService() {
		return (RuntimeService) SpringBeanTool.getBean("wfRuntimeService");
	}
	
	public static  TaskService getWfTaskService() {
		return (TaskService) SpringBeanTool.getBean("wfTaskService");
	}
	
	public static  HistoryService getWfHistoryService() {
		return (HistoryService) SpringBeanTool.getBean("wfHistoryService");
	}
	
	public static  ManagementService getWfManagementService() {
		return (ManagementService) SpringBeanTool.getBean("wfManagementService");
	}
	
	public static  FormService getWfFormService() {
		return (FormService) SpringBeanTool.getBean("wfFormService");
	}
	
	public static IdentityService getWfIdentityService() {
		return (IdentityService) SpringBeanTool.getBean("wfIdentityService");
	}
	
	public static ICaptchaService getCaptchaService() {
		return (ICaptchaService) SpringBeanTool.getBean("captchaService");
	}
	
	public static IUserLoginService getUserLoginService() {
		return (IUserLoginService) SpringBeanTool.getBean("userLoginService");
	}
	
	public static IUserLoginLogService getUserLoginLogService() {
		return (IUserLoginLogService) SpringBeanTool.getBean("userLoginLogService");
	}
	
	public static IUserLogoutService getUserLogoutService() {
		return (IUserLogoutService) SpringBeanTool.getBean("userLogoutService");
	}
	
	public static IUserLogoutLogService getUserLogoutLogService() {
		return (IUserLogoutLogService) SpringBeanTool.getBean("userLogoutLogService");
	}
	
	public static ISysSqlLogService getSysSqlLogService() {
		return (ISysSqlLogService) SpringBeanTool.getBean("sysSqlLogService");
	}
	
	public static TaskExecutor getSysSqlLogThreadPool() {
		return (TaskExecutor) SpringBeanTool.getBean("sysSqlLogThreadPool");
	}
	
	/**
	 * 验证码生产者
	 * 
	 * @return
	 * @since 1.0.0
	 * @author Kevice
	 * @time 2013年9月21日 下午6:39:43
	 */
	public static Producer getCaptchaProducer() {
		return (Producer) SpringBeanTool.getBean("captchaProducer");
	}

    public static IEntityMappingHolder getEntityMappingHolder() {
        return (IEntityMappingHolder) SpringBeanTool.getBean("entityMappingHolder");
    }

    public static IMapperManager getMapperManager() {
        return (IMapperManager) SpringBeanTool.getBean("mapperManager");
    }

}
