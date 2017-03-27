package org.gof.demo.worldsrv.rank;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Time;
import org.gof.core.support.observer.Listener;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.entity.CompetitionHuman;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.InstanceRank;
import org.gof.demo.worldsrv.entity.LevelRank;
import org.gof.demo.worldsrv.msg.Msg.DHumanInfo;
import org.gof.demo.worldsrv.msg.Msg.DInstanceRank;
import org.gof.demo.worldsrv.msg.Msg.DLevelRank;
import org.gof.demo.worldsrv.msg.Msg.SCGetSeflRankPage;
import org.gof.demo.worldsrv.msg.Msg.SCInstanceRank;
import org.gof.demo.worldsrv.msg.Msg.SCLevelRank;
import org.gof.demo.worldsrv.msg.Msg.SCRankResult;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.observer.EventKey;


public class RankManager extends ManagerBase {
	public static RankManager inst() {
		return inst(RankManager.class);
	}
	
	@Listener(EventKey.HUMAN_UPGRADE)
	public void onHumanUpgrage(Param param) {
		HumanObject humanObj = param.get("humanObj");
		if(humanObj.getHuman().getLevel() < 10) {
			return;
		}
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.updateInstanceRankLevel(humanObj.id, humanObj.getHuman().getLevel());
	}
	
	/**
	 * 更新等级
	 * @param humanId
	 * @param level
	 * @param towerList
	 */
	public void updateInstanceRankLevel(long humanId, int level, LinkedList<InstanceRank> rankList) {
		for (InstanceRank instanceRank : rankList) {
			if(instanceRank.getHumanId() == humanId) {
				instanceRank.setHumanLevel(level);
				return;
			}
		}
	}
	
//	---------------------------------------副本排行相关---------------------------------------
	/**
	 * 插入一条副本新记录
	 * @param rankList
	 * @param rank
	 */
	public void addNewToRank(InstanceRank rank, LinkedList<InstanceRank> rankList) {
		if(rank == null) {
			return;
		}
		
		// 如果为0 那么添加
		if(rankList.size() == 0) {
			rankList.add(rank);
			rank.setRank(1);
			rank.persist();
			return;
		}
		// 当前排行榜中有没有自己
		boolean self = false;
		int oldIndex = rankList.size();
		InstanceRank copyRank = null;
		for (InstanceRank instanceRank : rankList) {
			if (rank.getHumanId() == instanceRank.getHumanId()) {
				rank.setRank(instanceRank.getRank());
				oldIndex = rank.getRank()-1;
				self = true;
				copyRank = instanceRank;
				break;
			}
		}
		if (self) {
			rankList.remove(oldIndex);
			copyRank.remove();
			// 如果为0 那么添加
			if(rankList.size() == 0) {
				rankList.add(rank);
				rank.setRank(1);
				rank.persist();
				return;
			}
		} 
		// 二分查找索引
		int newIndex = binarySearch(rankList, rank, 0, rankList.size()-1);
		rankList.add(newIndex, rank);
		rank.setRank(newIndex+1);
		rank.persist();
		for (int i = newIndex; i <= oldIndex; i++) {
			InstanceRank r = rankList.get(i);
			r.setRank(i+1);
		}
	}
	
