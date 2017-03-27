package org.gof.demo;
import org.gof.core.gen.GofGenFile;
import org.gof.core.InputStream;

@GofGenFile
public final class CommonSerializer{
	public static org.gof.core.interfaces.ISerilizable create(int id){
		switch(id){
			case 413774452:
				return new org.gof.demo.worldsrv.entity.UnitPropPlus();
			case 1740355799:
				return new org.gof.demo.worldsrv.quest.QuestVO();
			case 1189305158:
				return new org.gof.demo.worldsrv.entity.Item();
			case -1343463857:
				return new org.gof.demo.worldsrv.character.HumanObject();
			case 755816453:
				return new org.gof.demo.worldsrv.human.HumanGlobalInfo();
			case -768325023:
				return new org.gof.demo.worldsrv.entity.CompetitionHuman();
			case -513018349:
				return new org.gof.demo.worldsrv.team.TeamResult();
			case 185284555:
				return new org.gof.demo.worldsrv.character.HumanObjectMirr();
			case 2126199119:
				return new org.gof.demo.worldsrv.item.ItemVO();
			case 785795448:
				return new org.gof.demo.worldsrv.entity.CompetitionLog();
			case 1189406186:
				return new org.gof.demo.worldsrv.entity.Mail();
			case 2061271490:
				return new org.gof.demo.worldsrv.entity.Activity();
			case 1189495846:
				return new org.gof.demo.worldsrv.entity.Part();
			case -1787132070:
				return new org.gof.demo.worldsrv.entity.Human();
			case -791521323:
				return new org.gof.demo.worldsrv.entity.General();
			case -1722775083:
				return new org.gof.demo.worldsrv.friend.FriendObject();
			case 475407509:
				return new org.gof.demo.worldsrv.entity.TowerMirror();
			case 2036177903:
				return new org.gof.demo.worldsrv.team.TeamMemberObject();
			case 1402556280:
				return new org.gof.demo.worldsrv.support.ReasonResult();
			case 1148911508:
				return new org.gof.demo.worldsrv.entity.InstanceRank();
			case -526078819:
				return new org.gof.demo.worldsrv.entity.PocketLine();
			case -1863547448:
				return new org.gof.demo.worldsrv.support.GetSetGrowthList();
			case -1805928076:
				return new org.gof.demo.battlesrv.support.Vector2D();
			case 2083957003:
				return new org.gof.demo.worldsrv.entity.CompetitionMirror();
			case 1098421634:
				return new org.gof.demo.worldsrv.competition.CompetitionHumanObj();
			case 1908949373:
				return new org.gof.demo.worldsrv.entity.LevelRank();
			case -588457386:
				return new org.gof.demo.worldsrv.team.Team();
			case 512525270:
				return new org.gof.demo.battlesrv.skill.SkillTempInfo();
			case 525244103:
				return new org.gof.demo.worldsrv.entity.Monster();
			case 373336593:
				return new org.gof.demo.worldsrv.entity.Friend();
			case -285952209:
				return new org.gof.demo.worldsrv.shop.ShopVO();
			case -1805928045:
				return new org.gof.demo.battlesrv.support.Vector3D();
			case -1512212314:
				return new org.gof.demo.battlesrv.stageObj.UnitPropPlusMap();
			case -1876674624:
				return new org.gof.demo.battlesrv.stageObj.UnitDataPersistance();
			case 499634758:
				return new org.gof.demo.worldsrv.dailyliveness.LivenessVO();
			case -463062774:
				return new org.gof.demo.worldsrv.character.GeneralObject();
			case 389361637:
				return new org.gof.demo.worldsrv.entity.InstanceChapter();
			case 1580166821:
				return new org.gof.demo.worldsrv.item.Bag();
			case 1189097606:
				return new org.gof.demo.worldsrv.entity.Buff();
			case -1127237809:
				return new org.gof.demo.worldsrv.item.ItemPack();
			case -1776218826:
				return new org.gof.demo.worldsrv.entity.Tower();
			case -2134618737:
				return new org.gof.demo.worldsrv.shop.GoodsVO();
		}
		return null;
	}
	public static void init(){
		InputStream.setCreateCommonFunc(CommonSerializer::create);
	}
}

