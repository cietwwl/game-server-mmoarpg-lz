package org.gof.demo.worldsrv.item;


public class ItemTypeKey {
	
	public static final int 装备 = 1;
	public static final int 道具 = 2;
	public static final int 宝石 = 3;
	public static final int 垃圾 = 4;
	public static final int 材料 = 5;
	public static final int 伙伴碎片 = 6;
	public static final int 伙伴装备 = 7;

	/* 装备子类型 */
	public static final int IT_装备_武器 = 1;
	public static final int IT_装备_衣服 = 2;
	public static final int IT_装备_帽子 = 3;
	public static final int IT_装备_鞋子 = 4;
	public static final int IT_装备_项链 = 5;
	public static final int IT_装备_戒指 = 6;
	public static final int IT_装备_翅膀 = 7;
	
	/* 道具子类型 */
	public static final int IT_道具_随机礼包 = 3;
	public static final int IT_道具_固定礼包 = 4;
	public static final int IT_道具_加虚拟品 = 5;
	public static final int IT_道具_任务道具 = 7; // 有限制条件的使用
	public static final int IT_道具_任务发布 = 8; // 打开一个任务UI，发布一个可接任务
	public static final int IT_道具_钥匙 = 9;
	public static final int IT_道具_碎片 = 10;
	public static final int IT_道具_立即增加虚拟品 = 11; // 不进包就使用的
	public static final int IT_道具_伙伴经验丹 = 12;
	public static final int IT_道具_增加背包格子 = 13;
	
	/* 垃圾类子类型（客户端根据此类型进行UI显示）*/
	public static final byte IT_垃圾_垃圾 = 1;
	public static final byte IT_垃圾_任务 = 2;

}