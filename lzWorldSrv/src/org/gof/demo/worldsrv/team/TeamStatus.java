package org.gof.demo.worldsrv.team;
/**
 * 队伍状态
 */
public enum TeamStatus {
	PUBLIS((byte)0), 			//发布，召集队友
	RUN((byte)1), 				//正在组队完成某项任务
	FULL_SIZE((byte)2),  		//队伍已满
	DISBANDING((byte)3), 		//解散中
	DISBANDED((byte)4),			//已解散
	;
	
	private final  byte type;
	TeamStatus(byte type){
		this.type = type;
	}
	public byte getType(){
		return type;
	}
	
	public static TeamStatus getTeamStatus(int type){
		for(TeamStatus t : TeamStatus.values()){
			if(t.getType() == type){
				return t;
			}
		}
		return null;
	}
}
