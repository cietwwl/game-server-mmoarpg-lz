package org.gof.demo.worldsrv.competition;

import java.util.List;

import org.gof.core.support.Param;
import org.gof.core.support.Time;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.StageRandomUtils;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.GeneralObjectMirr;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.HumanObjectMirr;
import org.gof.demo.worldsrv.config.ConfCharacterModel;
import org.gof.demo.worldsrv.entity.CompetitionMirror;
import org.gof.demo.worldsrv.entity.General;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.stage.StagePort;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;

public class StageObjectCompetition extends StageObject{
	public boolean start = false;
	public long createdAt;				//创建
	public boolean destroy;			//是否正在摧毁
	public boolean isPass;			//是否已经通过
	public long defenderId; 			//防守者ID
	public HumanObject humanObjAtk;
	
	public static long DESTROY_TIME = 10 * Time.SEC;
	
	public StageObjectCompetition(HumanObject humanObj,StagePort port, long stageId, int stageSn, long defenderId) {
		super(port, stageId, stageSn);
		
		this.defenderId = defenderId;
		createdAt = this.getTime();
		
		//副本就一个大格子
		this.cellWidth = this.width;
		this.cellHeight = this.height;
		
		this.randUtils = new StageRandomUtils(100);
		
		this.humanObjAtk = humanObj;
	}
	
//	public void refreshLayer(int layer) {
//		ConfTower confTower = ConfTower.get(layer);
//		this.confTower = confTower;
//		this.layer = layer;
//	}
	@Override
	public void pulse() {
		if(!start) {
//			return;
		}
		
		super.pulse();
		
		long curr = this.getTime();
		
		//如果一定时间内副本没人 删除
		if(curr - this.createdAt > DESTROY_TIME) {
			//没人了直接删除地图
			if(this.getHumanObjs().isEmpty()) {
				this.destory();
			} 
		}
	}

	
	@Override
	public void createMosnter() {
		if(conf == null) {
			return;
		}
		
		CompetitionServiceProxy proxy = CompetitionServiceProxy.newInstance();
		proxy.getHumanObjInfo(defenderId);
		proxy.listenResult(this::_result_getHumanInfo);
		
	}
	
	public void _result_getHumanInfo(Param results, Param context) {
		
		CompetitionHumanObj tempHumanObj = results.get();
		
		CompetitionMirror competitionMirr = tempHumanObj.humanMirror;
		
		//Vector2D bornPos = StageManager.inst().getBirthPosFromMapSn(CompetitionManager.comptitionMapSn);
		
		Vector2D bornPos = new Vector2D(ConfGlobalUtils.getValue(ConfGlobalKey.镜像位置X坐标),ConfGlobalUtils.getValue(ConfGlobalKey.镜像位置Y坐标));
		
		//主角
		HumanObjectMirr humanObjMirr = new HumanObjectMirr(this);
		humanObjMirr.id = defenderId;
		humanObjMirr.sn = competitionMirr.getSn();
		humanObjMirr.profession = competitionMirr.getProfession();
		humanObjMirr.modelSn = competitionMirr.getModelSn();
		humanObjMirr.stageObj = this;
		humanObjMirr.posBegin = bornPos;
		humanObjMirr.teamBundleID = defenderId;
		humanObjMirr.name = competitionMirr.getName();
		humanObjMirr.confModel = ConfCharacterModel.get(humanObjMirr.modelSn);
		
		//----------------
		
		//战斗属性
		humanObjMirr.dataPers.unit = new Human();
		humanObjMirr.dataPers.unit.setId(id);
		humanObjMirr.getUnit().setHpCur(100);
		humanObjMirr.getUnit().setHpMax(100);
		humanObjMirr.getUnit().setMpCur(100);
		humanObjMirr.getUnit().setMpMax(100);
		humanObjMirr.getUnit().setModelSn(humanObjMirr.modelSn);
		humanObjMirr.getUnit().setSn(humanObjMirr.sn);
		humanObjMirr.getUnit().setName(humanObjMirr.name);
		humanObjMirr.getUnit().setProfession(humanObjMirr.profession);
		
		humanObjMirr.getUnit().setLevel(competitionMirr.getLevel());
		humanObjMirr.dataPers.unitPropPlus.setBase(competitionMirr.getBase());
		humanObjMirr.getUnit().setSkill(competitionMirr.getSkill());
		humanObjMirr.getUnit().setSkillGroupSn(competitionMirr.getSkillGroupSn());
		
		//初始化技能
		SkillManager.inst().initSkill(humanObjMirr);
		
		UnitManager.inst().propCalc(humanObjMirr, true);
		
		humanObjMirr.attackerId = humanObjAtk.id;
		humanObjMirr.setCompeteAI(humanObjAtk);
		
		humanObjMirr.startup();
		
		//主角的武将
		List<CompetitionMirror> gensMirror = tempHumanObj.gensMirror;
		for(CompetitionMirror mirr : gensMirror){
			
			GeneralObjectMirr genMirr = new GeneralObjectMirr(humanObjMirr,this);
			genMirr.id = mirr.getId();
			genMirr.sn = mirr.getSn();
			genMirr.name = mirr.getName();
			genMirr.teamBundleID = humanObjMirr.id;
			genMirr.modelSn = mirr.getModelSn();
			genMirr.confModel = ConfCharacterModel.get(mirr.getModelSn());
			
			
			//初始化数据
			genMirr.dataPers.unit = new General();
			genMirr.dataPers.unit.setId(id);
			genMirr.getUnit().setHpCur(100);
			genMirr.getUnit().setHpMax(100);
			genMirr.getUnit().setMpCur(100);
			genMirr.getUnit().setMpMax(100);
			genMirr.getUnit().setModelSn(mirr.getModelSn());
			genMirr.getUnit().setSn(mirr.getSn());
			genMirr.getUnit().setName(mirr.getName());
			genMirr.getUnit().setProfession(mirr.getProfession());
			
			genMirr.getUnit().setLevel(mirr.getLevel());
			genMirr.dataPers.unitPropPlus.setBase(mirr.getBase());
			genMirr.getUnit().setSkill(mirr.getSkill());
			genMirr.getUnit().setSkillGroupSn(mirr.getSkillGroupSn());
			genMirr.getUnit().setAttingIndex(mirr.getAttendIndex());
			//初始化技能
			SkillManager.inst().initSkill(genMirr);
			
			UnitManager.inst().propCalc(genMirr, true);
			
			genMirr.posBegin = bornPos;
			
			genMirr.startup();
		}
	}
	
	@Override
	public void destory() {
		if(this.destroy) return;
		//删除副本地图
		super.destory();
		this.destroy = true;
	}
}
