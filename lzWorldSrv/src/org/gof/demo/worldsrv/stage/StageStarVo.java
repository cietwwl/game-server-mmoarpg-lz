package org.gof.demo.worldsrv.stage;



/**
 * 场景评分进度类
 * @author g
 *
 */
public class StageStarVo{
	public int sn;						    //场景sn	
	public int[][] targetProgress;			//目标进度
	public int[][] nowProgress;				//当前进度
	public int[][] status;					//状态 0:未达成 	1:已达成   2:失败
	public int[] type;					    //类型
	public String[][] targetSn;				//目标对象
	
	
	/**
	 * 更新目标的进度
	 * @param sn 唯一标识sn
	 * @param index 目标索引
	 * @param checkSn 是否检查sn (如:击杀指定怪物需要检查sn,累计击杀一定数量怪物不需要检查sn)
	 * @param isFail 是否认为评分失败(如:不能触发某个机关，触发了就认为失败了)
	 */
	public void checkTarget(String sn,int index,boolean checkSn,boolean isFail){
		String[] target = targetSn[index];
		
		for(int k=0; k<target.length; k++){
			if(checkSn && target[k].equals(sn)){
				nowProgress[index][k]++;
				if(nowProgress[index][k] >= targetProgress[index][k]){
					if(isFail){
						status[index][k] = StageScoreStatusKey.STATUS_FAILED;
					}else{
						status[index][k] = StageScoreStatusKey.STATUS_FINISHED;
					}
				}
			}
		}
	}
	
	/**
	 * 更新目标的进度
	 * @param sn 唯一标识sn
	 * @param index 目标索引
	 * @param count 数量
	 */
	public void checkTarget(String sn,int index,int count){
		String[] target = targetSn[index];
		
		for(int k=0; k<target.length; k++){
			if(target[k].equals(sn)){
				nowProgress[index][k] = count;
				if(nowProgress[index][k] >= targetProgress[index][k]){
					status[index][k] = StageScoreStatusKey.STATUS_FINISHED;
				}
			}
		}
	}
	
	
	/**
	 * 完成某个进度
	 */
	public void finishTarget(int index){
		String[] target = targetSn[index];
		for(int k=0; k<target.length; k++){
			nowProgress[index][k]++;
			status[index][k] = StageScoreStatusKey.STATUS_FINISHED;
		}
	}

}
