package ${rootPackageName};
import org.gof.core.support.observer.ObServer;
import org.gof.core.support.function.*;
import org.gof.core.gen.GofGenFile;

@GofGenFile
public final class ${rootClassName}{
	public static <K,P> void init(ObServer<K, P> ob){
	<#list methodsList as map>
	<#list map.methods as m>
	<#list m.keys as key>
		ob.reg("${key}", (GofFunction${m.paramsSize}${m.functionTypes})(ob.getTargetBean(${map.packageName}.${map.className}.class))::${m.name}, ${m.paramsSize});  
	</#list>
	</#list>
	</#list>
	}
}

