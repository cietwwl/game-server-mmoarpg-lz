package org.gof.demo.worldsrv.competition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.scheduler.ScheduleMethod;
import org.gof.core.support.Param;
import org.gof.core.support.RandomUtils;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.common.DataResetService;
import org.gof.demo.worldsrv.common.GamePort;
import org.gof.demo.worldsrv.common.GameServiceBase;
import org.gof.demo.worldsrv.config.ConfCharacterGeneral;
import org.gof.demo.worldsrv.config.ConfCharacterRobot;
import org.gof.demo.worldsrv.config.ConfCompeteAward;
import org.gof.demo.worldsrv.config.ConfEquipBaseData;
import org.gof.demo.worldsrv.config.ConfPartsData;
import org.gof.demo.worldsrv.config.ConfRobotEquip;
import org.gof.demo.worldsrv.config.ConfRobotEquipGroup;
import org.gof.demo.worldsrv.entity.CompetitionHuman;
import org.gof.demo.worldsrv.entity.CompetitionLog;
import org.gof.demo.worldsrv.entity.CompetitionMirror;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Part;
import org.gof.demo.worldsrv.mail.MailManager;
import org.gof.demo.worldsrv.rank.RankGlobalServiceProxy;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;

/**
 * 竞技场
 */
@DistrClass(
	servId = D.SERV_COMPETITION,
	importClass = {Human.class, List.class, CompetitionHuman.class, CompetitionMirror.class, HumanObject.class, Human.class}
)
public class CompetitionService extends GameServiceBase {
	
	private Map<Long, CompetitionHumanObj> humans = new HashMap<Long, CompetitionHumanObj>();
	//竞技场排行(此排行只做随机对手用）
	private LinkedList<CompetitionHuman> competRank = new LinkedList<CompetitionHuman>();
	private int competRankNum;
	
	public CompetitionService(GamePort port) {
		super(port);
	}

	@Override
	protected void init() {
		//获得数量
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.countBy(false, CompetitionHuman.tableName);
		Param param = prx.waitForResult();
		int count = param.get();
		if (count < 50) addRobot(50);
		
		//获得竞技场玩家数据
		List<Record> records;
		prx.findAll(CompetitionHuman.tableName);
		Param param1 = prx.waitForResult();
		records = param1.get();
		for(Record r : records) {
			CompetitionHumanObj obj = new CompetitionHumanObj();
			CompetitionHuman human = new CompetitionHuman(r);
			obj.human = human;
			
			// 取镜像
			prx.findBy(true, CompetitionMirror.tableName, CompetitionMirror.K.humanID, human.getHumanId());
			Param param2 = prx.waitForResult();
			List<Record> records2 = param2.get();
			for(Record r2 : records2) {
				CompetitionMirror mirror = new CompetitionMirror(r2);
				if (mirror.isHuman()) {
					obj.humanMirror = mirror;
				} else {
					obj.gensMirror.add(mirror);
				}
			}
			
			humans.put(human.getHumanId(), obj);
			competRank.add(human);
		}
		competRankNum = competRank.size();
		Collections.sort(competRank, new Comparator<CompetitionHuman>() {
			@Override
			public int compare(CompetitionHuman u1, CompetitionHuman u2) {
				if(u1.getRank() < u2.getRank())
					return -1;
				else {
					return 1;
				}
			}
		});
		
	}

