package org.gof.demo.worldsrv.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfCharacterHuman;
import org.gof.demo.worldsrv.config.ConfInborn;
import org.gof.demo.worldsrv.config.ConfSkill;
import org.gof.demo.worldsrv.config.ConfSkillGroup;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DInborn;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.SCAddRelSkillResult;
import org.gof.demo.worldsrv.msg.Msg.SCChangeInbornResult;
import org.gof.demo.worldsrv.msg.Msg.SCDelRelSkillResult;
import org.gof.demo.worldsrv.msg.Msg.SCInbornLevelupResult;
import org.gof.demo.worldsrv.msg.Msg.SCSkillInitResult;
import org.gof.demo.worldsrv.msg.Msg.SCSkillLevelupResult;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 技能，天赋，羁绊技能升级相关逻辑
 * 
 * @author GaoZhangCheng
 */
public class SkillInbornManager extends ManagerBase {
	
	/**o
	 * 获取实例
	 * 
	 * @return
	 */
	public static SkillInbornManager inst() {
		return inst(SkillInbornManager.class);
	}
	
	/**
	 * 技能天赋界面点击初始化
	 */
	public void skillInbornInit(HumanObject humanObj) {
		
		//取出所有的技能
		List<DSkill> dskillList = SkillManager.inst().getSkills(humanObj);
		
		//取出所有的天赋
		List<DInborn> dInbornList =  InbornVO.getMsgList(humanObj);
		
		//所有激活的羁绊技能
		List<DSkill> relDskillList = this.getRelSkillMsg(humanObj);
		
		//判断哪些是已经装备的羁绊技能
		List<DSkill> onRelDskill = this.getActiveRelSkillMsg(humanObj);
		
		//构建返回值
		SCSkillInitResult.Builder msg = SCSkillInitResult.newBuilder();
		msg.addAllSkills(dskillList);
		msg.addAllInborns(dInbornList);
		msg.addAllRelSkills(relDskillList);
		msg.addAllActiveRelSkills(onRelDskill);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 技能升级
	 * @param humanObj
	 * @param skillSn
	 */
	public void skillLevelup(HumanObject humanObj, int skillSn) {
		Human human = humanObj.getHuman();
		
		//获取技能配置
		ConfSkill conf = ConfSkill.get(skillSn);
		if(conf == null) {
			Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("技能配置错误，技能不存在{}", skillSn));
			return;
		}
		
		//判断技能的类型是否正确
		if(conf.activeType != 1){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("该技能不可以学习或者升级"));
			return;
		}
		
