package cn.smbms.service.role;

import java.sql.Connection;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import cn.smbms.dao.BaseDao;
import cn.smbms.dao.role.RoleDao;
import cn.smbms.pojo.Role;

@Service
public class RoleServiceImpl implements RoleService{
	@Resource
	private RoleDao roleDao;
	
	public List<Role> getRoleList() {
		// TODO Auto-generated method stub
		Connection connection = null;
		List<Role> roleList = null;
		try {
			connection = BaseDao.getConnection();
			roleList = roleDao.getRoleList(connection);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			BaseDao.closeResource(connection, null, null);
		}
		return roleList;
	}
	
}
