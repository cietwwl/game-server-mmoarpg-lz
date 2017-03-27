package org.gof.demo.worldsrv.rank;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gof.core.Record;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.entity.EntityBase;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.common.GamePort;
import org.gof.demo.worldsrv.common.GameServiceBase;
import org.gof.demo.worldsrv.entity.CompetitionHuman;
import org.gof.demo.worldsrv.entity.InstanceRank;
import org.gof.demo.worldsrv.entity.LevelRank;
import org.gof.demo.worldsrv.support.D;

@DistrClass(
		servId = D.SERV_RANK,
		importClass = {EntityBase.class, List.class, CompetitionHuman.class}
	)
	public class RankGlobalService extends GameServiceBase {

	//副本排行
	private LinkedList<InstanceRank> instanceList = new LinkedList<InstanceRank>();
	//等级排行
	private LinkedList<LevelRank> levelList = new LinkedList<LevelRank>();
	//竞技场每日排行
	private LinkedList<CompetitionHuman> competList = new LinkedList<CompetitionHuman>();
	private int competHumanNum;
	
	private Map<RankKey, List<EntityBase>> rankMap = Utils.ofMap(
			RankKey.InstanceRank, instanceList, RankKey.LevelRank, levelList, RankKey.CompetitionHuman, competList);
	
	public RankGlobalService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() {
		//遍历初始化各个排行榜
		DBServiceProxy prx = DBServiceProxy.newInstance();
		List<Record> records;
		//获得副本排行榜数量
		prx.findAll(InstanceRank.tableName);
		Param param1 = prx.waitForResult();
		records = param1.get();
		for(Record r : records) {
			InstanceRank rank = new InstanceRank(r);
			instanceList.add(rank);
		}
		if(instanceList.size() > 0) {
			RankManager.inst().sortInstanceRank(instanceList);
		}
		
		//获得等级排行榜数量
		prx.findAll(LevelRank.tableName);
		Param param2 = prx.waitForResult();
		records = param2.get();
		for(Record r : records) {
			LevelRank rank = new LevelRank(r);
			levelList.add(rank);
		}
		
		if(levelList.size() > 0) {
			RankManager.inst().sortLevelRank(levelList);
		}
		
		//获得竞技场每日排行榜（只在重启时取数据）
		prx.findAll(CompetitionHuman.tableName);
		Param param3 = prx.waitForResult();
		records = param3.get();
		for(Record r : records) {
//			if (competList.size()>=50) break;
			CompetitionHuman rank = new CompetitionHuman(r);
			competList.add(rank);
		}
		
		if(competList.size() > 0) {
			RankManager.inst().sortCompetRank(competList);
			competHumanNum = competList.size();
		}
	}
	
	@DistrMethod
	public void addNew(RankKey key, EntityBase entityAdd) {
		switch (key) {
			case InstanceRank:{
				RankManager.inst().addNewToRank((InstanceRank)entityAdd, instanceList);
			}break;
			case LevelRank:{
				RankManager.inst().addNewToRank((LevelRank)entityAdd, levelList);
			}break;
			default:{
				
			}break;
		}
	}
	
	@DistrMethod
	public void getRankList(RankKey key, int fromIndex, int toIndex) {
		List<EntityBase> rankList = rankMap.get(key);
		int size = rankList.size();
		fromIndex = Math.min(size, fromIndex);
		toIndex = Math.min(size, toIndex);
		
		port.returns("rankList", rankList.subList(fromIndex, toIndex), "maxNum", competHumanNum);
	}
	
	@DistrMethod
	public void getInstanceRankIndex(long humanId) {
		List<EntityBase> rankList = rankMap.get(RankKey.InstanceRank);
		for (EntityBase entityBase : rankList) {
			InstanceRank rank = (InstanceRank)entityBase;
			if (rank.getHumanId() == humanId) {
				port.returns("rank", rank.getRank());
			}
		}
	}
	
	@DistrMethod
	public void getLevelRankIndex(long humanId) {
		List<EntityBase> rankList = rankMap.get(RankKey.LevelRank);
		for (EntityBase entityBase : rankList) {
			LevelRank rank = (LevelRank)entityBase;
			if (rank.getHumanId() == humanId) {
				port.returns("rank", rank.getRank());
			}
		}
	}
	
	@DistrMethod
	public void getCompetRankIndex(long humanId) {
		List<EntityBase> rankList = rankMap.get(RankKey.CompetitionHuman);
		for (EntityBase entityBase : rankList) {
			CompetitionHuman rank = (CompetitionHuman)entityBase;
			if (rank.getHumanId() == humanId) {
				port.returns("rank", rank.getRank());
			}
		}
	}
	
	@DistrMethod 
	public void updateInstanceRankLevel(long humanId, int level) {
		//更新副本排行榜排行
		RankManager.inst().updateInstanceRankLevel(humanId, level, instanceList);
	}
	
	//清楚竞技场每日排行数据
	@DistrMethod 
	public void deleteCompetRankData() {
		competList.clear();
	}
	
	//增加镜像数据
	@DistrMethod 
	public void add(CompetitionHuman human) {
		competList.add(human);
		competHumanNum++;
	}
}
