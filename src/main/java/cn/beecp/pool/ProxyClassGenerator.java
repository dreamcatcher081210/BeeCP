/*
 * Copyright Chris2018998
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.beecp.pool;

import javassist.*;

import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * An independent execution toolkit class to generate JDBC proxy classes with javassist,
 * then write to class folder.
 *
 * @author Chris.Liao
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public final class ProxyClassGenerator {

	/**
	 * default classes output folder in project
	 */
	private static String folder ="BeeCP/target/classes";

	/**
	 * @param args take the first argument as classes generated output folder,otherwise take default folder
	 *
	 * @throws Exception throw exception in generating process
	 */
	public static void main(String[] args) throws Exception {
		if (args != null && args.length > 0)
			folder = args[0];

		writeProxyFile(folder);
	}

	/**
	 * write to disk folder
	 * @param folder classes generated will write to it
	 * @throws Exception if failed to write file to disk
	 */
	public static void writeProxyFile(String folder) throws Exception {
		ProxyClassGenerator builder = new ProxyClassGenerator();
		CtClass[] ctClasses = builder.createJdbcProxyClasses();
		for (CtClass ctClass:ctClasses) {
			ctClass.writeFile(folder);
		}
	}

	/**
	 * create all wrapper classes based on JDBC some interfaces
	 *  @throws Exception if failed to generate class
	 *  @return a class array generated by javassist
	 *
	 * new Class:
	 * cn.beecp.pool.ProxyConnection
	 * cn.beecp.pool.ProxyStatement
	 * cn.beecp.pool.ProxyPsStatement
	 * cn.beecp.pool.ProxyCsStatement
	 * cn.beecp.pool.ProxyResultSet
	 */
	public CtClass[] createJdbcProxyClasses() throws Exception {
		try{
			ClassPool classPool = ClassPool.getDefault();
			classPool.importPackage("java.sql");
			classPool.importPackage("cn.beecp.pool");
			classPool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));

			//............Connection Begin.........
			CtClass ctConIntf = classPool.get(Connection.class.getName());
			CtClass ctConSuperclass = classPool.get(ProxyConnectionBase.class.getName());
			String ctConIntfProxyClassName ="cn.beecp.pool.ProxyConnection";
			CtClass ctConIntfProxyImplClass = classPool.makeClass(ctConIntfProxyClassName,ctConSuperclass);
			ctConIntfProxyImplClass.setInterfaces(new CtClass[]{ctConIntf});
			ctConIntfProxyImplClass.setModifiers(Modifier.PUBLIC|Modifier.FINAL);

			CtClass[] conCreateParam = new CtClass[]{
					classPool.get("cn.beecp.pool.PooledConnection")};

			CtConstructor subClassConstructor = new CtConstructor(conCreateParam,ctConIntfProxyImplClass);
			subClassConstructor.setModifiers(Modifier.PUBLIC);
			StringBuilder body = new StringBuilder();
			body.append("{");
			body.append("super($$);");
			body.append("}");
			subClassConstructor.setBody(body.toString());
			ctConIntfProxyImplClass.addConstructor(subClassConstructor);
			//...............Connection End................

			//.............statement Begin.............
			CtClass ctStatementIntf = classPool.get(Statement.class.getName());
			CtClass ctStatementWrapClass=classPool.get(ProxyStatementBase.class.getName());
			String ctStatementIntfProxyClassName ="cn.beecp.pool.ProxyStatement";
			CtClass ctStatementProxyImplClass = classPool.makeClass(ctStatementIntfProxyClassName,ctStatementWrapClass);
			ctStatementProxyImplClass.setInterfaces(new CtClass[]{ctStatementIntf});
			ctStatementProxyImplClass.setModifiers(Modifier.PUBLIC|Modifier.FINAL);
			CtClass[] statementCreateParam = new CtClass[] {
					classPool.get("java.sql.Statement"),
					classPool.get("cn.beecp.pool.ProxyConnection"),
					classPool.get("cn.beecp.pool.PooledConnection")};
			subClassConstructor = new CtConstructor(statementCreateParam,ctStatementProxyImplClass);
			subClassConstructor.setModifiers(Modifier.PUBLIC);
			body.delete(0, body.length());
			body.append("{");
			body.append("super($$);");
			body.append("}");
			subClassConstructor.setBody(body.toString());
			ctStatementProxyImplClass.addConstructor(subClassConstructor);
			//.............Statement Begin...............

			//............PreparedStatement Begin...............
			CtClass ctPsStatementIntf = classPool.get(PreparedStatement.class.getName());
			CtClass ctPStatementWrapClass=classPool.get(ProxyStatementBase.class.getName());
			String ctPsStatementIntfProxyClassName ="cn.beecp.pool.ProxyPsStatement";
			CtClass ctPsStatementProxyImplClass = classPool.makeClass(ctPsStatementIntfProxyClassName,ctPStatementWrapClass);
			ctPsStatementProxyImplClass.setInterfaces(new CtClass[]{ctPsStatementIntf});
			ctPsStatementProxyImplClass.setModifiers(Modifier.PUBLIC|Modifier.FINAL);

			CtClass[] statementPsCreateParam = new CtClass[] {
					classPool.get("java.sql.PreparedStatement"),
					classPool.get("boolean"),
					classPool.get("cn.beecp.pool.ProxyConnection"),
					classPool.get("cn.beecp.pool.PooledConnection")};
			subClassConstructor = new CtConstructor(statementPsCreateParam,ctPsStatementProxyImplClass);
			subClassConstructor.setModifiers(Modifier.PUBLIC);
			body.delete(0, body.length());
			body.append("{");
			body.append("super($$);");
			body.append("}");
			subClassConstructor.setBody(body.toString());
			ctPsStatementProxyImplClass.addConstructor(subClassConstructor);
			//........PreparedStatement End..............

			//..............CallableStatement Begin.............
			CtClass ctCsStatementIntf = classPool.get(CallableStatement.class.getName());
			CtClass ctCStatementWrapClass=classPool.get(ProxyStatementBase.class.getName());
			String ctCsStatementIntfProxyClassName ="cn.beecp.pool.ProxyCsStatement";
			CtClass ctCsStatementProxyImplClass = classPool.makeClass(ctCsStatementIntfProxyClassName,ctCStatementWrapClass);
			ctCsStatementProxyImplClass.setInterfaces(new CtClass[]{ctCsStatementIntf});
			ctCsStatementProxyImplClass.setModifiers(Modifier.PUBLIC|Modifier.FINAL);

			CtClass[] statementCsCreateParam = new CtClass[] {
					classPool.get("java.sql.CallableStatement"),
					classPool.get("boolean"),
					classPool.get("cn.beecp.pool.ProxyConnection"),
					classPool.get("cn.beecp.pool.PooledConnection"),
					classPool.get("boolean")};
			subClassConstructor = new CtConstructor(statementCsCreateParam,ctCsStatementProxyImplClass);
			subClassConstructor.setModifiers(Modifier.PUBLIC);

			body.delete(0, body.length());
			body.append("{");
			body.append("super($$);");
			body.append("}");
			subClassConstructor.setBody(body.toString());
			ctCsStatementProxyImplClass.addConstructor(subClassConstructor);
			//...........CallableStatement End...............

			//..............DatabaseMetaData Begin.............
			CtClass ctDatabaseMetaDataIntf = classPool.get(DatabaseMetaData.class.getName());
			CtClass ctDatabaseMetaDataSuperClass = classPool.get(ProxyDatabaseMetaDataBase.class.getName());
			String cttDatabaseMetaDataIntfProxyClassName ="cn.beecp.pool.ProxyDatabaseMetaData";
			CtClass ctDatabaseMetaDataProxyImplClass = classPool.makeClass(cttDatabaseMetaDataIntfProxyClassName,ctDatabaseMetaDataSuperClass);
			ctDatabaseMetaDataProxyImplClass.setInterfaces(new CtClass[]{ctDatabaseMetaDataIntf});
			ctDatabaseMetaDataProxyImplClass.setModifiers(Modifier.PUBLIC|Modifier.FINAL);

			CtClass[] databaseMetaData = new CtClass[] {
					classPool.get("java.sql.DatabaseMetaData"),
					classPool.get("cn.beecp.pool.ProxyConnection"),
					classPool.get("cn.beecp.pool.PooledConnection")};
			subClassConstructor = new CtConstructor(databaseMetaData,ctDatabaseMetaDataProxyImplClass);
			subClassConstructor.setModifiers(Modifier.PUBLIC);
			body.delete(0, body.length());
			body.append("{");
			body.append("super($$);");
			body.append("}");
			subClassConstructor.setBody(body.toString());
			ctDatabaseMetaDataProxyImplClass.addConstructor(subClassConstructor);
			//...........DatabaseMetaData End...............

			//............... Result Begin..................
			CtClass ctResultSetIntf = classPool.get(ResultSet.class.getName());
			CtClass ctResultSetSuperclass= classPool.get(ProxyResultSetBase.class.getName());
			String ctResultSetIntfProxyClassName ="cn.beecp.pool.ProxyResultSet";
			CtClass ctResultSetIntfProxyImplClass = classPool.makeClass(ctResultSetIntfProxyClassName,ctResultSetSuperclass);
			ctResultSetIntfProxyImplClass.setInterfaces(new CtClass[]{ctResultSetIntf});
			ctResultSetIntfProxyImplClass.setModifiers(Modifier.PUBLIC|Modifier.FINAL);

			CtClass[] resultSetCreateParam = new CtClass[]{
					classPool.get("java.sql.ResultSet"),
					classPool.get("cn.beecp.pool.ProxyStatementBase"),
					classPool.get("cn.beecp.pool.PooledConnection")};
			subClassConstructor = new CtConstructor(resultSetCreateParam,ctResultSetIntfProxyImplClass);
			subClassConstructor.setModifiers(Modifier.PUBLIC);
			body.delete(0, body.length());
			body.append("{");
			body.append("super($$);");
			body.append("}");
			subClassConstructor.setBody(body.toString());
			ctResultSetIntfProxyImplClass.addConstructor(subClassConstructor);
			//............Result End...............

			this.createProxyConnectionClass(classPool,ctConIntfProxyImplClass,ctConIntf,ctConSuperclass);
			CtClass statementSuperClass= classPool.get(ProxyStatementBase.class.getName());
			this.createProxyStatementClass(classPool,ctStatementProxyImplClass,ctStatementIntf,statementSuperClass);
			this.createProxyStatementClass(classPool,ctPsStatementProxyImplClass,ctPsStatementIntf,statementSuperClass);
			this.createProxyStatementClass(classPool,ctCsStatementProxyImplClass,ctCsStatementIntf,statementSuperClass);
			this.createProxyDatabaseMetaDataClass(classPool,ctDatabaseMetaDataProxyImplClass,ctDatabaseMetaDataIntf,ctDatabaseMetaDataSuperClass);
			this.createProxyResultSetClass(classPool,ctResultSetIntfProxyImplClass,ctResultSetIntf,ctResultSetSuperclass);

			//............... FastConnectionPool Begin..................
			CtClass ctFastConnectionPoolClass = classPool.get(FastConnectionPool.class.getName());
			CtClass[] ctcreateProxyConnectionParamTypes = new CtClass[] {
					classPool.get("cn.beecp.pool.PooledConnection"),
					classPool.get("cn.beecp.pool.Borrower"),
			};
			CtMethod createProxyConnectionMethod=ctFastConnectionPoolClass.getDeclaredMethod("createProxyConnection",ctcreateProxyConnectionParamTypes);
			body.delete(0, body.length());
			body.append("{");
			body.append(" $2.setBorrowedConnection($1);");
			body.append("	return $1.proxyConn=new ProxyConnection($1);");
			body.append("}");
			createProxyConnectionMethod.setBody(body.toString());
			//............... FastConnectionPool end..................

			return new CtClass[]{
					ctConIntfProxyImplClass,
					ctStatementProxyImplClass,
					ctPsStatementProxyImplClass,
					ctCsStatementProxyImplClass,
					ctDatabaseMetaDataProxyImplClass,
					ctResultSetIntfProxyImplClass,
					ctFastConnectionPoolClass};
		}catch(Throwable e){
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	/**
	 * create connection proxy class, and add JDBC statement methods to it
	 *
	 * @param classPool javassist class pool
	 * @param ctConIntfProxyClass connection implemented sub class will be generated
	 * @param ctConIntf connection interface in javassist class pool
	 * @param ctConSuperClass super class extend by 'ctConIntfProxyClass'
	 * @return proxy class base on connection interface
	 * @throws Exception some error occurred
	 */
	private Class createProxyConnectionClass(ClassPool classPool,CtClass ctConIntfProxyClass,CtClass ctConIntf,CtClass ctConSuperClass)throws Exception{
		CtMethod[] ctSuperClassMethods = ctConSuperClass.getMethods();
		HashSet notNeedAddProxyMethods= new HashSet();
		for(int i=0,l=ctSuperClassMethods.length;i<l;i++){
			int modifiers=ctSuperClassMethods[i].getModifiers();
			if((!Modifier.isAbstract(modifiers) && (Modifier.isPublic(modifiers)||Modifier.isProtected(modifiers)))
					|| Modifier.isFinal(modifiers)|| Modifier.isStatic(modifiers)|| Modifier.isNative(modifiers)){
				notNeedAddProxyMethods.add(ctSuperClassMethods[i].getName() + ctSuperClassMethods[i].getSignature());
			}
		}

		LinkedList<CtMethod> linkedList = new LinkedList<CtMethod>();
		resolveInterfaceMethods(ctConIntf,linkedList,notNeedAddProxyMethods);

		CtClass ctStatementIntf = classPool.get(Statement.class.getName());
		CtClass ctPsStatementIntf = classPool.get(PreparedStatement.class.getName());
		CtClass ctCsStatementIntf = classPool.get(CallableStatement.class.getName());
		CtClass ctDatabaseMetaDataIntf = classPool.get(DatabaseMetaData.class.getName());

		StringBuilder methodBuffer = new StringBuilder();
		for(CtMethod ctMethod:linkedList){
			String methodName = ctMethod.getName();
			CtMethod newCtMethodm = CtNewMethod.copy(ctMethod, ctConIntfProxyClass, null);
			newCtMethodm.setModifiers(Modifier.PUBLIC);

			methodBuffer.delete(0, methodBuffer.length());
			methodBuffer.append("{");
			methodBuffer.append("checkClose();");
			if (newCtMethodm.getReturnType() == ctStatementIntf) {
				methodBuffer.append("return new ProxyStatement(delegate."+methodName+"($$),this,pConn);");
			}else if(newCtMethodm.getReturnType() == ctPsStatementIntf){
				methodBuffer.append("if(pConn.stmCacheIsValid){");
				methodBuffer.append(" StatementCachePsKey key=new StatementCachePsKey($$);");
				methodBuffer.append(" PreparedStatement stm=pConn.getStatement(key);");
				methodBuffer.append(" if(stm==null){");
				methodBuffer.append("    stm=delegate."+methodName+"($$);");
				methodBuffer.append("    pConn.putStatement(key,stm);");
				methodBuffer.append("  }");
				methodBuffer.append("  return new ProxyPsStatement(stm,true,this,pConn);");
				methodBuffer.append("}else{");
				methodBuffer.append("  return new ProxyPsStatement(delegate."+methodName+"($$),false,this,pConn);");
				methodBuffer.append("}");
			}else if(newCtMethodm.getReturnType() == ctCsStatementIntf){
				methodBuffer.append("if(pConn.stmCacheIsValid){");
				methodBuffer.append(" StatementCacheCsKey key=new StatementCacheCsKey($$);");
				methodBuffer.append(" CallableStatement stm=(CallableStatement)pConn.getStatement(key);");
				methodBuffer.append(" if(stm==null){");
				methodBuffer.append("   stm=delegate."+methodName+"($$);");
				methodBuffer.append("   pConn.putStatement(key,stm);");
				methodBuffer.append(  "}");
				methodBuffer.append("  return new ProxyCsStatement(stm,true,this,pConn,true);");
				methodBuffer.append("}else{");
				methodBuffer.append(" return new ProxyCsStatement(delegate."+methodName+"($$),false,this,pConn,true);");
				methodBuffer.append("}");
			}else if (newCtMethodm.getReturnType() == ctDatabaseMetaDataIntf) {
				methodBuffer.append("return new ProxyDatabaseMetaData(delegate."+methodName+"($$),this,pConn);");
			}else if(methodName.equals("close")){
				//methodBuffer.append("super."+methodName + "($$);");
			}else if (newCtMethodm.getReturnType() == CtClass.voidType){
				methodBuffer.append(" delegate." + methodName + "($$);");
			}else{
				methodBuffer.append("return delegate." + methodName + "($$);");
			}

			methodBuffer.append("}");
			newCtMethodm.setBody(methodBuffer.toString());
			ctConIntfProxyClass.addMethod(newCtMethodm);

		}
		return ctConIntfProxyClass.toClass();
	}

	private Class createProxyStatementClass(ClassPool classPool, CtClass statementProxyClass,CtClass ctStatementIntf, CtClass ctStatementSuperClass) throws Exception {
		CtMethod[] ctSuperClassMethods = ctStatementSuperClass.getMethods();
		HashSet superClassSignatureSet = new HashSet();
		for (int i = 0, l = ctSuperClassMethods.length; i < l; i++) {
			int modifiers = ctSuperClassMethods[i].getModifiers();
			if ((!Modifier.isAbstract(modifiers) && (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)))
					|| Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isNative(modifiers)) {
				superClassSignatureSet.add(ctSuperClassMethods[i].getName() + ctSuperClassMethods[i].getSignature());
			}
		}

		LinkedList<CtMethod> linkedList = new LinkedList<CtMethod>();
		resolveInterfaceMethods(ctStatementIntf, linkedList, superClassSignatureSet);

		CtClass ctResultSetIntf=classPool.get(ResultSet.class.getName());
		StringBuilder methodBuffer = new StringBuilder();

		String delegateName="delegate.";
		if("java.sql.PreparedStatement".equals(ctStatementIntf.getName())){
			delegateName="delegate1.";
		}else if("java.sql.CallableStatement".equals(ctStatementIntf.getName())){
			delegateName="delegate2.";
		}

		for (CtMethod ctMethod : linkedList) {
			String methodName = ctMethod.getName();
			CtMethod newCtMethodm = CtNewMethod.copy(ctMethod, statementProxyClass, null);
			newCtMethodm.setModifiers(Modifier.PUBLIC);

			methodBuffer.delete(0, methodBuffer.length());
			methodBuffer.append("{");
			methodBuffer.append("checkClose();");

			if (newCtMethodm.getReturnType() == CtClass.voidType) {
				methodBuffer.append(delegateName+methodName + "($$);");
				if(methodName.startsWith("execute"))
					methodBuffer.append("pooledConn.updateAccessTimeWithCommitDirty();");
			} else {
				methodBuffer.append(newCtMethodm.getReturnType().getName() + " re="+delegateName+methodName + "($$);");
				if(methodName.startsWith("execute"))methodBuffer.append("pConn.updateAccessTimeWithCommitDirty();");
				if (newCtMethodm.getReturnType() == ctResultSetIntf) {
					methodBuffer.append("re=new ProxyResultSet(re,this,pConn);");
				}
				methodBuffer.append(" return re;");
			}

			methodBuffer.append("}");
			newCtMethodm.setBody(methodBuffer.toString());
			statementProxyClass.addMethod(newCtMethodm);

		}
		return statementProxyClass.toClass();
	}

	//ctDatabaseMetaDataProxyImplClass,ctDatabaseMetaDataIntf,ctDatabaseMetaDataSuperClass
	private Class createProxyDatabaseMetaDataClass(ClassPool classPool,CtClass ctDatabaseMetaDataProxyImplClass,CtClass ctDatabaseMetaDataIntf,CtClass ctDatabaseMetaDataSuperClass)throws Exception{
		CtMethod[] ctSuperClassMethods = ctDatabaseMetaDataSuperClass.getMethods();
		HashSet superClassSignatureSet= new HashSet();
		for(int i=0,l=ctSuperClassMethods.length;i<l;i++){
			int modifiers=ctSuperClassMethods[i].getModifiers();
			if((!Modifier.isAbstract(modifiers) && (Modifier.isPublic(modifiers)||Modifier.isProtected(modifiers)))
					|| Modifier.isFinal(modifiers)|| Modifier.isStatic(modifiers)|| Modifier.isNative(modifiers)){
				superClassSignatureSet.add(ctSuperClassMethods[i].getName() + ctSuperClassMethods[i].getSignature());
			}
		}

		LinkedList<CtMethod> linkedList = new LinkedList();
		resolveInterfaceMethods(ctDatabaseMetaDataIntf,linkedList,superClassSignatureSet);
		CtClass ctResultSetIntf=classPool.get(ResultSet.class.getName());

		StringBuilder methodBuffer = new StringBuilder();
		for(CtMethod ctMethod:linkedList){
			String methodName = ctMethod.getName();
			CtMethod newCtMethodm = CtNewMethod.copy(ctMethod, ctDatabaseMetaDataProxyImplClass, null);
			newCtMethodm.setModifiers(Modifier.PUBLIC);

			methodBuffer.delete(0, methodBuffer.length());
			methodBuffer.append("{");
			methodBuffer.append("checkClose();");

			if (newCtMethodm.getReturnType() == ctResultSetIntf) {
				methodBuffer.append("return new ProxyResultSet(delegate."+methodName+"($$),null,pConn);");
			} else if (newCtMethodm.getReturnType() == CtClass.voidType) {
				methodBuffer.append("delegate." + methodName+"($$);");
			} else {
				methodBuffer.append("return delegate."+methodName+"($$);");
			}

			methodBuffer.append("}");
			newCtMethodm.setBody(methodBuffer.toString());
			ctDatabaseMetaDataProxyImplClass.addMethod(newCtMethodm);
		}
		return ctDatabaseMetaDataProxyImplClass.toClass();
	}

	private Class createProxyResultSetClass(ClassPool classPool,CtClass ctResultSetIntfProxyClass,CtClass ctResultSetIntf,CtClass ctResultSetIntfSuperClass)throws Exception{
		CtMethod[] ctSuperClassMethods = ctResultSetIntfSuperClass.getMethods();
		HashSet superClassSignatureSet= new HashSet();
		for(int i=0,l=ctSuperClassMethods.length;i<l;i++){
			int modifiers=ctSuperClassMethods[i].getModifiers();
			if((!Modifier.isAbstract(modifiers) && (Modifier.isPublic(modifiers)||Modifier.isProtected(modifiers)))
					|| Modifier.isFinal(modifiers)|| Modifier.isStatic(modifiers)|| Modifier.isNative(modifiers)){
				superClassSignatureSet.add(ctSuperClassMethods[i].getName() + ctSuperClassMethods[i].getSignature());
			}
		}

		LinkedList<CtMethod> linkedList = new LinkedList();
		resolveInterfaceMethods(ctResultSetIntf,linkedList,superClassSignatureSet);
		StringBuilder methodBuffer = new StringBuilder();

		for(CtMethod ctMethod:linkedList){
			String methodName = ctMethod.getName();
			CtMethod newCtMethodm = CtNewMethod.copy(ctMethod, ctResultSetIntfProxyClass, null);
			newCtMethodm.setModifiers(Modifier.PUBLIC);

			methodBuffer.delete(0, methodBuffer.length());
			methodBuffer.append("{");
			methodBuffer.append("checkClose();");

			if (methodName.equals("close")) {
				//methodBuffer.append("super." + methodName + "($$);");
			} else {
				if(newCtMethodm.getReturnType() == CtClass.voidType){
					methodBuffer.append("delegate." + methodName + "($$);");
					if (methodName.startsWith("insertRow")||methodName.startsWith("updateRow")||methodName.startsWith("deleteRow"))
						methodBuffer.append(" pConn.updateAccessTimeWithCommitDirty();");
				}else{
					methodBuffer.append(newCtMethodm.getReturnType().getName() + " re=delegate." + methodName + "($$);");
					if (methodName.startsWith("insertRow")||methodName.startsWith("updateRow")||methodName.startsWith("deleteRow"))
						methodBuffer.append(" pConn.updateAccessTimeWithCommitDirty();");
					methodBuffer.append(" return re;");
				}
			}

			methodBuffer.append("}");
			newCtMethodm.setBody(methodBuffer.toString());
			ctResultSetIntfProxyClass.addMethod(newCtMethodm);
		}
		return ctResultSetIntfProxyClass.toClass();
	}

	private void resolveInterfaceMethods(CtClass interfaceClass,LinkedList linkedList,HashSet exitSignatureSet)throws Exception{
		CtMethod[] ctMethods = interfaceClass.getDeclaredMethods();
		for(int i=0;i<ctMethods.length;i++){
			int modifiers=ctMethods[i].getModifiers();
			String signature = ctMethods[i].getName()+ctMethods[i].getSignature();
			if(Modifier.isAbstract(modifiers)
					&& (Modifier.isPublic(modifiers)||Modifier.isProtected(modifiers))
					&& !Modifier.isStatic(modifiers)
					&& !Modifier.isFinal(modifiers)
					&& !exitSignatureSet.contains(signature)){

				linkedList.add(ctMethods[i]);
				exitSignatureSet.add(signature);
			}
		}

		CtClass[] superInterfaces=interfaceClass.getInterfaces();
		for(int i=0;i<superInterfaces.length;i++){
			resolveInterfaceMethods(superInterfaces[i],linkedList,exitSignatureSet);
		}
	}
}


