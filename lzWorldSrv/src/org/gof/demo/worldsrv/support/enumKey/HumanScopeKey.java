package org.gof.demo.worldsrv.support.enumKey;

public enum HumanScopeKey {

	ALL("所有"),
	HUMAN("玩家"),
	STAGE("地图"),
	COUNTRY("阵营"),
	UNION("公会"),
	TEAM("组队");

	private String desc;
	
	private HumanScopeKey(String desc){

		this.desc = desc;
	}


	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