	/**
	 * 二分查找插入
	 * @param rankList
	 * @param rank
	 * @param from
	 * @param to
	 */
	public int binarySearch(List<InstanceRank> rankList, InstanceRank rank, int from, int to) {
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (rankList.get(middle).getStars() >= rank.getStars()) {
				return binarySearch(rankList, rank, middle+1, to);
			} else {
				return binarySearch(rankList, rank, from, middle-1);
			}
		} else {
			if (rankList.get(from).getStars() >= rank.getStars()) {
				return from+1;
			} else {
				return from;
			}
		}
	}
	
	/**
	 * 降序排序副本排行榜
	 * @param rankList
	 */
	public void sortInstanceRank(LinkedList<InstanceRank> rankList) {
		Collections.sort(rankList, new Comparator<InstanceRank>() {
			@Override
			public int compare(InstanceRank u1, InstanceRank u2) {
				if(u1 == null || u2 == null)
					return 0;
				if(u1.getRank() > u2.getRank())
					return -1;
				else if(u1.getRank() < u2.getRank())
					return 1;
				else
					return (int)((u1.getRankTime() - u2.getRankTime()) / Time.SEC);
			}
		});
	}
	
	/**
	 * 星星排行榜
	 * @param humanObj
	 */
	public void instanceRank(HumanObject humanObj) {
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.getRankList(RankKey.InstanceRank, 0, RankKey.InstanceRank.getSize());
		prx.listenResult(this::_result_instanceRank, "humanObj", humanObj);
	}
	
	public void _result_instanceRank(Param results, Param context) {
		SCInstanceRank.Builder msg = SCInstanceRank.newBuilder();
		HumanObject humanObj = context.get("humanObj");
		List<InstanceRank> rankList = results.get("rankList");
		for (InstanceRank rank : rankList) {
			DInstanceRank.Builder dRank = DInstanceRank.newBuilder();
			dRank.setHumanLevel(rank.getHumanLevel());
			dRank.setHumanName(rank.getHumanName());
			dRank.setRank(rank.getRank());
			dRank.setStars(rank.getStars());
			dRank.setCharacterSn(rank.getCharacterSn());
			msg.addRank(dRank);
		}
		
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.getInstanceRankIndex(humanObj.getHumanId());
		prx.listenResult(this::_result_instanceRank2, "humanObj", humanObj, "msg", msg);
	}

	public void _result_instanceRank2(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		SCInstanceRank.Builder msg = context.get("msg");
		int rank = results.get("rank");
		msg.setSelfRank(rank);
		humanObj.sendMsg(msg);
	}
	
