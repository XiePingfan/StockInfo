package org.tokenring.db;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatis {
	private static MyBatis uniqueInstance = null;
	// mybatis的配置文件
	String resource = "config.xml";
	SqlSessionFactory sessionFactory = null;

	private MyBatis() {
		// 使用类加载器加载mybatis的配置文件（它也加载关联的映射文件）
		InputStream is = MyBatis.class.getClassLoader().getResourceAsStream(resource);
		// 构建sqlSession的工厂
		sessionFactory = new SqlSessionFactoryBuilder().build(is);
	}

	public static synchronized MyBatis getInstance() {
		if (uniqueInstance == null){
			uniqueInstance = new MyBatis();
		}
		return uniqueInstance;
	}
	
	public boolean executeSQL(String sql) {
		SqlSession s = sessionFactory.openSession();
		Map<String, String> params = new HashMap<String, String>();
		params.put("sql", sql);
		s.insert("selfSql",params);
		s.commit();
		s.close();
		return true;
	}
	
	public List<Map> queryBySQL(String sql) {
		SqlSession s = sessionFactory.openSession();
		Map<String, String> params = new HashMap<String, String>();
		params.put("sql", sql);
		List<Map> lm = s.selectList("selectBysql",params);
		
		s.close();
		return lm;
	}
	
	public List<Map> queryByLabel(String label,Map params) {
		SqlSession s = sessionFactory.openSession();
		List<Map> lm = s.selectList(label, params);
		s.close();
		return lm;
	}
	
	public SqlSession getSession(){
		return sessionFactory.openSession();
	}
	
	public void closeSession(SqlSession s){
		s.close();
	}
	
	public Cursor<Map> queryBySQL(SqlSession s,String sql){
		Map<String, String> params = new HashMap<String, String>();
		params.put("sql", sql);
		return s.selectCursor("selectBysql",params);
	}
	
	public Cursor<Map> queryByLabel(SqlSession s,String label,Map params){
		return s.selectCursor(label,params);
	}
	
	public boolean updateByLabel(String label, Map params) {
		SqlSession s = sessionFactory.openSession();
		s.update(label,params);
		s.commit();
		s.close();
		return true;
	}
	
	public boolean insertByLabel(String label, Map params) {
		SqlSession s = sessionFactory.openSession();
		int iRet = s.insert(label,params);
		//System.out.println(iRet);
		s.commit();
		s.close();
		return (iRet > 0);
	}
	
	public boolean delByLabel(String label, Map params) {
		SqlSession s = sessionFactory.openSession();
		s.delete(label,params);
		s.commit();
		s.close();
		return true;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
