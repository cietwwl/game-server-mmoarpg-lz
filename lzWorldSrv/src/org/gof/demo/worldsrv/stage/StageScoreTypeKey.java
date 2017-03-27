package org.gof.demo.worldsrv.stage;

/**
 * 副本评分条件类型
 * @author g
 *
 */
public interface StageScoreTypeKey {
	
	public static final int TYPE_击杀指定怪物一定数量 = 1;
	
	public static final int TYPE_击杀所有怪物 = 2;
	
	public static final int TYPE_累计击杀一定数量怪物 = 3;
	
	public static final int TYPE_打开所有宝箱 = 4;//暂不考虑
	
	public static final int TYPE_获得指定道具指定数量 = 5;
	
	public static final int TYPE_不使用指定技能 = 6;
	
	public static final int TYPE_不触发指定机关 = 7;//暂不考虑
	
	public static final int TYPE_主角血量不低于 = 8;//只针对单人副本
	
	public static final int TYPE_全员血量不低于 = 9;//只针对单人副本
	
	public static final int TYPE_指定时间通关 = 10;//按照消耗时间,因为有的副本没有时间限制(单位:秒)
	
	public static final int TYPE_指定伙伴上阵 = 11;
	
	
}