//	---------------------------------------等级排行相关---------------------------------------
	/**
	 * 插入一条等级新记录
	 * @param rankList
	 * @param rank
	 */
	public void addNewToRank(LevelRank rank, LinkedList<LevelRank> rankList) {
		if(rank == null) {
			return;
		}
		
		// 如果为0 那么添加
		if(rankList.size() == 0) {
			rankList.add(rank);
			rank.setRank(1);
			rank.persist();
			return;
		}
		// 当前排行榜中有没有自己
		boolean self = false;
		int oldIndex = rankList.size();
		LevelRank copyRank = null;
		for (LevelRank levelRank : rankList) {
			if (rank.getHumanId() == levelRank.getHumanId()) {
				rank.setRank(levelRank.getRank());
				oldIndex = rank.getRank()-1;
				self = true;
				copyRank = levelRank;
				break;
			}
		}
		if (self) {
			rankList.remove(oldIndex);
			copyRank.remove();
			// 如果为0 那么添加
			if(rankList.size() == 0) {
				rankList.add(rank);
				rank.setRank(1);
				rank.persist();
				return;
			}
		} 
		// 二分查找索引
		int newIndex = binarySearch(rankList, rank, 0, rankList.size()-1);
		rankList.add(newIndex, rank);
		rank.setRank(newIndex+1);
		rank.persist();
		for (int i = newIndex; i <= oldIndex; i++) {
			LevelRank r = rankList.get(i);
			r.setRank(i+1);
		}
	}
	
	/**
	 * 二分查找插入
	 * @param rankList
	 * @param rank
	 * @param from
	 * @param to
	 */
	public int binarySearch(List<LevelRank> rankList, LevelRank rank, int from, int to) {
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (rankList.get(middle).getHumanLevel() >= rank.getHumanLevel()) {
				return binarySearch(rankList, rank, middle+1, to);
			} else {
				return binarySearch(rankList, rank, from, middle-1);
			}
		} else {
			if (rankList.get(from).getHumanLevel() >= rank.getHumanLevel()) {
				return from+1;
			} else {
				return from;
			}
		}
	}
 	

	
	/**
	 * 降序排序等级排行榜
	 * @param rankList
	 */
	public void sortLevelRank(LinkedList<LevelRank> rankList) {
		Collections.sort(rankList, new Comparator<LevelRank>() {
			@Override
			public int compare(LevelRank u1, LevelRank u2) {
				if(u1 == null || u2 == null)
					return 0;
				if(u1.getHumanLevel() > u2.getHumanLevel())
					return -1;
				else if(u1.getHumanLevel() < u2.getHumanLevel())
					return 1;
				else
					return (int)((u1.getRankTime() - u2.getRankTime()) / Time.SEC);
			}
		});
	}
	
	/**
	 * 等级排行榜
	 * @param humanObj
	 */
	public void levelRank(HumanObject humanObj) {
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.getRankList(RankKey.LevelRank, 0, RankKey.LevelRank.getSize());
		prx.listenResult(this::_result_levelRank, "humanObj", humanObj);
	}
	
	public void _result_levelRank(Param results, Param context) {
		SCLevelRank.Builder msg = SCLevelRank.newBuilder();
		HumanObject humanObj = context.get("humanObj");
		List<LevelRank> rankList = results.get("rankList");
		for (LevelRank rank : rankList) {
			DLevelRank.Builder dRank = DLevelRank.newBuilder();
			dRank.setHumanLevel(rank.getHumanLevel());
			dRank.setHumanName(rank.getHumanName());
			dRank.setRank(rank.getRank());
			dRank.setCharacterSn(rank.getCharacterSn());
			msg.addRank(dRank);
		}
		
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.getLevelRankIndex(humanObj.getHumanId());
		prx.listenResult(this::_result_levelRank2, "humanObj", humanObj, "msg", msg);
	}

	public void _result_levelRank2(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		SCLevelRank.Builder msg = context.get("msg");
		int rank = results.get("rank");
		msg.setSelfRank(rank);
		humanObj.sendMsg(msg);
	}
	
	@Listener({EventKey.HUMAN_UPGRADE, EventKey.HUMAN_FIRST_LOGIN})
	public void updateLevelRank(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		// 加入排行榜
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		LevelRank rank = new LevelRank();
		rank.setId(humanObj.id);
		rank.setHumanId(humanObj.id);
		rank.setCharacterSn(humanObj.getHuman().getSn());
		rank.setHumanLevel(human.getLevel());
		rank.setHumanName(human.getName());
		rank.setRankTime(Port.getTime());			
		proxy.addNew(RankKey.LevelRank, rank);
	}
