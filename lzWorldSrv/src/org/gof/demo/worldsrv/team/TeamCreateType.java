package org.gof.demo.worldsrv.team;
/**
 * 创建组队的类型
 * 副本队伍离开队伍会将下一位队员晋升为队长
 * 注：判断当下的队伍是不是副本队伍用  team的reqId是否为0来判断
 */
public enum TeamCreateType {
	DEFAULT_TEAM((byte)0, 4, "default"),	//普通组队
	REP_TEAM((byte)1, 4, "req"),		//副本组队
	;
	
	private final byte type;
	private final int maxNum;       		//成员数量
	private final String name;
	
	TeamCreateType(byte type, int size, String name){
		this.type = type;
		this.maxNum = size;
		this.name = name;
	}
	public byte getType(){
		return type;
	}
	
	public int getMaxNum(){
		return maxNum;
	}
	
	public String getName(){
		return name;
	}
	
	public static TeamCreateType getTeamType(int type){
		for(TeamCreateType teamType : TeamCreateType.values()){
			if(teamType.getType() == type){
				return teamType;
			}
		}
		return null;
	}
}
