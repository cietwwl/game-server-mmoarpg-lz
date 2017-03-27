package org.gof.demo.worldsrv.support.enumKey;


public enum StageMapTypeKey {
	common("common"),
	rep("rep"),
	tower("tower"),
	competition("competition"),
	;
	
	private StageMapTypeKey(String content) {
		this.content = content;
	}

	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
