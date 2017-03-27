package org.gof.demo.worldsrv.human;

import java.io.IOException;

import org.gof.core.CallPoint;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;

public class HumanGlobalInfo implements ISerilizable {
	public long id;					//ID
	public String modelSn;			//modelSn
	public String account;			//登录账号
	public String name;					//昵称
	public String nodeId;				//Node名称
	public String portId;				//Port名称
	public long stageId;				//所在地图
	public String stageName;			//所在地图名
	public int countryId;				//国家
	public long unionId;				//联盟
	public String sn;					//头像ID
	public int level;					//等级
	public int combat;                  //战斗力
	public int sex;						//性别
	public int profession;				//职业
	public long timeLogin;		//玩家登陆时间
	public CallPoint connPoint = new CallPoint();		//玩家连接ID
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(modelSn);
		out.write(account);
		out.write(name);
		out.write(nodeId);
		out.write(portId);
		out.write(stageId);
		out.write(stageName);
		out.write(countryId);
		out.write(unionId);
		out.write(sn);
		out.write(level);
		out.write(combat);
		out.write(sex);
		out.write(profession);
		out.write(timeLogin);
		out.write(connPoint);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		modelSn = in.read();
		account = in.read();
		name = in.read();
		nodeId = in.read();
		portId = in.read();
		stageId = in.read();
		stageName = in.read();
		countryId = in.read();
		unionId = in.read();
		sn = in.read();
		level = in.read();
		combat = in.read();
		sex = in.read();
		profession = in.read();
		timeLogin = in.read();
		connPoint = in.read();
	}
}