	/**
	 * 增加机器人
	 * @param num
	 */
	public void addRobot(int num) {
		// 生成机器人装备
		Collection<ConfCharacterRobot> robots = ConfCharacterRobot.findAll();
		if (robots.size() < num) {
			System.out.print("配置表中机器人数量不足");
		}
		int robotNum = 0;
		for (ConfCharacterRobot confRobot : robots) {
//			System.out.println(confRobot.name);
			// 生成基本数据
			CompetitionHuman competHuman = new CompetitionHuman();
			competHuman.setId(confRobot.sn);
			competHuman.setHumanId(confRobot.sn);
			competHuman.setCharacterSn(confRobot.characterSn);
			competHuman.setHumanName(confRobot.name);
			competHuman.setHumanLevel(confRobot.level);
			competHuman.setCombat(confRobot.combat);
			competHuman.setIsRobot(true);
			competHuman.setRank(confRobot.rank);
			competHuman.setRankDaily(confRobot.rank);
			competHuman.persist();
			// 生成机器人装备位和装备
			Collection<ConfPartsData> parts = ConfPartsData.findAll();
			// 生成装备位
			for (ConfPartsData confPartsData : parts) {
				// 赋初始值
				Part part = new Part();
				part.setId(Port.applyId());
				part.setHumanId(confRobot.sn);
				part.setSn(confPartsData.sn);
				part.setGemsIds("{}");
				part.setChongxingLv("{}");
				part.setPropJson("{}");
				// 存数据库
				part.persist();
			}
			// 生成装备
			ConfRobotEquipGroup confEquipGroup = ConfRobotEquipGroup.get(confRobot.equipGroupSn);
			for (int robotEquipId : confEquipGroup.equipIds) {
				ConfRobotEquip confRobotEquip = ConfRobotEquip.get(robotEquipId);
				int equipId = confRobotEquip.equipId;
//				System.out.println(equipId);
				int partSn = ConfEquipBaseData.get(equipId).partSn;
				DBServiceProxy prx = DBServiceProxy.newInstance();
				// 取装备位
				prx.findBy(true, Part.tableName, Part.K.humanId, confRobot.sn, Part.K.sn, partSn);
				Param param = prx.waitForResult();
				List<Record> records = param.get();
				for(Record r : records) {
					Part part = new Part(r);
					part.setQianghuaLv(confRobotEquip.qiangHua);
					Map<String, Integer> chongXingLv = new HashMap<>();
					chongXingLv.put(String.valueOf(confRobotEquip.chongXing[0]), confRobotEquip.chongXing[1]);
					part.setChongxingLv(Utils.toJSONString(chongXingLv));
					Map<String, Integer> gem = new HashMap<>();
					gem.put(String.valueOf(confRobotEquip.gem[0]), confRobotEquip.gem[1]);
					part.setGemsIds(Utils.toJSONString(gem));
					// 计算装备位属性
				}
			}
			// 生成伙伴
			int index = 0;
			for (int genSn : confRobot.general) {
				ConfCharacterGeneral confCharacterGeneral = ConfCharacterGeneral.get(String.valueOf(genSn));
				GeneralObject robotGeneralObj = new GeneralObject(null, confCharacterGeneral);
				CompetitionManager.inst().unitToMirror(robotGeneralObj, confRobot.sn, confRobot.characterSn, index);
				index++;
			}
			HumanObject robotObj = new HumanObject(null, confRobot);
			CompetitionManager.inst().unitToMirror(robotObj, confRobot.sn, confRobot.characterSn, -1);
			
			// 判断数量是否足够
			robotNum++;
			if (robotNum == num) break;
		}
	}
	
	/**
	 * 交换排名
	 * @param attackerId
	 * @param defenderId
	 * @param win
	 */
	@DistrMethod
	public void swapRank(long attackerId, long defenderId, boolean win) {
		CompetitionHuman attacker = humans.get(attackerId).human;
		CompetitionHuman defender = humans.get(defenderId).human;
		int attRank = attacker.getRank();
		int defRank = defender.getRank();
		boolean isSwap = false;
		
		// 如果输了且排名靠前, 或者赢了且排名靠后，交换！
		if((!win && attRank < defRank) || (win && attRank > defRank)) {
			// 交换排名
			competRank.set(attRank-1, defender);
			competRank.set(defRank-1, attacker);
			attacker.setRank(defRank);
			defender.setRank(attRank);
			isSwap = true;
		}
		
		// 记录挑战历史
		CompetitionLog log = new CompetitionLog();
		
		log.setId(Port.applyId());
		log.setCreatedTime(Port.getTime());
		log.setAttackerId(attackerId);
		log.setDefenderId(defenderId);
		log.setAttackerName(attacker.getHumanName());
		log.setDefenderName(defender.getHumanName());
		log.setAttackerLevel(attacker.getHumanLevel());
		log.setDefenderLevel(defender.getHumanLevel());
		log.setAttackerModelSn(attacker.getCharacterSn());
		log.setDefenderModelSn(defender.getCharacterSn());
		log.setAttackerRank(attacker.getRank());
		log.setDefenderRank(defender.getRank());
		log.setWin(win);
		
		log.persist();
		port.returns("attacker", attacker, "defender", defender, "isSwap", isSwap);
	}
	
	/**
	 * 返回竞技场玩家数量
	 * @param humanId
	 */
	@DistrMethod
	public void getCompetRankNum() {
		port.returns(competRankNum);
	}
	
