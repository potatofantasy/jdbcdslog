package org.jdbcdslog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSetLoggingProxy  implements InvocationHandler {

	static Logger logger = LoggerFactory.getLogger(ResultSetLoggingProxy.class);
	
	Object target = null;
	
	public ResultSetLoggingProxy(ResultSet target) {
		this.target = target;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object r = method.invoke(target, args);
		if(logger.isInfoEnabled() && method.getName().equals("next") && ((Boolean)r).booleanValue()) {
			String fullMethodName = method.getDeclaringClass().getName() + "." + method.getName();
			ResultSet rs = (ResultSet)target;
			ResultSetMetaData md = rs.getMetaData();
			StringBuffer s = new StringBuffer(fullMethodName).append(" {");
			if(md.getColumnCount() > 0)
				s.append("(").append(rs.getObject(1)).append(")");
			for(int i = 2; i <= md.getColumnCount(); i++)
				s.append(", (").append(rs.getObject(i)).append(")");
			s.append("}");
			logger.info(s.toString());
		} 
		return r;
	}

	static Object wrapByResultSetProxy(ResultSet r) {
		return Proxy.newProxyInstance(r.getClass().getClassLoader(), new Class[]{ResultSet.class}, 
				new ResultSetLoggingProxy(r));
	}

}
