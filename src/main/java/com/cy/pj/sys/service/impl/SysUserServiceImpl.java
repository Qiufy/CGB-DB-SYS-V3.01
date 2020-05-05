package com.cy.pj.sys.service.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cy.pj.common.annotation.RequiredLog;
import com.cy.pj.common.exception.ServiceException;
import com.cy.pj.common.vo.PageObject;
import com.cy.pj.sys.dao.SysUserDao;
import com.cy.pj.sys.dao.SysUserRoleDao;
import com.cy.pj.sys.entity.SysUser;
import com.cy.pj.sys.service.SysUserService;
import com.cy.pj.sys.vo.SysUserDeptVo;
//1)@Service描述类表示要交给Spring管理
//2)Spring会创建此类对象,并将对象存储到map容器
//3)Spring Bean容器中的key默认为类名首字母小写.
@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;
    @Override
    public Map<String,Object> findObjectById(Integer id) {
    	//1.参数有效性校验
    	if(id==null||id<1)
    		throw new IllegalArgumentException("id值无效");
    	//2.基于id查询用户以及对应的部门信息
    	SysUserDeptVo user=
    	sysUserDao.findObjectById(id);
    	if(user==null)
    		throw new ServiceException("用户不存在");
    	//3.查询用户对应的角色id
    	List<Integer> roleIds=
    	sysUserRoleDao.findRoleIdsByUserId(id);
    	//4.封装两次查询结果
    	Map<String,Object> map=new HashMap<>();
    	map.put("user", user);
    	map.put("roleIds", roleIds);
    	return map;
    }
    
    @Override
    public int updateObject(SysUser entity, 
    		Integer[] roleIds) {
    	//1.参数检验
    	if(entity==null)
    		throw new IllegalArgumentException("保存对象不能为空");
    	if(StringUtils.isEmpty(entity.getUsername()))
    		throw new IllegalArgumentException("用户名不能为空");
    	if(roleIds==null||roleIds.length==0)
    		throw new ServiceException("必须为用户分配角色");
    	//2.保存用户自身信息
    	int rows=sysUserDao.updateObject(entity);
    	//3.保存用户和角色关系数据
    	sysUserRoleDao.deleteObjectsByUserId(entity.getId());
    	sysUserRoleDao.insertObjects(
    			entity.getId(),roleIds);
    	//4.返回结果
    	return rows;
    }
    
    @Override
    public int saveObject(SysUser entity, 
    		Integer[] roleIds) {
    	//1.参数检验
    	if(entity==null)
    		throw new IllegalArgumentException("保存对象不能为空");
    	if(StringUtils.isEmpty(entity.getUsername()))
    		throw new IllegalArgumentException("用户名不能为空");
    	if(StringUtils.isEmpty(entity.getPassword()))
    		throw new IllegalArgumentException("密码不能为空");
    	if(roleIds==null||roleIds.length==0)
    		throw new ServiceException("必须为用户分配角色");
    	//2.保存用户自身信息
    	//2.1对密码进行md5盐值加密
    	//获取一个盐值,这个值使用随机的字符串
    	String salt=UUID.randomUUID().toString();
    	//借助shiro框架中的API对应密码进行盐值加密
    	SimpleHash sh=new SimpleHash(
    			"MD5",//algorithmName 算法名(MD5算法是一种消息摘要算法)
    			entity.getPassword(),//source 未加密的密码
    			salt,//盐
    			1);//hashIterations表示加密次数
    	//将加密结果转成16进制
    	String pwd=sh.toHex();
    	//2.2将盐值和密码存储SysUser对象
    	entity.setSalt(salt);
    	entity.setPassword(pwd);
    	//2.3将SysUser对象持久化到数据库
    	int rows=sysUserDao.insertObject(entity);
    	//3.保存用户和角色关系数据
    	sysUserRoleDao.insertObjects(
    			entity.getId(),roleIds);
    	//4.返回结果
    	return rows;
    }
    
    @RequiredLog("禁用启用")
    @Override
    public int validById(Integer id, 
    		Integer valid, String modifiedUser) {
    	//1.验证参数有效性
    	if(id==null||id<1)
    		throw new IllegalArgumentException("id值无效");
    	if(valid==null||valid!=1&&valid!=0)
    		throw new IllegalArgumentException("状态不正确");
    	//...	
    	//2.更新用户状态,并对其结果进行判定
    	int rows=sysUserDao.validById(id, valid, modifiedUser);
    	if(rows==0)
    		throw new ServiceException("记录可能已经不存在");
    	//3.返回结果
    	return rows;
    }
    @RequiredLog("用户分页查询")
	@Override
	public PageObject<SysUserDeptVo> findPageObjects(String username, Integer pageCurrent) {
	    //1.参数校验
		if(pageCurrent==null||pageCurrent<1)
			throw new IllegalArgumentException("页码值不正确");
		//2.统计总记录数并校验
		int rowCount=
		sysUserDao.getRowCount(username);
		if(rowCount==0)
			throw new ServiceException("记录不存在");
		//3.查询当前页记录
		int pageSize=3;
		int startIndex=(pageCurrent-1)*pageSize;
		List<SysUserDeptVo> records=
		sysUserDao.findPageObjects(username,
				startIndex, pageSize);
		//4.封装查询结果
		return new PageObject<>(pageCurrent, pageSize, rowCount, records);
	}

}