	/**
	 * 返回玩家竞技场信息
	 * @param humanId
	 */
	@DistrMethod
	public void getHumanRankInfo(long humanId) {
		CompetitionHuman human = humans.get(humanId).human;
		port.returns(human);
	}
	
	/**
	 * 返回玩家竞技场信息
	 * @param humanId
	 */
	@DistrMethod
	public void getHumanObjInfo(long humanId) {
		CompetitionHumanObj human = humans.get(humanId);
		port.returns(human);
	}
	
	/**
	 * 返回竞技场对手数据
	 * @param attackerId
	 */
	@DistrMethod
	public void getCompeteInfo(long attackerId) {
		
		CompetitionHuman human = humans.get(attackerId).human;
		int curRank = human.getRank();
		Set<Integer> ranks = new HashSet<>();
		if (curRank <= 3) {
			// 如果排名小于等于3，那么显示前三
			for (int i = 0; i < 3; i++) {
				ranks.add(i);
			}
		} else {
			// 返回高于自己排名的3个玩家
			while (ranks.size()<3) {
				ranks.add(RandomUtils.nextInt(curRank));
			}
		}
		List<CompetitionHuman> infos = new ArrayList<>();
		for (int rank : ranks) {
			infos.add(competRank.get(rank));
		}

		port.returns("humans", infos);
	}
	
