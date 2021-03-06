package org.joy.web.spmvc.modules.plugins.security.login.controller;

import org.joy.plugin.security.user.support.enums.LogoutMethod;
import org.joy.plugin.support.PluginBeanFactory;
import org.joy.web.support.utils.HttpRequestTool;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author Kevice
 * @time 2013-2-24 下午7:49:41
 */
@Controller
public class LogoutController {

	public static final String JOY_KEY__LOGOUT_METHOD_CODE = "_joy_key__logout_method_code";
	
	@RequestMapping("/logout")
	public String logout() {
		String logoutMethodCode = HttpRequestTool.getParameter(JOY_KEY__LOGOUT_METHOD_CODE);
		LogoutMethod logoutMethod = LogoutMethod.enumOf(logoutMethodCode);
		PluginBeanFactory.getUserLogoutService().logout(null, logoutMethod, null);
		return "redirect:login";
	}
	
}