		//判断技能是否已经学习
		SkillCommon skill = humanObj.skills.get(skillSn);
		if(skill == null) {
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("您还没有学习这个技能"));
			return;
		}
		int level = skill.skillLevel;
		
		//基本不打大于基本差
		if(level > conf.levelCut + human.getLevel()) {
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("技能级别不能大于主角级别限制"));
			return;
		}
		
		//判断钱够不够
		int cost = conf.baseCost + conf.plusCost * (level + 1);
		ReasonResult result = HumanManager.inst().canProduceReduce(humanObj, ProduceMoneyKey.coin, cost);
		if(!result.success) {
			Inform.user(humanObj.getHumanId(), Inform.提示操作, "您没有足够的钱来升级技能");
			return;
		}
		
		//正经升级
		SkillManager.inst().addSkill(humanObj, skillSn, level + 1, conf.position);
		
		//给前端返回消息
		SCSkillLevelupResult.Builder msg = SCSkillLevelupResult.newBuilder();
		msg.setResult(true);
		DSkill.Builder dSkill = DSkill.newBuilder();
		dSkill.setSkillSn(skillSn);
		dSkill.setSkillLevel(level + 1);
		msg.setSkill(dSkill);
		humanObj.sendMsg(msg);
		
	}
	
	/**
	 * 升级天赋
	 * 
	 * @param humanObj
	 * @param InbornSn
	 */
	public void inbornLevelup(HumanObject humanObj, int inbornSn) {
		Human human = humanObj.getHuman();
		
		//获取天赋配置
//		ConfInborn conf = ConfInborn.get(inbornSn);
		ConfInborn conf = null;
		if(conf == null) {
			Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("天赋配置错误，天赋不存在{}", inbornSn));
			return;
		}
		
		//判断技能是否已经学习
		InbornVO vo = InbornVO.getBySn(humanObj.getInborns(), inbornSn);
		if(vo == null) {
			vo = new InbornVO(inbornSn, 0, 0, false);
		}
		
		//级别不打大于级别差
		if(vo.level > conf.levelCut + human.getLevel()) {
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("天赋级别不能大于主角级别限制"));
			return;
		}
		
		//判断钱够不够
		int cost = conf.baseCost + conf.growCost * (vo.level + 1);
		ReasonResult result = HumanManager.inst().canProduceReduce(humanObj, ProduceMoneyKey.coin, cost);
		if(!result.success) {
			Inform.user(humanObj.getHumanId(), Inform.提示操作, "您没有足够的钱来升级天赋");
			return;
		}
		
		//正经升级
		vo.level = vo.level+1;
		InbornVO.addInborn(vo.sn, vo.pos, vo.level, humanObj);
		
		//给前端返回消息
		SCInbornLevelupResult.Builder msg = SCInbornLevelupResult.newBuilder();
		msg.setResult(true);
		msg.setInborn(InbornVO.voToMsg(vo));
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 更改天赋
	 * @param humanObj
	 * @param pos
	 * @param sn
	 */
	public void inbornChange(HumanObject humanObj, int pos, int sn) {
		
		//获取天赋配置
		ConfInborn conf = ConfInborn.get(sn);
		if(conf == null) {
			Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("天赋配置错误，天赋不存在{}", sn));
			return;
		}
		
		//判断技能是否已经学习
		InbornVO voTemp = InbornVO.getBySn(humanObj.getInborns(), sn);
		if(voTemp == null) {
			Inform.user(humanObj.id, Inform.提示操作, "您还没有学习这个天赋");
			return;
		}
		
		//已经装配了天赋
		if(voTemp.pos > 0){
			Inform.user(humanObj.id, Inform.提示操作, "您已经装配了这个天赋");
			return;
		}
		
		//正经升级
		List<InbornVO> voList = InbornVO.getList(humanObj.getInborns());
		for (InbornVO vo : voList) {
			//将原来装配的干掉
			if(vo.pos == pos){
				vo.pos = 0;
			}
			
			//装备新的天赋
			if(vo.sn == sn) {
				vo.pos = pos;
				voTemp = vo;
			}
		}
		humanObj.getHuman().setInborn(InbornVO.listToString(voList));
		
		//给前端返回消息
		SCChangeInbornResult.Builder msg = SCChangeInbornResult.newBuilder();
		msg.setResult(true);
		msg.setInborn(InbornVO.voToMsg(voTemp));
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 玩家设置羁绊技能
	 * @param humanObj
	 * @param relSkillSn
	 * @param pos
	 */
	public void setRelSkill(HumanObject humanObj, int sn, int pos) {
		
		//技能不存在
		ConfSkill conf = ConfSkill.get(sn);
		if(conf == null){
			Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("羁绊配置错误，技能不存在{}", sn));
			return;
		}
		
		//判断这个技能是否处于激活状态
		if(!humanObj.relSkills.keySet().contains(sn)) {
			Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("您还没有激活这个技能{}", sn));
			return;
		}
		
		//已经装备
		if(humanObj.getSkills().contains(sn+"")){
			Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("您已经装备了这个技能{}", sn));
			return;
		}
		
		//先删除
		removeRelSkillByPos(humanObj, pos);
		
		//再装配新的
		int level = humanObj.relSkills.get(sn);
		SkillManager.inst().addSkill(humanObj, sn, level, pos);
		
		//返回消息体
		SCAddRelSkillResult.Builder msg = SCAddRelSkillResult.newBuilder();
		DSkill.Builder dSkill = DSkill.newBuilder();
		dSkill.setSkillSn(sn);
		dSkill.setSkillLevel(level);
		dSkill.setPosition(pos);
		msg.setRelSkills(dSkill);
		humanObj.sendMsg(msg);
	}

	/**
	 * 卸下某个羁绊技能
	 * @param humanObj
	 * @param relSkillSn
	 * @param pos
	 */
	public void delRelSkill(HumanObject humanObj, int sn, int pos) {
		
		//技能不存在
		ConfSkill conf = ConfSkill.get(sn);
		if(conf == null){
			Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("羁绊配置错误，技能不存在{}", sn));
			return;
		}
		
		//判断这个技能是否处于激活状态
		if(!humanObj.relSkills.keySet().contains(sn)) {
			Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("您还没有激活这个技能[{}]", sn));
			return;
		}
		
		//已经装备
		if(!humanObj.getSkills().contains(sn+"")){
			Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("您没有装备这个技能[{}]", sn));
			return;
		}
		
		//先删除
		removeRelSkillByPos(humanObj, pos);
		
		//返回消息体
		SCDelRelSkillResult.Builder msg = SCDelRelSkillResult.newBuilder();
		DSkill.Builder dSkill = DSkill.newBuilder();
		dSkill.setSkillSn(sn);
		dSkill.setSkillLevel(1);
		dSkill.setPosition(pos);
		msg.setRelSkills(dSkill);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 刷新玩家身上的羁绊技能
	 * @param param
	 */
	@Listener({EventKey.GENERAL_CREATE, EventKey.GENERAL_QUILITY_LEVELUP, EventKey.HUMAN_LOGIN})
	public void refreshRelSkill(Param param) {
		HumanObject humanObj = param.get("humanObj");
		
		//所有招募过的武将
		String generals = humanObj.getHuman().getAllGeneral();
		
		//所有的羁绊技能
		ConfCharacterHuman confHuman = ConfCharacterHuman.get(humanObj.getHuman().getSn());
		ConfSkillGroup confGroup = ConfSkillGroup.get(confHuman.skillGroupSn);
		int[] skills = confGroup.relSkill;
		
		//先设定结果集
		List<ConfSkill> activeSkills = new ArrayList<ConfSkill>();
		
		//挨个遍历匹配，看看技能是否激活
		for (int skillSn : skills) {
			ConfSkill confSkill = ConfSkill.get(skillSn);
			if(confSkill == null) {
				continue;
			}
			
			String[] relGeneralSn = confSkill.relGeneralSn;
			boolean find = true;
			for (String sn : relGeneralSn) {
				//如果有一个没有，就跳过
				if(!generals.contains(sn)) {
					find = false;
					break;
				}
			}
			
			//如果都有，就加入到列表里
			if(find) {
				activeSkills.add(confSkill);
			}
		}
		
		//如果没有，就清空返回
		if(activeSkills.size() == 0){
			humanObj.relSkills.clear();
			return;
		}
		
		//找到就装备到玩家身上 sn:level
		Map<Integer, Integer> res = new HashMap<Integer, Integer>();
		for (ConfSkill confSkill : activeSkills) {
			//找到最低的武将级别
			int level = 100;
			for (String sn : confSkill.relGeneralSn) {
				GeneralObject go = humanObj.getGeneralObjectBySn(sn);
				if(go == null){
					continue;
				}
				if(go.getGeneral().getLevel() < level) level = go.getGeneral().getLevel();
			}
			
			res.put(confSkill.sn, level);
		}
		humanObj.relSkills = res;
	}
	
	/**
	 * 提取激活的羁绊技能MSG队列
	 * @param humanObj
	 * @return
	 */
	public List<DSkill> getRelSkillMsg(HumanObject humanObj) {
		List<DSkill> res = new ArrayList<>();
		
		Map<Integer, Integer> skillMap  = humanObj.relSkills;
		for (Entry<Integer, Integer> entry : skillMap.entrySet()) {
			DSkill.Builder d = DSkill.newBuilder();
			d.setSkillSn(entry.getKey());
			d.setSkillLevel(entry.getValue());
			d.setPosition(0);
			res.add(d.build());
		}
		
		return res;
	}
	
	/**
	 * 提取所有已经装配的羁绊技能
	 * @param humanObj
	 * @return
	 */
	public List<DSkill> getActiveRelSkillMsg(HumanObject humanObj) {
		List<DSkill> res = new ArrayList<>();
		
		//取出所有的技能
		List<DSkill> dskillList = SkillManager.inst().getSkills(humanObj);
		//判断哪些是已经装备的羁绊技能
		for (DSkill dSkill : dskillList) {
			ConfSkill confTemp = ConfSkill.get(dSkill.getSkillSn());
			
			if(confTemp == null) continue;
			
			if(confTemp.activeType == 2){
				res.add(dSkill);
			}
		}
		
		return res;
	}
	
	/**
	 *  删除一个羁绊技能
	 * @param uniObj
	 * @param sn
	 */
	public void removeRelSkillByPos(UnitObject uniObj, int pos) {
		JSONArray ja = Utils.toJSONArray(uniObj.getSkills());
		
		JSONObject target = null;
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			int sn = jo.getInteger("sn");
			ConfSkill conf = ConfSkill.get(sn);
			
			//如果不是羁绊技能忽略
			if(conf.activeType !=2) continue;
			
			//如果位置一样，处理
			if(jo.getInteger("pos") == pos){
				target = jo;
				break;
			}
		}
		
		//没有就返回
		if(target == null) {
			return;
		}
		
		//重新设置技能
		ja.remove(target);
		uniObj.getUnit().setSkill(Utils.toJSONString(ja));
	}
}
