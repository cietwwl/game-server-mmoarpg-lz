package org.gof.core;
import org.gof.core.gen.GofGenFile;
import org.gof.core.InputStream;

@GofGenFile
public final class CommonSerializer{
	public static org.gof.core.interfaces.ISerilizable create(int id){
		switch(id){
			case 1097158212:
				return new org.gof.core.support.ConnectionStatus();
			case 517232605:
				return new org.gof.core.connsrv.ConnectionBuf();
			case 957933789:
				return new org.gof.core.CallPoint();
			case 948421433:
				return new org.gof.core.support.Param();
			case -320466045:
				return new org.gof.core.CallReturn();
			case 1618489055:
				return new org.gof.core.db.Field();
			case -493833132:
				return new org.gof.core.support.TickTimer();
			case 1299139699:
				return new org.gof.core.Call();
			case -1592761974:
				return new org.gof.core.dbsrv.entity.IdAllot();
			case -929362650:
				return new org.gof.core.Record();
			case 1771839492:
				return new org.gof.core.RecordTransient();
			case 1104655619:
				return new org.gof.core.db.FieldSet();
			case 1618842360:
				return new org.gof.core.Chunk();
		}
		return null;
	}
	public static void init(){
		InputStream.setCreateCommonFunc(CommonSerializer::create);
	}
}

