package org.joy.web.spmvc.modules.plugins.security.login.controller;

import org.joy.commons.bean.Pair;
import org.joy.commons.lang.string.StringTool;
import org.joy.plugin.image.captcha.consts.CaptchaConsts;
import org.joy.plugin.security.user.support.vo.UserLoginVo;
import org.joy.plugin.support.PluginBeanFactory;
import org.joy.web.support.utils.HttpRequestTool;
import org.joy.web.support.utils.HttpSessionTool;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author Kevice
 * @time 2013-2-23 下午7:46:37
 */
@Controller
public class LoginController {

	private static final String LOGIN_VIEW_NAME = "joy/commons/plugins/login/login";

	// @Resource
	// private ICaptchaService captchaService;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public String login(@RequestParam("username") String username, @RequestParam("password") String password,
			@RequestParam(value = "captcha", required = false) String captcha,
			@RequestParam(value = "rememberMe", required = false) String rememberMe) {
		boolean captchaRequire = PluginBeanFactory.getUserLoginLogService().shouldCaptchaRequire(username);
		if (StringTool.isBlank(captcha)) {
			if (captchaRequire) {
				return "captchaRequire";
			}
		}

		UserLoginVo loginVo = createLoginVo(username, password, captcha, rememberMe, captchaRequire);
		return PluginBeanFactory.getUserLoginService().login(loginVo);
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login() {
		boolean hasLogin = PluginBeanFactory.getUserLoginService().hasLogin();
		if (hasLogin) {
			return "redirect:";
		} else {
			return LOGIN_VIEW_NAME;
		}
	}

	@RequestMapping(value = "")
	public String home() {
		return "index.jsp";
	}

	private UserLoginVo createLoginVo(String username, String password, String captcha, String rememberMe,
			boolean captchaRequire) {
		UserLoginVo loginVo = new UserLoginVo();
		loginVo.setUserAccount(username);
		loginVo.setUserPassword(password);
		 loginVo.setRememberMe(StringTool.isNotBlank(rememberMe) ? "1" : "0");
		loginVo.setCaptchaRequire(captchaRequire);
		loginVo.setCaptchaClient(captcha);
		loginVo.setLoginIp(HttpRequestTool.getIpAddr());
		Pair<String, String> osInfo = HttpRequestTool.getOsInfo();
		loginVo.setOsType(osInfo.getLeft());
		loginVo.setOsVersion(osInfo.getRight());
		Pair<String, String> browserInfo = HttpRequestTool.getBrowserInfo();
		loginVo.setBroswerType(browserInfo.getLeft());
		loginVo.setBroswerVersion(browserInfo.getRight());
		String captchaGenTime = (String) HttpSessionTool.getSession().getAttribute(CaptchaConsts.CAPTCHA_SESSION_DATE);
		loginVo.setCaptchaGenTime(captchaGenTime);
		String captchaServer = (String) HttpSessionTool.getSession().getAttribute(CaptchaConsts.CAPTCHA_SESSION_KEY);
		loginVo.setCaptchaServer(captchaServer);
		return loginVo;
	}

}