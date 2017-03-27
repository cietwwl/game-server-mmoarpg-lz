package org.gof.demo.worldsrv.stage;

public interface StageScoreStatusKey {
	public static final int STATUS_INIT = 0;	//未完成
	public static final int STATUS_FINISHED = 1; //已完成
	public static final int STATUS_FAILED = 1; //失败(失去评分)
}
