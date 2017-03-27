package ${packageName};
                    
import java.util.List;  
import org.gof.core.Port;
import org.gof.core.CallPoint;
import org.gof.core.Service;
import org.gof.core.support.Distr;
import org.gof.core.support.Param;
import org.gof.core.support.log.LogCore;
import org.gof.core.gen.proxy.ProxyBase;
import org.gof.core.support.function.*;
//import ${rootPacageName}.EnumCall;
import ${annotationPack};
<#if importPackages??>
<#list importPackages as package>
import ${package};
</#list>
</#if>

${annotation}
public final class ${proxyName} extends ProxyBase {
	public final class EnumCall{
		<#assign i = 0>
		<#list methods as method>
		<#assign i = i + 1> 
		public static final int ${method.enumCall} = ${i};
		</#list>		
	}
	<#if hasDefault>
	private static final String SERV_ID = "${servId}";
	</#if>
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private ${proxyName}() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		${className} serv = (${className})service;
		switch (methodKey) {
			<#list methods as method>
			case EnumCall.${method.enumCall}: {
				<#if method.hasException>
				GofFunction${method.paramsSize}${method.functionTypes} f = (${method.paramsCall}) -> { try { serv.${method.name}(${method.paramsCall}); } catch(Exception e) { throw new org.gof.core.support.SysException(e); } };
				return f;
				<#else>
				return (GofFunction${method.paramsSize}${method.functionTypes})serv::${method.name};
				</#if>
			}
			</#list>
			default: break;
		}
		return null;
	}
	
	<#if hasDefault>
	/**
	 * 获取实例
	 * 大多数情况下可用此函数获取
	 * @param localPort
	 * @return
	 */
	public static ${proxyName} newInstance() {
		String portId = Distr.getPortId(SERV_ID);
		if(portId == null) {
			LogCore.remote.error("通过servId未能找到查找上级Port: servId={}", SERV_ID);
			return null;
		}
		
		String nodeId = Distr.getNodeId(portId);
		if(nodeId == null) {
			LogCore.remote.error("通过portId未能找到查找上级Node: portId={}", portId);
			return null;
		}
		
		return createInstance(nodeId, portId, SERV_ID);
	}
	</#if>
	
	<#if !hasDefault>
	/**
	 * 获取实例
	 * @return
	 */
	public static ${proxyName} newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static ${proxyName} newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	</#if>
	
	/**
	 * 创建实例
	 * @param localPort
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static ${proxyName} createInstance(String node, String port, Object id) {
		${proxyName} inst = new ${proxyName}();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Param context) {
		localPort.listenResult(method, context);
	}
	
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Param context) {
		localPort.listenResult(method, context);
	}
	
	
	/**
	 * 等待返回值
	 */
	public Param waitForResult() {
		return localPort.waitForResult();
	}
	<#list methods as method>
	
	public void ${method.name}(${method.params}) {
		localPort.call(remote, EnumCall.${method.enumCall}, new Object[]{ ${method.paramsCall} });
	}
	</#list>
}
