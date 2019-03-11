package cn.smbms.controller;

import cn.smbms.pojo.Role;
import cn.smbms.pojo.User;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.user.UserService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController{
	private Logger logger = Logger.getLogger(UserController.class);

	@Resource
	private UserService userService;

	@Resource
	private RoleService roleService;

	//实现跳转到登录页
	@RequestMapping(value = "/login.html")
	public String login(){
		logger.info("UserController welcome SMBMS==============");
		return "login";
	}

	//实现登录
	@RequestMapping(value = "dologin.html",method = RequestMethod.POST)
	public String doLogin(@RequestParam String userCode, @RequestParam String userPassword,
						  HttpSession session , HttpServletRequest request){
		logger.info("doLogin==============");
		//调用service方法，进行用户匹配
		User user = userService.login(userCode, userPassword);
		if (null != user) {//登录成功
			//放入session
			//页面跳转（frame.jsp) 带出提示信息
			session.setAttribute(Constants.USER_SESSION,user);
			return "forward:/user/main.html";
			//Response.sedRedirect("jsp/frame.jsp")
		} else {
			//页面跳转(login.jsp)
			request.setAttribute("error","用户名或密码不正确");
			return "login";
		}
	}


	@RequestMapping(value = "main.html")
	public String main(HttpSession session){
		if (session.getAttribute(Constants.USER_SESSION) == null) {
			return "forward:/user/login.html";
		}
		return "frame";
	}

	@RequestMapping(value = "/logout.html")
	public String logout(HttpSession session) {
		//清除session
		session.removeAttribute(Constants.USER_SESSION);
		return "login";
	}


	@RequestMapping(value = "exlogin.html",method = RequestMethod.GET)
	public String exLogin(@RequestParam String userCode, @RequestParam String userPassword){
		logger.info("exLogin=============================");
		//调用service方法，进行用户匹配
		User user = userService.login(userCode, userPassword);
		if (user == null) {//登录失败
			throw new RuntimeException("用户名或密码不正确");
		}
		return "redirect:/user/main.html";
	}

	/*@ExceptionHandler(value = {RuntimeException.class})
	public String handlerException(RuntimeException e, HttpServletRequest request) {
		request.setAttribute("e",e);
		return "error";
	}*/

	@RequestMapping(value = "/userlist.html")
	public String getUserList(Model model,@RequestParam(value = "queryname",required = false)String queryUserName,
							  @RequestParam(value = "queryUserRole",required = false)String queryUserRole,
							  @RequestParam(value = "pageIndex",required = false)String pageIndex){
		logger.info("getUserList --------> queryUserName: "+queryUserName);
		logger.info("getUserList --------> queryUserRole: "+queryUserRole);
		logger.info("getUserList --------> pageIndex: "+pageIndex);

		int _queryUserRole = 0;
		List<User> userList = null;
		//设置页面容量
		int pageSize = Constants.pageSize;
		//当前页码
		int currentPageNo = 1;

		if (queryUserName == null) {
			queryUserName = "";
		}
		if (queryUserRole != null && !queryUserRole.equals("")) {
			_queryUserRole = Integer.parseInt(queryUserRole);
		}

		if (pageIndex != null) {
			try {
				currentPageNo = Integer.valueOf(pageIndex);
			} catch (NumberFormatException e) {
				return "redirect:/user/syserror.html";
				//response.sendRedirect("syserror.jsp");
			}
		}

		//总数量（表）
		int totalCount = userService.getUserCount(queryUserName, _queryUserRole);
		//总页数
		PageSupport pages = new PageSupport();
		pages.setCurrentPageNo(currentPageNo);
		pages.setPageSize(pageSize);
		pages.setTotalCount(totalCount);
		int totalPageCount = pages.getTotalPageCount();
		//控制首页和尾页
		if (currentPageNo < 1) {
			currentPageNo = 1;
		} else if (currentPageNo > totalPageCount) {
			currentPageNo = totalPageCount;
		}
		userList = userService.getUserList(queryUserName, _queryUserRole, currentPageNo, pageSize);
		model.addAttribute("userList", userList);
		List<Role> roleList = null;
		roleList = roleService.getRoleList();
		model.addAttribute("roleList", roleList);
		model.addAttribute("queryUserName", queryUserName);
		model.addAttribute("_queryUserRole", _queryUserRole);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("currentPageNo", currentPageNo);
		System.out.println("hello");
		return "userlist";
	}

	@RequestMapping(value = "/syserror.html")
	public String sysError(){
		return "syserror";
	}

	@RequestMapping(value = "/useradd.html", method = RequestMethod.GET)
	public String addUser(User user, Model model) {

		model.addAttribute("user", user);
		return "useradd";
	}
	/*@RequestMapping(value = "/useradd.html", method = RequestMethod.GET)
	public String addUser(@ModelAttribute("user") User user) {
		return "useradd";
	}*/

	@RequestMapping(value = "/useraddsave.html", method = RequestMethod.POST)
	public String addUserSave(User user, HttpSession session,
							  HttpServletRequest request,
							  @RequestParam(value = "attachs",required = false) MultipartFile[] attachs) {
		String idPicPath = null;
		String workPicPath = null;
		String errorinfo = null;
		boolean flag = true;
		String path = request.getSession().getServletContext().getRealPath("statics"
				+ File.separator+"uploadfiles");

		for (int i=0;i<attachs.length;i++){
			MultipartFile attach = attachs[i];

			//判断文件是否为空
			if (!attach.isEmpty()) {
				if (i == 0) {
					errorinfo = "uploadFileError";
				} else if (i == 1) {
					errorinfo = "uploadWpError";
				}
				//定义上传目标路径
				String oldFileName = attach.getOriginalFilename();
				String suffix = FilenameUtils.getExtension(oldFileName);
				int filesize = 5000000;
				if (attach.getSize() > filesize) {
					request.setAttribute("errorinfo","* 上传大小不得超过500kb");
					flag = false;
				} else if (suffix.equalsIgnoreCase("jpg")
						|| suffix.equalsIgnoreCase("jpeg")
						|| suffix.equalsIgnoreCase("png")
						|| suffix.equalsIgnoreCase("pneg")) {
					//当前系统时间+随机数+“_Personal.jpg"
					String fileName = System.currentTimeMillis()+ RandomUtils.nextInt(1000000)+"_Personal."+suffix;
					File targetFile = new File(path, fileName);
					if (!targetFile.exists()) {
						targetFile.mkdirs();
					}

					try {
						attach.transferTo(targetFile);
					} catch (IOException e) {
						e.printStackTrace();
						request.setAttribute("errorinfo","* 上传失败");
						flag = false;
					}
					if (i == 0) {
						idPicPath = path+File.separator+fileName;
					} else if (i == 1) {
						workPicPath = path+File.separator+fileName;
					}
				} else {
					request.setAttribute("errorinfo","* 上传图片格式不正确");
					flag = false;
				}

			}

		}
		if (flag) {
			user.setCreatedBy(((User) session.getAttribute(Constants.USER_SESSION)).getId());
			user.setCreationDate(new Date());
			user.setIdPicPath(idPicPath);
			user.setWorkPicPath(workPicPath);
			if (userService.add(user)) {
				return "redirect:/user/userlist.html";
			}
		}
		return "useradd";
	}


	@RequestMapping(value = "/add.html",method = RequestMethod.GET)
	public String add(@ModelAttribute("user")User user){
		return "user/useradd";
	}
	@RequestMapping(value = "/add.html",method = RequestMethod.POST)
	public String addSave(@Valid User user, BindingResult bindingResult, HttpSession session) {
		if (bindingResult.hasErrors()) {
			logger.info("add user validated has errors===============");
			return "user/useradd";
		}

		user.setCreatedBy(((User) session.getAttribute(Constants.USER_SESSION)).getId());
		user.setCreationDate(new Date());
		if (userService.add(user)) {
			return "redirect:/user/userlist.html";
		}
		return "/user/useradd";
	}

	//根据用户id获取用户信息
	@RequestMapping(value = "/usermodify.html",method = RequestMethod.GET)
	public String getUserById(@RequestParam String uid,Model model){
		User user = userService.getUserById(uid);
		model.addAttribute(user);
		return "usermodify";
	}

	@RequestMapping(value = "/usermodifysave.html", method = RequestMethod.POST)
	public String modifUserSave(User user, HttpSession session) {
		user.setModifyBy(((User) session.getAttribute(Constants.USER_SESSION)).getId());
		user.setModifyDate(new Date());
		if (userService.modify(user)) {
			return "redirect:/user/userlist.html";
		} else {
			return "usermodify";
		}
	}

	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String view(@PathVariable String id, Model model) {
		User user = userService.getUserById(id);
		model.addAttribute(user);
		return "userview";
	}

	@RequestMapping(value = "/ucexist.html")
	@ResponseBody
	public Object userCodeIsExist(@RequestParam String userCode) {
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if (com.mysql.jdbc.StringUtils.isNullOrEmpty(userCode)) {
			resultMap.put("userCode", "exist");
		} else {
			User user = userService.selectUserCodeExist(userCode);
			if (user != null) {
				resultMap.put("userCode", "exist");
			} else {
				resultMap.put("userCode", "noexist");
			}
		}
		return JSONArray.toJSONString(resultMap);
	}

	@RequestMapping(value = "/view",method = RequestMethod.GET/*,
					produces = {"application/json;charset=UTF-8"}*/)
	@ResponseBody
	public Object view(@RequestParam String id){
		String cjson = "";
		if (null == id || "".equals(id)) {
			return "nodata";
		} else {
			try {
				User user = userService.getUserById(id);
				cjson = JSONArray.toJSONString(user);
			} catch (Exception e) {
				e.printStackTrace();
				return "failed";
			}
		}
		return cjson;
	}


}