	/**
	 * 返回爬塔对手数据
	 * @param attackerId
	1、	以玩家名次的正负20名为一个区间，该区间内随机取出一个人的数据为对手id5。
	2、	在比id5名次靠后的所有人中，取4名玩家，根据排名高低，安排到id1-4中。
	3、	当比id5名次靠后的人，不足4人时，则取全所有比id5名次靠后的人后，取id5名次-1的人。
	4、	id1-15的数据，各个名次相差需要超过30名，如数据量不足以支持30*2时，则不考虑此条要求。
	5、	排除掉以前取过的人，在比id5名次靠前的剩余所有人中，取10名玩家，根据排名高低，安排到id6-15中。
	6、	当比id5名次靠前的人，不足10名玩家时，则取全所有比id5名次靠前的人后，取id5名次+1的人
	 */
	@DistrMethod
	public void getTowerInfo(long humanId) {
		int id5Range = ConfGlobalUtils.getValue(ConfGlobalKey.爬塔id5的区间);
		int deltaRange = ConfGlobalUtils.getValue(ConfGlobalKey.爬塔随机对手的间隔范围);
		TreeSet<Integer> ranks = new TreeSet<>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		});
		CompetitionHuman human = humans.get(humanId).human;
		// 找基准点
		int leftRange = (human.getRank()-id5Range>=0 ? human.getRank()-id5Range : 0);
		int rightRange = (human.getRank()+id5Range<=competRankNum ? human.getRank()+id5Range : competRankNum);
		int anchor = 0;
		while(true) {
			anchor = RandomUtils.nextInt(rightRange-leftRange)+leftRange;
			if (anchor != human.getRank()) break;
		}
		ranks.add(anchor);
		// 找id1~id4
		int point1 = anchor;
		while (point1+deltaRange<competRankNum) {
			int rank = RandomUtils.nextInt(deltaRange)+point1;
			if (anchor != human.getRank()) ranks.add(rank);
			if (ranks.size() >=5) break;
			point1+=deltaRange;
		}
		if (ranks.size() < 5) {
			// 如果小于间隔范围
			if (competRankNum-point1 <= deltaRange) {
				if (competRankNum-point1 > 4) {
					while(ranks.size()<5) {
						int rank = RandomUtils.nextInt(competRankNum-point1)+point1;
						if (rank != human.getRank()) ranks.add(rank);
					}
				} else {
					// 
					for (int i=point1; i<=competRankNum; i++) {
						if (i != human.getRank()) ranks.add(i);
					}
					// 不足的去上一个id取
					while(ranks.size()<5) {
						int rank = RandomUtils.nextInt(deltaRange)+point1-deltaRange;
						if (rank != human.getRank()) ranks.add(rank);
					}
				}
			} 
		}
		// 找id6~id15
		int point2 = anchor;
		while (point2-deltaRange>0) {
			int rank = RandomUtils.nextInt(deltaRange)+point2-deltaRange;
			if (anchor != human.getRank()) ranks.add(rank);
			if (ranks.size() >=15) break;
			point2-=deltaRange;
		}
		if (ranks.size() < 15) {
			// 如果小于间隔范围
			if (point2-deltaRange <= 0) {
				if (point2 > 10) {
					while(ranks.size()<15) {
						int rank = RandomUtils.nextInt(point2);
						if (rank != human.getRank()) ranks.add(rank);
					}
				} else {
					// 
					for (int i=0; i<=point2; i++) {
						if (i != human.getRank()) ranks.add(i);
					}
					// 不足的去下一个id取
					while(ranks.size()<15) {
						int rank = RandomUtils.nextInt(deltaRange)+point2;
						if (rank != human.getRank()) ranks.add(rank);
					}
				}
			} 
		}
		List<CompetitionHumanObj> list = new ArrayList<>();
		for (int rank : ranks) {
			CompetitionHuman competHuman = competRank.get(rank);
			CompetitionHumanObj competObj = humans.get(competHuman.getHumanId());
			list.add(competObj);
		}
		port.returns("list", list);
	}
	
	/**
	 * 加入新数据
	 * @param attackerId
	 */
	@DistrMethod
	public void addNew(Human human, CompetitionMirror humanMirror, List<CompetitionMirror> mirrorList) {
		CompetitionHuman competHuman = new CompetitionHuman();
		competHuman.setId(human.getId());
		competHuman.setHumanId(human.getId());
		competHuman.setCharacterSn(human.getSn());
		competHuman.setHumanName(human.getName());
		competHuman.setHumanLevel(human.getLevel());
		competHuman.setCombat(human.getCombat());
		competHuman.setIsRobot(false);
		competHuman.setRank(competRankNum+1);
		competHuman.setRankDaily(competRankNum+1);
		competHuman.persist();
		
		CompetitionHumanObj obj = new CompetitionHumanObj();
		obj.human = competHuman;
		obj.humanMirror = humanMirror;
		obj.gensMirror = mirrorList;
		humans.put(competHuman.getHumanId(), obj);
		competRank.add(competHuman);
		competRankNum++;
		
		// 测试的时候把新玩家加入到每日排行榜，正式的时候要删掉
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.add(competHuman);
	}
	
	/**
	 * 更新属性和伙伴
	 * @param attackerId
	 */
	@DistrMethod
	public void update(long humanId, String prop, List<CompetitionMirror> mirrorList) {
		CompetitionHumanObj obj = humans.get(humanId);
		obj.humanMirror.setBase(prop);
		for (CompetitionMirror genMirror : obj.gensMirror) {
			genMirror.remove();
		}
		obj.gensMirror = mirrorList;
		humans.put(humanId, obj);
		port.returns();
	}
	
	/**
	 * 更新玩家等级
	 * @param attackerId
	 */
	@DistrMethod 
	public void updateLevel(HumanObject humanObj) {
		CompetitionHuman competHuman = humans.get(humanObj.getHumanId()).human;
		competHuman.setHumanLevel(humanObj.getHuman().getLevel());
		for (CompetitionHuman human : competRank) {
			if(human.getHumanId() == humanObj.getHumanId()) {
				human.setHumanLevel(humanObj.getHuman().getLevel());
				break;
			}
		}
	}
	
	/**
	 * 9点保存前50排行榜 发放奖励
	 */
	@ScheduleMethod(DataResetService.CRON_DAY_21ST)
	public void awardDaily() {
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.deleteCompetRankData();
		ConfCompeteAward conf = null;
		Collection<ConfCompeteAward> confs = ConfCompeteAward.findAll();
		for (int i = 0; i < competRank.size(); i++) {
			CompetitionHuman human = competRank.get(i);
			// 镜像加入
			prx.add(human);
			for (ConfCompeteAward confTemp : confs) {
				if(human.getRankDaily() >= confTemp.min && human.getRankDaily() <= confTemp.max) {
					conf = confTemp;
					break;
				}
			}
			MailManager.inst().sendSysMail(human.getHumanId(), Port.applyId(), conf.mailId);
			human.setAwardTime(Port.getTime());
		}
	}
	
	// 保存这一时刻的排名
	@ScheduleMethod(DataResetService.CRON_DAY_21ST)
	public void saveRankDaily() {
		for (CompetitionHuman human : competRank) {
			human.setRankDaily(human.getRank());
		}
	}
}
