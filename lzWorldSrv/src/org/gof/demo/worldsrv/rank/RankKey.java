package org.gof.demo.worldsrv.rank;

import org.gof.core.entity.EntityBase;
import org.gof.demo.worldsrv.entity.CompetitionHuman;
import org.gof.demo.worldsrv.entity.InstanceRank;
import org.gof.demo.worldsrv.entity.LevelRank;

public enum RankKey {
	//星星排行
	InstanceRank(InstanceRank.class, "stars",  50, "rankTime"),	
	//等级排行
	LevelRank(LevelRank.class, "humanLevel",  50, "rankTime"),	
	//竞技场排行
	CompetitionHuman(CompetitionHuman.class, "rankDaily",  50, ""),
	;
	
	private RankKey(Class<? extends EntityBase> clazz, String rankValue, int size, String rankValueExtra) {
		this.clazz = clazz;
		this.rankProp = rankValue;
		this.size = size;
		this.rankPropExtra = rankValueExtra;
	}
	
	private Class<? extends EntityBase> clazz;			//对应排行榜存储的表名
	private String rankProp;		//对应排行榜排行对应的排行属性
	private int size;				//排行榜大小
	private String rankPropExtra;				//第二排行属性（隐藏排行属性）目前只支持两个排序
	
	public Class<? extends EntityBase> getClazz() {
		return clazz;
	}
	public void setClazz(Class<? extends EntityBase> clazz) {
		this.clazz = clazz;
	}
	public String getRankProp() {
		return rankProp;
	}
	public void setRankProp(String rankProp) {
		this.rankProp = rankProp;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getRankPropExtra() {
		return rankPropExtra;
	}
	public void setRankPropExtra(String rankPropExtra) {
		this.rankPropExtra = rankPropExtra;
	}
	
}