//	---------------------------------------竞技场排行相关---------------------------------------	
	/**
	 * 降序排序副本排行榜
	 * @param rankList
	 */
	public void sortCompetRank(LinkedList<CompetitionHuman> rankList) {
		Collections.sort(rankList, new Comparator<CompetitionHuman>() {
			@Override
			public int compare(CompetitionHuman u1, CompetitionHuman u2) {
				if(u1.getRankDaily() < u2.getRankDaily())
					return -1;
				else 
					return 1;
			}
		});
	}
	
	/**
	 * 竞技场排行榜
	 * @param humanObj
	 */
	public void competitionRank(HumanObject humanObj, int page) {
		int num = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场排行榜每页显示人数);
		int start = num*(page-1);
		int end = num*page;
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.getRankList(RankKey.CompetitionHuman, start, end);
		prx.listenResult(this::_result_competitionRank, "humanObj", humanObj, "page", page);
	}
	
	public void _result_competitionRank(Param results, Param context) {
		SCRankResult.Builder msg = SCRankResult.newBuilder();
		HumanObject humanObj = context.get("humanObj");
		int page = context.get("page");
		int maxNum = results.get("maxNum");
		int totalPages = 0;
		if (maxNum%ConfGlobalUtils.getValue(ConfGlobalKey.竞技场排行榜每页显示人数) == 0) {
			totalPages = maxNum/ConfGlobalUtils.getValue(ConfGlobalKey.竞技场排行榜每页显示人数);
		} else {
			totalPages = 1 + maxNum/ConfGlobalUtils.getValue(ConfGlobalKey.竞技场排行榜每页显示人数);
		}
		msg.setTotalPages(totalPages);
		List<CompetitionHuman> rankList = results.get("rankList");
		for (CompetitionHuman rank : rankList) {
			DHumanInfo.Builder dRank = DHumanInfo.newBuilder();
			dRank.setLevel(rank.getHumanLevel());
			dRank.setName(rank.getHumanName());
			dRank.setRank(rank.getRank());
			dRank.setHumanSn(rank.getCharacterSn());
			dRank.setHumanId(rank.getHumanId());
			dRank.setCombat(rank.getCombat());
			msg.addInfo(dRank);
		}
		msg.setCurPage(page);
		humanObj.sendMsg(msg);

	}
	
	/**
	 * 得到自己的排行页
	 * @param humanObj
	 */
	public void getSelfRankPage(HumanObject humanObj) {
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.getCompetRankIndex(humanObj.getHumanId());
		prx.listenResult(this::_result_getSelfRankPage, "humanObj", humanObj);
	}
	
	public void _result_getSelfRankPage(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		int rank = results.get("rank");
		int curPage = 1 + rank/ConfGlobalUtils.getValue(ConfGlobalKey.竞技场排行榜每页显示人数);
		
		int num = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场排行榜每页显示人数);
		int start = num*(curPage-1);
		int end = num*curPage;
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.getRankList(RankKey.CompetitionHuman, start, end);
		prx.listenResult(this::_result_competitionRank2, "humanObj", humanObj, "curPage", curPage);
	}
	
	public void _result_competitionRank2(Param results, Param context) {
		SCGetSeflRankPage.Builder msg = SCGetSeflRankPage.newBuilder();
		HumanObject humanObj = context.get("humanObj");
		int curPage = context.get("curPage");
		int maxNum = results.get("maxNum");
		int totalPages = 0;
		if (maxNum%ConfGlobalUtils.getValue(ConfGlobalKey.竞技场排行榜每页显示人数) == 0) {
			totalPages = maxNum/ConfGlobalUtils.getValue(ConfGlobalKey.竞技场排行榜每页显示人数);
		} else {
			totalPages = 1 + maxNum/ConfGlobalUtils.getValue(ConfGlobalKey.竞技场排行榜每页显示人数);
		}
		msg.setTotalPages(totalPages);
		List<CompetitionHuman> rankList = results.get("rankList");
		for (CompetitionHuman rank : rankList) {
			DHumanInfo.Builder dRank = DHumanInfo.newBuilder();
			dRank.setLevel(rank.getHumanLevel());
			dRank.setName(rank.getHumanName());
			dRank.setRank(rank.getRank());
			dRank.setHumanSn(rank.getCharacterSn());
			dRank.setHumanId(rank.getHumanId());
			dRank.setCombat(rank.getCombat());
			msg.addInfo(dRank);
		}
		msg.setCurPage(curPage);
		humanObj.sendMsg(msg);
	}
	
	@Listener(EventKey.HUMAN_UPGRADE)
	public void updateCompetitionRank(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		// 加入排行榜
		if (human.getLevel() >= ConfGlobalUtils.getValue(ConfGlobalKey.竞技场开启等级)) {
			RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
			CompetitionHuman rank = new CompetitionHuman();
			rank.setId(humanObj.id);
			rank.setHumanId(humanObj.id);
			rank.setCharacterSn(humanObj.getHuman().getSn());
			rank.setHumanLevel(human.getLevel());
			rank.setHumanName(human.getName());
			rank.setRankTime(Port.getTime());			
			proxy.addNew(RankKey.CompetitionHuman, rank);
		}
	}
}
