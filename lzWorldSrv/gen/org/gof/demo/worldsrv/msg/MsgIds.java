package org.gof.demo.worldsrv.msg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import org.gof.core.support.SysException;

public class MsgIds {
	public static final int SCMsgFill = 101;
	public static final int CSLogin = 111;
	public static final int SCLoginResult = 112;
	public static final int CSAccountReconnect = 121;
	public static final int SCAccountReconnectResult = 122;
	public static final int CSChangeModel = 141;
	public static final int SCChangeModel = 142;
	public static final int CSAccountRandomName = 151;
	public static final int SCAccountRandomName = 152;
	public static final int CSQueryCharacters = 1003;
	public static final int SCQueryCharactersResult = 1004;
	public static final int CSCharacterCreate = 1005;
	public static final int SCCharacterCreateResult = 1006;
	public static final int CSCharacterDelete = 1007;
	public static final int SCCharacterDeleteResult = 1008;
	public static final int CSCharacterLogin = 1009;
	public static final int SCCharacterLoginResult = 1010;
	public static final int SCInitData = 1101;
	public static final int SCHumanKick = 1200;
	public static final int SCHumanInfoChange = 1108;
	public static final int SCStageObjectInfoChange = 1109;
	public static final int CSHumanInfo = 1110;
	public static final int SCHumanInfo = 1111;
	public static final int SCDieStage = 1113;
	public static final int SCResult = 1119;
	public static final int CSStageEnter = 1201;
	public static final int SCStageEnterResult = 1202;
	public static final int SCStageEnterEnd = 1218;
	public static final int CSStageSwitch = 1203;
	public static final int SCStageSwitch = 1204;
	public static final int CSStageMove = 1211;
	public static final int SCStageMove = 1212;
	public static final int SCStageSetPos = 1213;
	public static final int CSStageMoveStop = 1214;
	public static final int SCStageMoveStop = 1215;
	public static final int SCStageObjectAppear = 1216;
	public static final int SCStageObjectDisappear = 1217;
	public static final int SCStageMoveTeleport = 1220;
	public static final int CSStageMove2 = 1222;
	public static final int SCStagePullTo = 1226;
	public static final int SCUnitobjStatusChange = 1227;
	public static final int SCStageObjectLevelUp = 1228;
	public static final int SCSceneInit = 1251;
	public static final int SCScenePlotChange = 1252;
	public static final int CSSceneTrigger = 1253;
	public static final int CSSceneEvent = 1254;
	public static final int SCSceneEventStart = 1255;
	public static final int CSFightAtk = 1301;
	public static final int SCFightAtkResult = 1302;
	public static final int SCFightSkill = 1303;
	public static final int SCFightHpChg = 1304;
	public static final int CSFightRevive = 1310;
	public static final int SCFightRevive = 1350;
	public static final int SCFightStageChange = 1311;
	public static final int SCFightDotHpChg = 1312;
	public static final int DRageAdd = 1313;
	public static final int SCFightBulletHpChg = 1316;
	public static final int SCFightBulletMove = 1317;
	public static final int CSSkillInterrupt = 1318;
	public static final int SCSkillInterrupt = 1319;
	public static final int CSSkillAddGeneral = 1320;
	public static final int CSSkillRemoveGeneral = 1321;
	public static final int CSSkillAddGeneralToUnion = 1322;
	public static final int CSUnionFightStart = 1323;
	public static final int CSUnionFightAIPause = 1324;
	public static final int CSUnionFightAIUnpause = 1325;
	public static final int CSUnionFightSpecial = 1326;
	public static final int CSUnionFightAuto = 1327;
	public static final int SCFightSkillTeamCancel = 1328;
	public static final int SCBagUpdate = 1400;
	public static final int CSUseItem = 1401;
	public static final int CSArrangeBag = 1403;
	public static final int CSItemBachSell = 1405;
	public static final int CSBagExpand = 1407;
	public static final int SCBagExpand = 1408;
	public static final int CSInformChat = 1501;
	public static final int SCInformMsg = 1502;
	public static final int SCInformMsgAll = 1504;
	public static final int SCBuffAdd = 1802;
	public static final int SCBuffUpdate = 1803;
	public static final int SCBuffDispel = 1804;
	public static final int CSBuffDispelByHuman = 1805;
	public static final int CSGeneralList = 1901;
	public static final int SCGeneralList = 1902;
	public static final int CSGeneralInfo = 1903;
	public static final int SCGeneralInfo = 1904;
	public static final int CSGeneralRecruit = 1905;
	public static final int SCGeneralRecruitResult = 1906;
	public static final int CSGeneralExpAdd = 1907;
	public static final int SCGeneralExpAdd = 1908;
	public static final int CSGeneralStarUp = 1909;
	public static final int SCGeneralStarUp = 1910;
	public static final int CSGeneralQualityUp = 1911;
	public static final int SCGeneralQualityUp = 1912;
	public static final int CSGeneralEquipUp = 1913;
	public static final int SCGeneralEquipUp = 1914;
	public static final int CSFragInfo = 1915;
	public static final int SCFragInfo = 1916;
	public static final int CSOneFragInfo = 1917;
	public static final int SCOneFragInfo = 1918;
	public static final int CSSellFrag = 1919;
	public static final int SCSellFrag = 1920;
	public static final int CSGenTaskFightTimes = 1921;
	public static final int SCGenTaskFightTimes = 1922;
	public static final int CSEnterGeneralTask = 1923;
	public static final int SCEnterGeneralTask = 1924;
	public static final int SCGeneralTaskFightResult = 1925;
	public static final int SCGeneralTaskReward = 1926;
	public static final int CSGeneralToAttIng = 1927;
	public static final int SCGeneralToAttIng = 1928;
	public static final int CSGeneralInfoAttIng = 1929;
	public static final int SCGeneralInfoAttIng = 1930;
	public static final int SCGeneralRemove = 1931;
	public static final int CSGeneralChooseFirst = 1932;
	public static final int SCGeneralChooseFirstResult = 1933;
	public static final int CSSignIn = 2000;
	public static final int SCSignIn = 2001;
	public static final int CSCommitQuestNormal = 2101;
	public static final int SCCommitQuestNormalResult = 2121;
	public static final int CSCommitQuestDaily = 2102;
	public static final int CSOpenQuest = 2103;
	public static final int CSOpenQuestDaily = 2104;
	public static final int SCQuestInfo = 2105;
	public static final int CSOpenQuestInstDaily = 2106;
	public static final int SCQuestInstDailyCount = 2107;
	public static final int CSCommitQuestInstDaily = 2108;
	public static final int SCQuestInstDailyInfo = 2109;
	public static final int CSInstanceEnter = 2201;
	public static final int CSInstanceLeave = 2202;
	public static final int CSInstanceEnd = 2203;
	public static final int SCInstanceEnd = 2204;
	public static final int SCUpdateChapter = 2207;
	public static final int SCAllChapter = 2208;
	public static final int CSInstanceAuto = 2209;
	public static final int SCInstanceAuto = 2210;
	public static final int CSBoxAward = 2212;
	public static final int SCBoxAward = 2213;
	public static final int CSInstanceLottery = 2214;
	public static final int SCInstanceLottery = 2215;
	public static final int CSChangeName = 2301;
	public static final int SCChangeNameResult = 2302;
	public static final int SCChangeNameQuestFinish = 2303;
	public static final int CSChangeNameRandom = 2304;
	public static final int SCChangeNameRandomResult = 2305;
	public static final int SCAddMail = 2401;
	public static final int CSMailList = 2402;
	public static final int SCMailList = 2403;
	public static final int CSReadMail = 2404;
	public static final int CSPickupMail = 2405;
	public static final int SCPickupMail = 2406;
	public static final int CSPickupAllMail = 2407;
	public static final int SCPickupAllMail = 2408;
	public static final int CSClearMail = 2409;
	public static final int SCDeleteMail = 2410;
	public static final int CSCheckMailRemoveable = 2411;
	public static final int SCCheckMailRemoveable = 2412;
	public static final int CSRsvLivenessAwards = 2501;
	public static final int SCRsvLivenessAwardsResult = 2502;
	public static final int CSOpenLivenessUI = 2503;
	public static final int SCLivenessInfo = 2504;
	public static final int CSEquipUp = 2601;
	public static final int SCEquipUp = 2602;
	public static final int CSEquipDown = 2603;
	public static final int SCEquipDown = 2604;
	public static final int CSEquipReclaim = 2605;
	public static final int SCEquipReclaim = 2606;
	public static final int SCEquipChangeBoardcast = 2608;
	public static final int CSPartQianghua = 2609;
	public static final int SCPartQianghua = 2610;
	public static final int CSPartChongxing = 2611;
	public static final int SCPartChongxing = 2612;
	public static final int CSGemUp = 2701;
	public static final int CSGemUpAll = 2703;
	public static final int SCSetGem = 2704;
	public static final int CSGemDown = 2705;
	public static final int CSGemDownAll = 2707;
	public static final int CSGemComposite = 2709;
	public static final int SCGemComposite = 2710;
	public static final int CSGemCompositeTop = 2711;
	public static final int SCGemCompositeTop = 2712;
	public static final int CSGemCompositeAllTop = 2713;
	public static final int SCGemCompositeAllTop = 2714;
	public static final int CSGatcha = 2801;
	public static final int CSGatcha10 = 2802;
	public static final int SCGatcha = 2803;
	public static final int CSSkillInit = 2911;
	public static final int SCSkillInitResult = 2912;
	public static final int CSSkillLevelup = 2913;
	public static final int SCSkillLevelupResult = 2914;
	public static final int CSInbornLevelup = 2915;
	public static final int SCInbornLevelupResult = 2916;
	public static final int CSChangeInborn = 2917;
	public static final int SCChangeInbornResult = 2918;
	public static final int CSAddRelSkill = 2923;
	public static final int SCAddRelSkillResult = 2924;
	public static final int CSDelRelSkill = 2925;
	public static final int SCDelRelSkillResult = 2926;
	public static final int CSOpenMall = 3001;
	public static final int SCMallResult = 3002;
	public static final int CSBuyMallGoods = 3003;
	public static final int SCBuyMallGoods = 3004;
	public static final int CSRequestShopTags = 3005;
	public static final int SCResultShopTags = 3006;
	public static final int CSOpenShop = 3007;
	public static final int SCShopResult = 3008;
	public static final int CSBuyShopGoods = 3009;
	public static final int SCBuyShopGoods = 3010;
	public static final int CSRefreshShop = 3011;
	public static final int SCRefreshShopResult = 3012;
	public static final int SCOpenTempShop = 3013;
	public static final int CSBuyShop = 3014;
	public static final int SCBuyShopResult = 3015;
	public static final int CSInstanceRank = 3101;
	public static final int SCInstanceRank = 3102;
	public static final int CSLevelRank = 3103;
	public static final int SCLevelRank = 3104;
	public static final int CSCombatRank = 3105;
	public static final int SCCombatRank = 3106;
	public static final int SCAllLayer = 3201;
	public static final int CSLayerEnter = 3202;
	public static final int CSLayerEnd = 3203;
	public static final int SCLayerEnd = 3204;
	public static final int CSLayerLeave = 3205;
	public static final int CSLayerAward = 3206;
	public static final int SCLayerAward = 3207;
	public static final int CSLayerRefreshBuff = 3208;
	public static final int SCLayerRefreshBuff = 3209;
	public static final int CSRefreshTower = 3210;
	public static final int CSTeamReqOpenUI = 3301;
	public static final int SCTeamReqOpenUIResult = 3302;
	public static final int CSTeamReqCloseUI = 3315;
	public static final int SCTeamReqUpdateRealTime = 3316;
	public static final int CSTeamReqJoin = 3303;
	public static final int SCTeamReqSelfResult = 3304;
	public static final int CSTeamReqLaunch = 3305;
	public static final int CSTeamRepCreate = 3307;
	public static final int CSTeamRepCall = 3321;
	public static final int CSTeamRepLeave = 3309;
	public static final int SCTeamRepLeave = 3310;
	public static final int CSTeamRepLeaveQueue = 3317;
	public static final int SCTeamRepLeaveQueue = 3318;
	public static final int CSTeamRepKick = 3311;
	public static final int SCTeamRepKick = 3314;
	public static final int SCTeamReqLeftNum = 3312;
	public static final int CSTeamInvite = 3331;
	public static final int SCTeamInvite = 3332;
	public static final int CSTeamInviteConfirm = 3333;
	public static final int CSTeamApply = 3335;
	public static final int SCTeamApply = 3336;
	public static final int CSTeamApplyConfirm = 3337;
	public static final int CSTeamMine = 3341;
	public static final int SCTeamMine = 3338;
	public static final int CSTeamLeave = 3339;
	public static final int SCTeamLeave = 3340;
	public static final int CSCompetitionEnter = 3401;
	public static final int CSCompetitionEnd = 3402;
	public static final int SCCompetitionEnd = 3403;
	public static final int CSCompetitionLeave = 3404;
	public static final int CSRefreshEnemy = 3405;
	public static final int SCCompetitionInfo = 3407;
	public static final int CSResetCDTime = 3408;
	public static final int SCResetCDTime = 3409;
	public static final int CSOpenRank = 3410;
	public static final int SCRankResult = 3411;
	public static final int CSCompetitionBattleLog = 3412;
	public static final int SCCompetitionBattleLog = 3413;
	public static final int CSRefreshCount = 3414;
	public static final int SCRefreshCount = 3415;
	public static final int CSGetSeflRankPage = 3416;
	public static final int SCGetSeflRankPage = 3417;
	public static final int CSFriendList = 3501;
	public static final int SCFriendList = 3502;
	public static final int CSRecommendFriend = 3509;
	public static final int SCRecommendFriend = 3510;
	public static final int CSRequestFriend = 3503;
	public static final int SCRequestFriend = 3504;
	public static final int CSAcceptFriend = 3505;
	public static final int SCAcceptFriend = 3506;
	public static final int CSRefuseFriend = 3507;
	public static final int SCRefuseFriend = 3508;
	public static final int CSSearchFriend = 3511;
	public static final int SCSearchFriend = 3512;
	public static final int CSRemoveFriend = 3513;
	public static final int SCRemoveFriend = 3514;
	public static final int CSToBlackList = 3517;
	public static final int SCToBlackList = 3518;
	public static final int CSRemoveBlackList = 3519;
	public static final int SCRemoveBlackList = 3520;
	public static final int CSBulletinOpenUI = 3601;
	public static final int SCBulletinOpenUI = 3602;
	public static final int CSTest = 9901;
	public static final int CSTestAddMoney = 9902;
	public static final int CSTestAddItem = 9903;
	public static final int CSTestGiveGeneral = 9908;
	public static final int CSTestWhoIsMyDad = 9909;
	public static final int CSTestEnterRep = 9917;
	public static final int CSTestGiveAllGeneral = 9919;
	public static final int CSTestSendSysMail = 9920;
	public static final int CSTestVIP = 9921;
	public static final int CSTestInstanceStar = 9922;
	public static final int CSTestUpdateTime = 9923;
	public static final int CSInformToAll = 9924;
	
	//消息CLASS与消息ID的对应关系<消息class, 消息ID>
	private static final Map<Class<? extends Message>, Integer> classToId = new HashMap<>();
	//消息ID与消息CLASS的对应关系<消息ID, 消息class>
	private static final Map<Integer, Class<? extends Message>> idToClass = new HashMap<>();
	
	static {
		//初始化消息CLASS与消息ID的对应关系
		initClassToId();
		//初始化消息ID与消息CLASS的对应关系
		initIdToClass();
	}
	
	/**
	 * 获取消息ID
	 * @param clazz
	 * @return
	 */
	public static int getIdByClass(Class<? extends Message> clazz) {
		return classToId.get(clazz);
	}
	
	/**
	 * 获取消息CLASS
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getClassById(int msgId) {
		return (T) idToClass.get(msgId);
	}
	
	/**
	 * 获取消息名称
	 * @param clazz
	 * @return
	 */
	public static String getNameById(int msgId) {
		try {
			return idToClass.get(msgId).getSimpleName();
		} catch (Exception e) {
			throw new SysException(e, "获取消息名称是发生错误：msgId={0}", msgId);
		}
	}
	
	/**
	 * 初始化消息CLASS与消息ID的对应关系
	 */
	private static void initClassToId() {
		classToId.put(Msg.SCMsgFill.class, SCMsgFill);
		classToId.put(Msg.CSLogin.class, CSLogin);
		classToId.put(Msg.SCLoginResult.class, SCLoginResult);
		classToId.put(Msg.CSAccountReconnect.class, CSAccountReconnect);
		classToId.put(Msg.SCAccountReconnectResult.class, SCAccountReconnectResult);
		classToId.put(Msg.CSChangeModel.class, CSChangeModel);
		classToId.put(Msg.SCChangeModel.class, SCChangeModel);
		classToId.put(Msg.CSAccountRandomName.class, CSAccountRandomName);
		classToId.put(Msg.SCAccountRandomName.class, SCAccountRandomName);
		classToId.put(Msg.CSQueryCharacters.class, CSQueryCharacters);
		classToId.put(Msg.SCQueryCharactersResult.class, SCQueryCharactersResult);
		classToId.put(Msg.CSCharacterCreate.class, CSCharacterCreate);
		classToId.put(Msg.SCCharacterCreateResult.class, SCCharacterCreateResult);
		classToId.put(Msg.CSCharacterDelete.class, CSCharacterDelete);
		classToId.put(Msg.SCCharacterDeleteResult.class, SCCharacterDeleteResult);
		classToId.put(Msg.CSCharacterLogin.class, CSCharacterLogin);
		classToId.put(Msg.SCCharacterLoginResult.class, SCCharacterLoginResult);
		classToId.put(Msg.SCInitData.class, SCInitData);
		classToId.put(Msg.SCHumanKick.class, SCHumanKick);
		classToId.put(Msg.SCHumanInfoChange.class, SCHumanInfoChange);
		classToId.put(Msg.SCStageObjectInfoChange.class, SCStageObjectInfoChange);
		classToId.put(Msg.CSHumanInfo.class, CSHumanInfo);
		classToId.put(Msg.SCHumanInfo.class, SCHumanInfo);
		classToId.put(Msg.SCDieStage.class, SCDieStage);
		classToId.put(Msg.SCResult.class, SCResult);
		classToId.put(Msg.CSStageEnter.class, CSStageEnter);
		classToId.put(Msg.SCStageEnterResult.class, SCStageEnterResult);
		classToId.put(Msg.SCStageEnterEnd.class, SCStageEnterEnd);
		classToId.put(Msg.CSStageSwitch.class, CSStageSwitch);
		classToId.put(Msg.SCStageSwitch.class, SCStageSwitch);
		classToId.put(Msg.CSStageMove.class, CSStageMove);
		classToId.put(Msg.SCStageMove.class, SCStageMove);
		classToId.put(Msg.SCStageSetPos.class, SCStageSetPos);
		classToId.put(Msg.CSStageMoveStop.class, CSStageMoveStop);
		classToId.put(Msg.SCStageMoveStop.class, SCStageMoveStop);
		classToId.put(Msg.SCStageObjectAppear.class, SCStageObjectAppear);
		classToId.put(Msg.SCStageObjectDisappear.class, SCStageObjectDisappear);
		classToId.put(Msg.SCStageMoveTeleport.class, SCStageMoveTeleport);
		classToId.put(Msg.CSStageMove2.class, CSStageMove2);
		classToId.put(Msg.SCStagePullTo.class, SCStagePullTo);
		classToId.put(Msg.SCUnitobjStatusChange.class, SCUnitobjStatusChange);
		classToId.put(Msg.SCStageObjectLevelUp.class, SCStageObjectLevelUp);
		classToId.put(Msg.SCSceneInit.class, SCSceneInit);
		classToId.put(Msg.SCScenePlotChange.class, SCScenePlotChange);
		classToId.put(Msg.CSSceneTrigger.class, CSSceneTrigger);
		classToId.put(Msg.CSSceneEvent.class, CSSceneEvent);
		classToId.put(Msg.SCSceneEventStart.class, SCSceneEventStart);
		classToId.put(Msg.CSFightAtk.class, CSFightAtk);
		classToId.put(Msg.SCFightAtkResult.class, SCFightAtkResult);
		classToId.put(Msg.SCFightSkill.class, SCFightSkill);
		classToId.put(Msg.SCFightHpChg.class, SCFightHpChg);
		classToId.put(Msg.CSFightRevive.class, CSFightRevive);
		classToId.put(Msg.SCFightRevive.class, SCFightRevive);
		classToId.put(Msg.SCFightStageChange.class, SCFightStageChange);
		classToId.put(Msg.SCFightDotHpChg.class, SCFightDotHpChg);
		classToId.put(Msg.DRageAdd.class, DRageAdd);
		classToId.put(Msg.SCFightBulletHpChg.class, SCFightBulletHpChg);
		classToId.put(Msg.SCFightBulletMove.class, SCFightBulletMove);
		classToId.put(Msg.CSSkillInterrupt.class, CSSkillInterrupt);
		classToId.put(Msg.SCSkillInterrupt.class, SCSkillInterrupt);
		classToId.put(Msg.CSSkillAddGeneral.class, CSSkillAddGeneral);
		classToId.put(Msg.CSSkillRemoveGeneral.class, CSSkillRemoveGeneral);
		classToId.put(Msg.CSSkillAddGeneralToUnion.class, CSSkillAddGeneralToUnion);
		classToId.put(Msg.CSUnionFightStart.class, CSUnionFightStart);
		classToId.put(Msg.CSUnionFightAIPause.class, CSUnionFightAIPause);
		classToId.put(Msg.CSUnionFightAIUnpause.class, CSUnionFightAIUnpause);
		classToId.put(Msg.CSUnionFightSpecial.class, CSUnionFightSpecial);
		classToId.put(Msg.CSUnionFightAuto.class, CSUnionFightAuto);
		classToId.put(Msg.SCFightSkillTeamCancel.class, SCFightSkillTeamCancel);
		classToId.put(Msg.SCBagUpdate.class, SCBagUpdate);
		classToId.put(Msg.CSUseItem.class, CSUseItem);
		classToId.put(Msg.CSArrangeBag.class, CSArrangeBag);
		classToId.put(Msg.CSItemBachSell.class, CSItemBachSell);
		classToId.put(Msg.CSBagExpand.class, CSBagExpand);
		classToId.put(Msg.SCBagExpand.class, SCBagExpand);
		classToId.put(Msg.CSInformChat.class, CSInformChat);
		classToId.put(Msg.SCInformMsg.class, SCInformMsg);
		classToId.put(Msg.SCInformMsgAll.class, SCInformMsgAll);
		classToId.put(Msg.SCBuffAdd.class, SCBuffAdd);
		classToId.put(Msg.SCBuffUpdate.class, SCBuffUpdate);
		classToId.put(Msg.SCBuffDispel.class, SCBuffDispel);
		classToId.put(Msg.CSBuffDispelByHuman.class, CSBuffDispelByHuman);
		classToId.put(Msg.CSGeneralList.class, CSGeneralList);
		classToId.put(Msg.SCGeneralList.class, SCGeneralList);
		classToId.put(Msg.CSGeneralInfo.class, CSGeneralInfo);
		classToId.put(Msg.SCGeneralInfo.class, SCGeneralInfo);
		classToId.put(Msg.CSGeneralRecruit.class, CSGeneralRecruit);
		classToId.put(Msg.SCGeneralRecruitResult.class, SCGeneralRecruitResult);
		classToId.put(Msg.CSGeneralExpAdd.class, CSGeneralExpAdd);
		classToId.put(Msg.SCGeneralExpAdd.class, SCGeneralExpAdd);
		classToId.put(Msg.CSGeneralStarUp.class, CSGeneralStarUp);
		classToId.put(Msg.SCGeneralStarUp.class, SCGeneralStarUp);
		classToId.put(Msg.CSGeneralQualityUp.class, CSGeneralQualityUp);
		classToId.put(Msg.SCGeneralQualityUp.class, SCGeneralQualityUp);
		classToId.put(Msg.CSGeneralEquipUp.class, CSGeneralEquipUp);
		classToId.put(Msg.SCGeneralEquipUp.class, SCGeneralEquipUp);
		classToId.put(Msg.CSFragInfo.class, CSFragInfo);
		classToId.put(Msg.SCFragInfo.class, SCFragInfo);
		classToId.put(Msg.CSOneFragInfo.class, CSOneFragInfo);
		classToId.put(Msg.SCOneFragInfo.class, SCOneFragInfo);
		classToId.put(Msg.CSSellFrag.class, CSSellFrag);
		classToId.put(Msg.SCSellFrag.class, SCSellFrag);
		classToId.put(Msg.CSGenTaskFightTimes.class, CSGenTaskFightTimes);
		classToId.put(Msg.SCGenTaskFightTimes.class, SCGenTaskFightTimes);
		classToId.put(Msg.CSEnterGeneralTask.class, CSEnterGeneralTask);
		classToId.put(Msg.SCEnterGeneralTask.class, SCEnterGeneralTask);
		classToId.put(Msg.SCGeneralTaskFightResult.class, SCGeneralTaskFightResult);
		classToId.put(Msg.SCGeneralTaskReward.class, SCGeneralTaskReward);
		classToId.put(Msg.CSGeneralToAttIng.class, CSGeneralToAttIng);
		classToId.put(Msg.SCGeneralToAttIng.class, SCGeneralToAttIng);
		classToId.put(Msg.CSGeneralInfoAttIng.class, CSGeneralInfoAttIng);
		classToId.put(Msg.SCGeneralInfoAttIng.class, SCGeneralInfoAttIng);
		classToId.put(Msg.SCGeneralRemove.class, SCGeneralRemove);
		classToId.put(Msg.CSGeneralChooseFirst.class, CSGeneralChooseFirst);
		classToId.put(Msg.SCGeneralChooseFirstResult.class, SCGeneralChooseFirstResult);
		classToId.put(Msg.CSSignIn.class, CSSignIn);
		classToId.put(Msg.SCSignIn.class, SCSignIn);
		classToId.put(Msg.CSCommitQuestNormal.class, CSCommitQuestNormal);
		classToId.put(Msg.SCCommitQuestNormalResult.class, SCCommitQuestNormalResult);
		classToId.put(Msg.CSCommitQuestDaily.class, CSCommitQuestDaily);
		classToId.put(Msg.CSOpenQuest.class, CSOpenQuest);
		classToId.put(Msg.CSOpenQuestDaily.class, CSOpenQuestDaily);
		classToId.put(Msg.SCQuestInfo.class, SCQuestInfo);
		classToId.put(Msg.CSOpenQuestInstDaily.class, CSOpenQuestInstDaily);
		classToId.put(Msg.SCQuestInstDailyCount.class, SCQuestInstDailyCount);
		classToId.put(Msg.CSCommitQuestInstDaily.class, CSCommitQuestInstDaily);
		classToId.put(Msg.SCQuestInstDailyInfo.class, SCQuestInstDailyInfo);
		classToId.put(Msg.CSInstanceEnter.class, CSInstanceEnter);
		classToId.put(Msg.CSInstanceLeave.class, CSInstanceLeave);
		classToId.put(Msg.CSInstanceEnd.class, CSInstanceEnd);
		classToId.put(Msg.SCInstanceEnd.class, SCInstanceEnd);
		classToId.put(Msg.SCUpdateChapter.class, SCUpdateChapter);
		classToId.put(Msg.SCAllChapter.class, SCAllChapter);
		classToId.put(Msg.CSInstanceAuto.class, CSInstanceAuto);
		classToId.put(Msg.SCInstanceAuto.class, SCInstanceAuto);
		classToId.put(Msg.CSBoxAward.class, CSBoxAward);
		classToId.put(Msg.SCBoxAward.class, SCBoxAward);
		classToId.put(Msg.CSInstanceLottery.class, CSInstanceLottery);
		classToId.put(Msg.SCInstanceLottery.class, SCInstanceLottery);
		classToId.put(Msg.CSChangeName.class, CSChangeName);
		classToId.put(Msg.SCChangeNameResult.class, SCChangeNameResult);
		classToId.put(Msg.SCChangeNameQuestFinish.class, SCChangeNameQuestFinish);
		classToId.put(Msg.CSChangeNameRandom.class, CSChangeNameRandom);
		classToId.put(Msg.SCChangeNameRandomResult.class, SCChangeNameRandomResult);
		classToId.put(Msg.SCAddMail.class, SCAddMail);
		classToId.put(Msg.CSMailList.class, CSMailList);
		classToId.put(Msg.SCMailList.class, SCMailList);
		classToId.put(Msg.CSReadMail.class, CSReadMail);
		classToId.put(Msg.CSPickupMail.class, CSPickupMail);
		classToId.put(Msg.SCPickupMail.class, SCPickupMail);
		classToId.put(Msg.CSPickupAllMail.class, CSPickupAllMail);
		classToId.put(Msg.SCPickupAllMail.class, SCPickupAllMail);
		classToId.put(Msg.CSClearMail.class, CSClearMail);
		classToId.put(Msg.SCDeleteMail.class, SCDeleteMail);
		classToId.put(Msg.CSCheckMailRemoveable.class, CSCheckMailRemoveable);
		classToId.put(Msg.SCCheckMailRemoveable.class, SCCheckMailRemoveable);
		classToId.put(Msg.CSRsvLivenessAwards.class, CSRsvLivenessAwards);
		classToId.put(Msg.SCRsvLivenessAwardsResult.class, SCRsvLivenessAwardsResult);
		classToId.put(Msg.CSOpenLivenessUI.class, CSOpenLivenessUI);
		classToId.put(Msg.SCLivenessInfo.class, SCLivenessInfo);
		classToId.put(Msg.CSEquipUp.class, CSEquipUp);
		classToId.put(Msg.SCEquipUp.class, SCEquipUp);
		classToId.put(Msg.CSEquipDown.class, CSEquipDown);
		classToId.put(Msg.SCEquipDown.class, SCEquipDown);
		classToId.put(Msg.CSEquipReclaim.class, CSEquipReclaim);
		classToId.put(Msg.SCEquipReclaim.class, SCEquipReclaim);
		classToId.put(Msg.SCEquipChangeBoardcast.class, SCEquipChangeBoardcast);
		classToId.put(Msg.CSPartQianghua.class, CSPartQianghua);
		classToId.put(Msg.SCPartQianghua.class, SCPartQianghua);
		classToId.put(Msg.CSPartChongxing.class, CSPartChongxing);
		classToId.put(Msg.SCPartChongxing.class, SCPartChongxing);
		classToId.put(Msg.CSGemUp.class, CSGemUp);
		classToId.put(Msg.CSGemUpAll.class, CSGemUpAll);
		classToId.put(Msg.SCSetGem.class, SCSetGem);
		classToId.put(Msg.CSGemDown.class, CSGemDown);
		classToId.put(Msg.CSGemDownAll.class, CSGemDownAll);
		classToId.put(Msg.CSGemComposite.class, CSGemComposite);
		classToId.put(Msg.SCGemComposite.class, SCGemComposite);
		classToId.put(Msg.CSGemCompositeTop.class, CSGemCompositeTop);
		classToId.put(Msg.SCGemCompositeTop.class, SCGemCompositeTop);
		classToId.put(Msg.CSGemCompositeAllTop.class, CSGemCompositeAllTop);
		classToId.put(Msg.SCGemCompositeAllTop.class, SCGemCompositeAllTop);
		classToId.put(Msg.CSGatcha.class, CSGatcha);
		classToId.put(Msg.CSGatcha10.class, CSGatcha10);
		classToId.put(Msg.SCGatcha.class, SCGatcha);
		classToId.put(Msg.CSSkillInit.class, CSSkillInit);
		classToId.put(Msg.SCSkillInitResult.class, SCSkillInitResult);
		classToId.put(Msg.CSSkillLevelup.class, CSSkillLevelup);
		classToId.put(Msg.SCSkillLevelupResult.class, SCSkillLevelupResult);
		classToId.put(Msg.CSInbornLevelup.class, CSInbornLevelup);
		classToId.put(Msg.SCInbornLevelupResult.class, SCInbornLevelupResult);
		classToId.put(Msg.CSChangeInborn.class, CSChangeInborn);
		classToId.put(Msg.SCChangeInbornResult.class, SCChangeInbornResult);
		classToId.put(Msg.CSAddRelSkill.class, CSAddRelSkill);
		classToId.put(Msg.SCAddRelSkillResult.class, SCAddRelSkillResult);
		classToId.put(Msg.CSDelRelSkill.class, CSDelRelSkill);
		classToId.put(Msg.SCDelRelSkillResult.class, SCDelRelSkillResult);
		classToId.put(Msg.CSOpenMall.class, CSOpenMall);
		classToId.put(Msg.SCMallResult.class, SCMallResult);
		classToId.put(Msg.CSBuyMallGoods.class, CSBuyMallGoods);
		classToId.put(Msg.SCBuyMallGoods.class, SCBuyMallGoods);
		classToId.put(Msg.CSRequestShopTags.class, CSRequestShopTags);
		classToId.put(Msg.SCResultShopTags.class, SCResultShopTags);
		classToId.put(Msg.CSOpenShop.class, CSOpenShop);
		classToId.put(Msg.SCShopResult.class, SCShopResult);
		classToId.put(Msg.CSBuyShopGoods.class, CSBuyShopGoods);
		classToId.put(Msg.SCBuyShopGoods.class, SCBuyShopGoods);
		classToId.put(Msg.CSRefreshShop.class, CSRefreshShop);
		classToId.put(Msg.SCRefreshShopResult.class, SCRefreshShopResult);
		classToId.put(Msg.SCOpenTempShop.class, SCOpenTempShop);
		classToId.put(Msg.CSBuyShop.class, CSBuyShop);
		classToId.put(Msg.SCBuyShopResult.class, SCBuyShopResult);
		classToId.put(Msg.CSInstanceRank.class, CSInstanceRank);
		classToId.put(Msg.SCInstanceRank.class, SCInstanceRank);
		classToId.put(Msg.CSLevelRank.class, CSLevelRank);
		classToId.put(Msg.SCLevelRank.class, SCLevelRank);
		classToId.put(Msg.CSCombatRank.class, CSCombatRank);
		classToId.put(Msg.SCCombatRank.class, SCCombatRank);
		classToId.put(Msg.SCAllLayer.class, SCAllLayer);
		classToId.put(Msg.CSLayerEnter.class, CSLayerEnter);
		classToId.put(Msg.CSLayerEnd.class, CSLayerEnd);
		classToId.put(Msg.SCLayerEnd.class, SCLayerEnd);
		classToId.put(Msg.CSLayerLeave.class, CSLayerLeave);
		classToId.put(Msg.CSLayerAward.class, CSLayerAward);
		classToId.put(Msg.SCLayerAward.class, SCLayerAward);
		classToId.put(Msg.CSLayerRefreshBuff.class, CSLayerRefreshBuff);
		classToId.put(Msg.SCLayerRefreshBuff.class, SCLayerRefreshBuff);
		classToId.put(Msg.CSRefreshTower.class, CSRefreshTower);
		classToId.put(Msg.CSTeamReqOpenUI.class, CSTeamReqOpenUI);
		classToId.put(Msg.SCTeamReqOpenUIResult.class, SCTeamReqOpenUIResult);
		classToId.put(Msg.CSTeamReqCloseUI.class, CSTeamReqCloseUI);
		classToId.put(Msg.SCTeamReqUpdateRealTime.class, SCTeamReqUpdateRealTime);
		classToId.put(Msg.CSTeamReqJoin.class, CSTeamReqJoin);
		classToId.put(Msg.SCTeamReqSelfResult.class, SCTeamReqSelfResult);
		classToId.put(Msg.CSTeamReqLaunch.class, CSTeamReqLaunch);
		classToId.put(Msg.CSTeamRepCreate.class, CSTeamRepCreate);
		classToId.put(Msg.CSTeamRepCall.class, CSTeamRepCall);
		classToId.put(Msg.CSTeamRepLeave.class, CSTeamRepLeave);
		classToId.put(Msg.SCTeamRepLeave.class, SCTeamRepLeave);
		classToId.put(Msg.CSTeamRepLeaveQueue.class, CSTeamRepLeaveQueue);
		classToId.put(Msg.SCTeamRepLeaveQueue.class, SCTeamRepLeaveQueue);
		classToId.put(Msg.CSTeamRepKick.class, CSTeamRepKick);
		classToId.put(Msg.SCTeamRepKick.class, SCTeamRepKick);
		classToId.put(Msg.SCTeamReqLeftNum.class, SCTeamReqLeftNum);
		classToId.put(Msg.CSTeamInvite.class, CSTeamInvite);
		classToId.put(Msg.SCTeamInvite.class, SCTeamInvite);
		classToId.put(Msg.CSTeamInviteConfirm.class, CSTeamInviteConfirm);
		classToId.put(Msg.CSTeamApply.class, CSTeamApply);
		classToId.put(Msg.SCTeamApply.class, SCTeamApply);
		classToId.put(Msg.CSTeamApplyConfirm.class, CSTeamApplyConfirm);
		classToId.put(Msg.CSTeamMine.class, CSTeamMine);
		classToId.put(Msg.SCTeamMine.class, SCTeamMine);
		classToId.put(Msg.CSTeamLeave.class, CSTeamLeave);
		classToId.put(Msg.SCTeamLeave.class, SCTeamLeave);
		classToId.put(Msg.CSCompetitionEnter.class, CSCompetitionEnter);
		classToId.put(Msg.CSCompetitionEnd.class, CSCompetitionEnd);
		classToId.put(Msg.SCCompetitionEnd.class, SCCompetitionEnd);
		classToId.put(Msg.CSCompetitionLeave.class, CSCompetitionLeave);
		classToId.put(Msg.CSRefreshEnemy.class, CSRefreshEnemy);
		classToId.put(Msg.SCCompetitionInfo.class, SCCompetitionInfo);
		classToId.put(Msg.CSResetCDTime.class, CSResetCDTime);
		classToId.put(Msg.SCResetCDTime.class, SCResetCDTime);
		classToId.put(Msg.CSOpenRank.class, CSOpenRank);
		classToId.put(Msg.SCRankResult.class, SCRankResult);
		classToId.put(Msg.CSCompetitionBattleLog.class, CSCompetitionBattleLog);
		classToId.put(Msg.SCCompetitionBattleLog.class, SCCompetitionBattleLog);
		classToId.put(Msg.CSRefreshCount.class, CSRefreshCount);
		classToId.put(Msg.SCRefreshCount.class, SCRefreshCount);
		classToId.put(Msg.CSGetSeflRankPage.class, CSGetSeflRankPage);
		classToId.put(Msg.SCGetSeflRankPage.class, SCGetSeflRankPage);
		classToId.put(Msg.CSFriendList.class, CSFriendList);
		classToId.put(Msg.SCFriendList.class, SCFriendList);
		classToId.put(Msg.CSRecommendFriend.class, CSRecommendFriend);
		classToId.put(Msg.SCRecommendFriend.class, SCRecommendFriend);
		classToId.put(Msg.CSRequestFriend.class, CSRequestFriend);
		classToId.put(Msg.SCRequestFriend.class, SCRequestFriend);
		classToId.put(Msg.CSAcceptFriend.class, CSAcceptFriend);
		classToId.put(Msg.SCAcceptFriend.class, SCAcceptFriend);
		classToId.put(Msg.CSRefuseFriend.class, CSRefuseFriend);
		classToId.put(Msg.SCRefuseFriend.class, SCRefuseFriend);
		classToId.put(Msg.CSSearchFriend.class, CSSearchFriend);
		classToId.put(Msg.SCSearchFriend.class, SCSearchFriend);
		classToId.put(Msg.CSRemoveFriend.class, CSRemoveFriend);
		classToId.put(Msg.SCRemoveFriend.class, SCRemoveFriend);
		classToId.put(Msg.CSToBlackList.class, CSToBlackList);
		classToId.put(Msg.SCToBlackList.class, SCToBlackList);
		classToId.put(Msg.CSRemoveBlackList.class, CSRemoveBlackList);
		classToId.put(Msg.SCRemoveBlackList.class, SCRemoveBlackList);
		classToId.put(Msg.CSBulletinOpenUI.class, CSBulletinOpenUI);
		classToId.put(Msg.SCBulletinOpenUI.class, SCBulletinOpenUI);
		classToId.put(Msg.CSTest.class, CSTest);
		classToId.put(Msg.CSTestAddMoney.class, CSTestAddMoney);
		classToId.put(Msg.CSTestAddItem.class, CSTestAddItem);
		classToId.put(Msg.CSTestGiveGeneral.class, CSTestGiveGeneral);
		classToId.put(Msg.CSTestWhoIsMyDad.class, CSTestWhoIsMyDad);
		classToId.put(Msg.CSTestEnterRep.class, CSTestEnterRep);
		classToId.put(Msg.CSTestGiveAllGeneral.class, CSTestGiveAllGeneral);
		classToId.put(Msg.CSTestSendSysMail.class, CSTestSendSysMail);
		classToId.put(Msg.CSTestVIP.class, CSTestVIP);
		classToId.put(Msg.CSTestInstanceStar.class, CSTestInstanceStar);
		classToId.put(Msg.CSTestUpdateTime.class, CSTestUpdateTime);
		classToId.put(Msg.CSInformToAll.class, CSInformToAll);
	}
	
	/**
	 * 初始化消息ID与消息CLASS的对应关系
	 */
	private static void initIdToClass() {
		idToClass.put(SCMsgFill, Msg.SCMsgFill.class);
		idToClass.put(CSLogin, Msg.CSLogin.class);
		idToClass.put(SCLoginResult, Msg.SCLoginResult.class);
		idToClass.put(CSAccountReconnect, Msg.CSAccountReconnect.class);
		idToClass.put(SCAccountReconnectResult, Msg.SCAccountReconnectResult.class);
		idToClass.put(CSChangeModel, Msg.CSChangeModel.class);
		idToClass.put(SCChangeModel, Msg.SCChangeModel.class);
		idToClass.put(CSAccountRandomName, Msg.CSAccountRandomName.class);
		idToClass.put(SCAccountRandomName, Msg.SCAccountRandomName.class);
		idToClass.put(CSQueryCharacters, Msg.CSQueryCharacters.class);
		idToClass.put(SCQueryCharactersResult, Msg.SCQueryCharactersResult.class);
		idToClass.put(CSCharacterCreate, Msg.CSCharacterCreate.class);
		idToClass.put(SCCharacterCreateResult, Msg.SCCharacterCreateResult.class);
		idToClass.put(CSCharacterDelete, Msg.CSCharacterDelete.class);
		idToClass.put(SCCharacterDeleteResult, Msg.SCCharacterDeleteResult.class);
		idToClass.put(CSCharacterLogin, Msg.CSCharacterLogin.class);
		idToClass.put(SCCharacterLoginResult, Msg.SCCharacterLoginResult.class);
		idToClass.put(SCInitData, Msg.SCInitData.class);
		idToClass.put(SCHumanKick, Msg.SCHumanKick.class);
		idToClass.put(SCHumanInfoChange, Msg.SCHumanInfoChange.class);
		idToClass.put(SCStageObjectInfoChange, Msg.SCStageObjectInfoChange.class);
		idToClass.put(CSHumanInfo, Msg.CSHumanInfo.class);
		idToClass.put(SCHumanInfo, Msg.SCHumanInfo.class);
		idToClass.put(SCDieStage, Msg.SCDieStage.class);
		idToClass.put(SCResult, Msg.SCResult.class);
		idToClass.put(CSStageEnter, Msg.CSStageEnter.class);
		idToClass.put(SCStageEnterResult, Msg.SCStageEnterResult.class);
		idToClass.put(SCStageEnterEnd, Msg.SCStageEnterEnd.class);
		idToClass.put(CSStageSwitch, Msg.CSStageSwitch.class);
		idToClass.put(SCStageSwitch, Msg.SCStageSwitch.class);
		idToClass.put(CSStageMove, Msg.CSStageMove.class);
		idToClass.put(SCStageMove, Msg.SCStageMove.class);
		idToClass.put(SCStageSetPos, Msg.SCStageSetPos.class);
		idToClass.put(CSStageMoveStop, Msg.CSStageMoveStop.class);
		idToClass.put(SCStageMoveStop, Msg.SCStageMoveStop.class);
		idToClass.put(SCStageObjectAppear, Msg.SCStageObjectAppear.class);
		idToClass.put(SCStageObjectDisappear, Msg.SCStageObjectDisappear.class);
		idToClass.put(SCStageMoveTeleport, Msg.SCStageMoveTeleport.class);
		idToClass.put(CSStageMove2, Msg.CSStageMove2.class);
		idToClass.put(SCStagePullTo, Msg.SCStagePullTo.class);
		idToClass.put(SCUnitobjStatusChange, Msg.SCUnitobjStatusChange.class);
		idToClass.put(SCStageObjectLevelUp, Msg.SCStageObjectLevelUp.class);
		idToClass.put(SCSceneInit, Msg.SCSceneInit.class);
		idToClass.put(SCScenePlotChange, Msg.SCScenePlotChange.class);
		idToClass.put(CSSceneTrigger, Msg.CSSceneTrigger.class);
		idToClass.put(CSSceneEvent, Msg.CSSceneEvent.class);
		idToClass.put(SCSceneEventStart, Msg.SCSceneEventStart.class);
		idToClass.put(CSFightAtk, Msg.CSFightAtk.class);
		idToClass.put(SCFightAtkResult, Msg.SCFightAtkResult.class);
		idToClass.put(SCFightSkill, Msg.SCFightSkill.class);
		idToClass.put(SCFightHpChg, Msg.SCFightHpChg.class);
		idToClass.put(CSFightRevive, Msg.CSFightRevive.class);
		idToClass.put(SCFightRevive, Msg.SCFightRevive.class);
		idToClass.put(SCFightStageChange, Msg.SCFightStageChange.class);
		idToClass.put(SCFightDotHpChg, Msg.SCFightDotHpChg.class);
		idToClass.put(DRageAdd, Msg.DRageAdd.class);
		idToClass.put(SCFightBulletHpChg, Msg.SCFightBulletHpChg.class);
		idToClass.put(SCFightBulletMove, Msg.SCFightBulletMove.class);
		idToClass.put(CSSkillInterrupt, Msg.CSSkillInterrupt.class);
		idToClass.put(SCSkillInterrupt, Msg.SCSkillInterrupt.class);
		idToClass.put(CSSkillAddGeneral, Msg.CSSkillAddGeneral.class);
		idToClass.put(CSSkillRemoveGeneral, Msg.CSSkillRemoveGeneral.class);
		idToClass.put(CSSkillAddGeneralToUnion, Msg.CSSkillAddGeneralToUnion.class);
		idToClass.put(CSUnionFightStart, Msg.CSUnionFightStart.class);
		idToClass.put(CSUnionFightAIPause, Msg.CSUnionFightAIPause.class);
		idToClass.put(CSUnionFightAIUnpause, Msg.CSUnionFightAIUnpause.class);
		idToClass.put(CSUnionFightSpecial, Msg.CSUnionFightSpecial.class);
		idToClass.put(CSUnionFightAuto, Msg.CSUnionFightAuto.class);
		idToClass.put(SCFightSkillTeamCancel, Msg.SCFightSkillTeamCancel.class);
		idToClass.put(SCBagUpdate, Msg.SCBagUpdate.class);
		idToClass.put(CSUseItem, Msg.CSUseItem.class);
		idToClass.put(CSArrangeBag, Msg.CSArrangeBag.class);
		idToClass.put(CSItemBachSell, Msg.CSItemBachSell.class);
		idToClass.put(CSBagExpand, Msg.CSBagExpand.class);
		idToClass.put(SCBagExpand, Msg.SCBagExpand.class);
		idToClass.put(CSInformChat, Msg.CSInformChat.class);
		idToClass.put(SCInformMsg, Msg.SCInformMsg.class);
		idToClass.put(SCInformMsgAll, Msg.SCInformMsgAll.class);
		idToClass.put(SCBuffAdd, Msg.SCBuffAdd.class);
		idToClass.put(SCBuffUpdate, Msg.SCBuffUpdate.class);
		idToClass.put(SCBuffDispel, Msg.SCBuffDispel.class);
		idToClass.put(CSBuffDispelByHuman, Msg.CSBuffDispelByHuman.class);
		idToClass.put(CSGeneralList, Msg.CSGeneralList.class);
		idToClass.put(SCGeneralList, Msg.SCGeneralList.class);
		idToClass.put(CSGeneralInfo, Msg.CSGeneralInfo.class);
		idToClass.put(SCGeneralInfo, Msg.SCGeneralInfo.class);
		idToClass.put(CSGeneralRecruit, Msg.CSGeneralRecruit.class);
		idToClass.put(SCGeneralRecruitResult, Msg.SCGeneralRecruitResult.class);
		idToClass.put(CSGeneralExpAdd, Msg.CSGeneralExpAdd.class);
		idToClass.put(SCGeneralExpAdd, Msg.SCGeneralExpAdd.class);
		idToClass.put(CSGeneralStarUp, Msg.CSGeneralStarUp.class);
		idToClass.put(SCGeneralStarUp, Msg.SCGeneralStarUp.class);
		idToClass.put(CSGeneralQualityUp, Msg.CSGeneralQualityUp.class);
		idToClass.put(SCGeneralQualityUp, Msg.SCGeneralQualityUp.class);
		idToClass.put(CSGeneralEquipUp, Msg.CSGeneralEquipUp.class);
		idToClass.put(SCGeneralEquipUp, Msg.SCGeneralEquipUp.class);
		idToClass.put(CSFragInfo, Msg.CSFragInfo.class);
		idToClass.put(SCFragInfo, Msg.SCFragInfo.class);
		idToClass.put(CSOneFragInfo, Msg.CSOneFragInfo.class);
		idToClass.put(SCOneFragInfo, Msg.SCOneFragInfo.class);
		idToClass.put(CSSellFrag, Msg.CSSellFrag.class);
		idToClass.put(SCSellFrag, Msg.SCSellFrag.class);
		idToClass.put(CSGenTaskFightTimes, Msg.CSGenTaskFightTimes.class);
		idToClass.put(SCGenTaskFightTimes, Msg.SCGenTaskFightTimes.class);
		idToClass.put(CSEnterGeneralTask, Msg.CSEnterGeneralTask.class);
		idToClass.put(SCEnterGeneralTask, Msg.SCEnterGeneralTask.class);
		idToClass.put(SCGeneralTaskFightResult, Msg.SCGeneralTaskFightResult.class);
		idToClass.put(SCGeneralTaskReward, Msg.SCGeneralTaskReward.class);
		idToClass.put(CSGeneralToAttIng, Msg.CSGeneralToAttIng.class);
		idToClass.put(SCGeneralToAttIng, Msg.SCGeneralToAttIng.class);
		idToClass.put(CSGeneralInfoAttIng, Msg.CSGeneralInfoAttIng.class);
		idToClass.put(SCGeneralInfoAttIng, Msg.SCGeneralInfoAttIng.class);
		idToClass.put(SCGeneralRemove, Msg.SCGeneralRemove.class);
		idToClass.put(CSGeneralChooseFirst, Msg.CSGeneralChooseFirst.class);
		idToClass.put(SCGeneralChooseFirstResult, Msg.SCGeneralChooseFirstResult.class);
		idToClass.put(CSSignIn, Msg.CSSignIn.class);
		idToClass.put(SCSignIn, Msg.SCSignIn.class);
		idToClass.put(CSCommitQuestNormal, Msg.CSCommitQuestNormal.class);
		idToClass.put(SCCommitQuestNormalResult, Msg.SCCommitQuestNormalResult.class);
		idToClass.put(CSCommitQuestDaily, Msg.CSCommitQuestDaily.class);
		idToClass.put(CSOpenQuest, Msg.CSOpenQuest.class);
		idToClass.put(CSOpenQuestDaily, Msg.CSOpenQuestDaily.class);
		idToClass.put(SCQuestInfo, Msg.SCQuestInfo.class);
		idToClass.put(CSOpenQuestInstDaily, Msg.CSOpenQuestInstDaily.class);
		idToClass.put(SCQuestInstDailyCount, Msg.SCQuestInstDailyCount.class);
		idToClass.put(CSCommitQuestInstDaily, Msg.CSCommitQuestInstDaily.class);
		idToClass.put(SCQuestInstDailyInfo, Msg.SCQuestInstDailyInfo.class);
		idToClass.put(CSInstanceEnter, Msg.CSInstanceEnter.class);
		idToClass.put(CSInstanceLeave, Msg.CSInstanceLeave.class);
		idToClass.put(CSInstanceEnd, Msg.CSInstanceEnd.class);
		idToClass.put(SCInstanceEnd, Msg.SCInstanceEnd.class);
		idToClass.put(SCUpdateChapter, Msg.SCUpdateChapter.class);
		idToClass.put(SCAllChapter, Msg.SCAllChapter.class);
		idToClass.put(CSInstanceAuto, Msg.CSInstanceAuto.class);
		idToClass.put(SCInstanceAuto, Msg.SCInstanceAuto.class);
		idToClass.put(CSBoxAward, Msg.CSBoxAward.class);
		idToClass.put(SCBoxAward, Msg.SCBoxAward.class);
		idToClass.put(CSInstanceLottery, Msg.CSInstanceLottery.class);
		idToClass.put(SCInstanceLottery, Msg.SCInstanceLottery.class);
		idToClass.put(CSChangeName, Msg.CSChangeName.class);
		idToClass.put(SCChangeNameResult, Msg.SCChangeNameResult.class);
		idToClass.put(SCChangeNameQuestFinish, Msg.SCChangeNameQuestFinish.class);
		idToClass.put(CSChangeNameRandom, Msg.CSChangeNameRandom.class);
		idToClass.put(SCChangeNameRandomResult, Msg.SCChangeNameRandomResult.class);
		idToClass.put(SCAddMail, Msg.SCAddMail.class);
		idToClass.put(CSMailList, Msg.CSMailList.class);
		idToClass.put(SCMailList, Msg.SCMailList.class);
		idToClass.put(CSReadMail, Msg.CSReadMail.class);
		idToClass.put(CSPickupMail, Msg.CSPickupMail.class);
		idToClass.put(SCPickupMail, Msg.SCPickupMail.class);
		idToClass.put(CSPickupAllMail, Msg.CSPickupAllMail.class);
		idToClass.put(SCPickupAllMail, Msg.SCPickupAllMail.class);
		idToClass.put(CSClearMail, Msg.CSClearMail.class);
		idToClass.put(SCDeleteMail, Msg.SCDeleteMail.class);
		idToClass.put(CSCheckMailRemoveable, Msg.CSCheckMailRemoveable.class);
		idToClass.put(SCCheckMailRemoveable, Msg.SCCheckMailRemoveable.class);
		idToClass.put(CSRsvLivenessAwards, Msg.CSRsvLivenessAwards.class);
		idToClass.put(SCRsvLivenessAwardsResult, Msg.SCRsvLivenessAwardsResult.class);
		idToClass.put(CSOpenLivenessUI, Msg.CSOpenLivenessUI.class);
		idToClass.put(SCLivenessInfo, Msg.SCLivenessInfo.class);
		idToClass.put(CSEquipUp, Msg.CSEquipUp.class);
		idToClass.put(SCEquipUp, Msg.SCEquipUp.class);
		idToClass.put(CSEquipDown, Msg.CSEquipDown.class);
		idToClass.put(SCEquipDown, Msg.SCEquipDown.class);
		idToClass.put(CSEquipReclaim, Msg.CSEquipReclaim.class);
		idToClass.put(SCEquipReclaim, Msg.SCEquipReclaim.class);
		idToClass.put(SCEquipChangeBoardcast, Msg.SCEquipChangeBoardcast.class);
		idToClass.put(CSPartQianghua, Msg.CSPartQianghua.class);
		idToClass.put(SCPartQianghua, Msg.SCPartQianghua.class);
		idToClass.put(CSPartChongxing, Msg.CSPartChongxing.class);
		idToClass.put(SCPartChongxing, Msg.SCPartChongxing.class);
		idToClass.put(CSGemUp, Msg.CSGemUp.class);
		idToClass.put(CSGemUpAll, Msg.CSGemUpAll.class);
		idToClass.put(SCSetGem, Msg.SCSetGem.class);
		idToClass.put(CSGemDown, Msg.CSGemDown.class);
		idToClass.put(CSGemDownAll, Msg.CSGemDownAll.class);
		idToClass.put(CSGemComposite, Msg.CSGemComposite.class);
		idToClass.put(SCGemComposite, Msg.SCGemComposite.class);
		idToClass.put(CSGemCompositeTop, Msg.CSGemCompositeTop.class);
		idToClass.put(SCGemCompositeTop, Msg.SCGemCompositeTop.class);
		idToClass.put(CSGemCompositeAllTop, Msg.CSGemCompositeAllTop.class);
		idToClass.put(SCGemCompositeAllTop, Msg.SCGemCompositeAllTop.class);
		idToClass.put(CSGatcha, Msg.CSGatcha.class);
		idToClass.put(CSGatcha10, Msg.CSGatcha10.class);
		idToClass.put(SCGatcha, Msg.SCGatcha.class);
		idToClass.put(CSSkillInit, Msg.CSSkillInit.class);
		idToClass.put(SCSkillInitResult, Msg.SCSkillInitResult.class);
		idToClass.put(CSSkillLevelup, Msg.CSSkillLevelup.class);
		idToClass.put(SCSkillLevelupResult, Msg.SCSkillLevelupResult.class);
		idToClass.put(CSInbornLevelup, Msg.CSInbornLevelup.class);
		idToClass.put(SCInbornLevelupResult, Msg.SCInbornLevelupResult.class);
		idToClass.put(CSChangeInborn, Msg.CSChangeInborn.class);
		idToClass.put(SCChangeInbornResult, Msg.SCChangeInbornResult.class);
		idToClass.put(CSAddRelSkill, Msg.CSAddRelSkill.class);
		idToClass.put(SCAddRelSkillResult, Msg.SCAddRelSkillResult.class);
		idToClass.put(CSDelRelSkill, Msg.CSDelRelSkill.class);
		idToClass.put(SCDelRelSkillResult, Msg.SCDelRelSkillResult.class);
		idToClass.put(CSOpenMall, Msg.CSOpenMall.class);
		idToClass.put(SCMallResult, Msg.SCMallResult.class);
		idToClass.put(CSBuyMallGoods, Msg.CSBuyMallGoods.class);
		idToClass.put(SCBuyMallGoods, Msg.SCBuyMallGoods.class);
		idToClass.put(CSRequestShopTags, Msg.CSRequestShopTags.class);
		idToClass.put(SCResultShopTags, Msg.SCResultShopTags.class);
		idToClass.put(CSOpenShop, Msg.CSOpenShop.class);
		idToClass.put(SCShopResult, Msg.SCShopResult.class);
		idToClass.put(CSBuyShopGoods, Msg.CSBuyShopGoods.class);
		idToClass.put(SCBuyShopGoods, Msg.SCBuyShopGoods.class);
		idToClass.put(CSRefreshShop, Msg.CSRefreshShop.class);
		idToClass.put(SCRefreshShopResult, Msg.SCRefreshShopResult.class);
		idToClass.put(SCOpenTempShop, Msg.SCOpenTempShop.class);
		idToClass.put(CSBuyShop, Msg.CSBuyShop.class);
		idToClass.put(SCBuyShopResult, Msg.SCBuyShopResult.class);
		idToClass.put(CSInstanceRank, Msg.CSInstanceRank.class);
		idToClass.put(SCInstanceRank, Msg.SCInstanceRank.class);
		idToClass.put(CSLevelRank, Msg.CSLevelRank.class);
		idToClass.put(SCLevelRank, Msg.SCLevelRank.class);
		idToClass.put(CSCombatRank, Msg.CSCombatRank.class);
		idToClass.put(SCCombatRank, Msg.SCCombatRank.class);
		idToClass.put(SCAllLayer, Msg.SCAllLayer.class);
		idToClass.put(CSLayerEnter, Msg.CSLayerEnter.class);
		idToClass.put(CSLayerEnd, Msg.CSLayerEnd.class);
		idToClass.put(SCLayerEnd, Msg.SCLayerEnd.class);
		idToClass.put(CSLayerLeave, Msg.CSLayerLeave.class);
		idToClass.put(CSLayerAward, Msg.CSLayerAward.class);
		idToClass.put(SCLayerAward, Msg.SCLayerAward.class);
		idToClass.put(CSLayerRefreshBuff, Msg.CSLayerRefreshBuff.class);
		idToClass.put(SCLayerRefreshBuff, Msg.SCLayerRefreshBuff.class);
		idToClass.put(CSRefreshTower, Msg.CSRefreshTower.class);
		idToClass.put(CSTeamReqOpenUI, Msg.CSTeamReqOpenUI.class);
		idToClass.put(SCTeamReqOpenUIResult, Msg.SCTeamReqOpenUIResult.class);
		idToClass.put(CSTeamReqCloseUI, Msg.CSTeamReqCloseUI.class);
		idToClass.put(SCTeamReqUpdateRealTime, Msg.SCTeamReqUpdateRealTime.class);
		idToClass.put(CSTeamReqJoin, Msg.CSTeamReqJoin.class);
		idToClass.put(SCTeamReqSelfResult, Msg.SCTeamReqSelfResult.class);
		idToClass.put(CSTeamReqLaunch, Msg.CSTeamReqLaunch.class);
		idToClass.put(CSTeamRepCreate, Msg.CSTeamRepCreate.class);
		idToClass.put(CSTeamRepCall, Msg.CSTeamRepCall.class);
		idToClass.put(CSTeamRepLeave, Msg.CSTeamRepLeave.class);
		idToClass.put(SCTeamRepLeave, Msg.SCTeamRepLeave.class);
		idToClass.put(CSTeamRepLeaveQueue, Msg.CSTeamRepLeaveQueue.class);
		idToClass.put(SCTeamRepLeaveQueue, Msg.SCTeamRepLeaveQueue.class);
		idToClass.put(CSTeamRepKick, Msg.CSTeamRepKick.class);
		idToClass.put(SCTeamRepKick, Msg.SCTeamRepKick.class);
		idToClass.put(SCTeamReqLeftNum, Msg.SCTeamReqLeftNum.class);
		idToClass.put(CSTeamInvite, Msg.CSTeamInvite.class);
		idToClass.put(SCTeamInvite, Msg.SCTeamInvite.class);
		idToClass.put(CSTeamInviteConfirm, Msg.CSTeamInviteConfirm.class);
		idToClass.put(CSTeamApply, Msg.CSTeamApply.class);
		idToClass.put(SCTeamApply, Msg.SCTeamApply.class);
		idToClass.put(CSTeamApplyConfirm, Msg.CSTeamApplyConfirm.class);
		idToClass.put(CSTeamMine, Msg.CSTeamMine.class);
		idToClass.put(SCTeamMine, Msg.SCTeamMine.class);
		idToClass.put(CSTeamLeave, Msg.CSTeamLeave.class);
		idToClass.put(SCTeamLeave, Msg.SCTeamLeave.class);
		idToClass.put(CSCompetitionEnter, Msg.CSCompetitionEnter.class);
		idToClass.put(CSCompetitionEnd, Msg.CSCompetitionEnd.class);
		idToClass.put(SCCompetitionEnd, Msg.SCCompetitionEnd.class);
		idToClass.put(CSCompetitionLeave, Msg.CSCompetitionLeave.class);
		idToClass.put(CSRefreshEnemy, Msg.CSRefreshEnemy.class);
		idToClass.put(SCCompetitionInfo, Msg.SCCompetitionInfo.class);
		idToClass.put(CSResetCDTime, Msg.CSResetCDTime.class);
		idToClass.put(SCResetCDTime, Msg.SCResetCDTime.class);
		idToClass.put(CSOpenRank, Msg.CSOpenRank.class);
		idToClass.put(SCRankResult, Msg.SCRankResult.class);
		idToClass.put(CSCompetitionBattleLog, Msg.CSCompetitionBattleLog.class);
		idToClass.put(SCCompetitionBattleLog, Msg.SCCompetitionBattleLog.class);
		idToClass.put(CSRefreshCount, Msg.CSRefreshCount.class);
		idToClass.put(SCRefreshCount, Msg.SCRefreshCount.class);
		idToClass.put(CSGetSeflRankPage, Msg.CSGetSeflRankPage.class);
		idToClass.put(SCGetSeflRankPage, Msg.SCGetSeflRankPage.class);
		idToClass.put(CSFriendList, Msg.CSFriendList.class);
		idToClass.put(SCFriendList, Msg.SCFriendList.class);
		idToClass.put(CSRecommendFriend, Msg.CSRecommendFriend.class);
		idToClass.put(SCRecommendFriend, Msg.SCRecommendFriend.class);
		idToClass.put(CSRequestFriend, Msg.CSRequestFriend.class);
		idToClass.put(SCRequestFriend, Msg.SCRequestFriend.class);
		idToClass.put(CSAcceptFriend, Msg.CSAcceptFriend.class);
		idToClass.put(SCAcceptFriend, Msg.SCAcceptFriend.class);
		idToClass.put(CSRefuseFriend, Msg.CSRefuseFriend.class);
		idToClass.put(SCRefuseFriend, Msg.SCRefuseFriend.class);
		idToClass.put(CSSearchFriend, Msg.CSSearchFriend.class);
		idToClass.put(SCSearchFriend, Msg.SCSearchFriend.class);
		idToClass.put(CSRemoveFriend, Msg.CSRemoveFriend.class);
		idToClass.put(SCRemoveFriend, Msg.SCRemoveFriend.class);
		idToClass.put(CSToBlackList, Msg.CSToBlackList.class);
		idToClass.put(SCToBlackList, Msg.SCToBlackList.class);
		idToClass.put(CSRemoveBlackList, Msg.CSRemoveBlackList.class);
		idToClass.put(SCRemoveBlackList, Msg.SCRemoveBlackList.class);
		idToClass.put(CSBulletinOpenUI, Msg.CSBulletinOpenUI.class);
		idToClass.put(SCBulletinOpenUI, Msg.SCBulletinOpenUI.class);
		idToClass.put(CSTest, Msg.CSTest.class);
		idToClass.put(CSTestAddMoney, Msg.CSTestAddMoney.class);
		idToClass.put(CSTestAddItem, Msg.CSTestAddItem.class);
		idToClass.put(CSTestGiveGeneral, Msg.CSTestGiveGeneral.class);
		idToClass.put(CSTestWhoIsMyDad, Msg.CSTestWhoIsMyDad.class);
		idToClass.put(CSTestEnterRep, Msg.CSTestEnterRep.class);
		idToClass.put(CSTestGiveAllGeneral, Msg.CSTestGiveAllGeneral.class);
		idToClass.put(CSTestSendSysMail, Msg.CSTestSendSysMail.class);
		idToClass.put(CSTestVIP, Msg.CSTestVIP.class);
		idToClass.put(CSTestInstanceStar, Msg.CSTestInstanceStar.class);
		idToClass.put(CSTestUpdateTime, Msg.CSTestUpdateTime.class);
		idToClass.put(CSInformToAll, Msg.CSInformToAll.class);
	}
	/**
	 * 根据消息id解析消息
	 */
	public static GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException{
		switch(type){
			case CSLogin:
				return Msg.CSLogin.parseFrom(s);
			case CSAccountReconnect:
				return Msg.CSAccountReconnect.parseFrom(s);
			case CSChangeModel:
				return Msg.CSChangeModel.parseFrom(s);
			case CSAccountRandomName:
				return Msg.CSAccountRandomName.parseFrom(s);
			case CSQueryCharacters:
				return Msg.CSQueryCharacters.parseFrom(s);
			case CSCharacterCreate:
				return Msg.CSCharacterCreate.parseFrom(s);
			case CSCharacterDelete:
				return Msg.CSCharacterDelete.parseFrom(s);
			case CSCharacterLogin:
				return Msg.CSCharacterLogin.parseFrom(s);
			case CSHumanInfo:
				return Msg.CSHumanInfo.parseFrom(s);
			case CSStageEnter:
				return Msg.CSStageEnter.parseFrom(s);
			case CSStageSwitch:
				return Msg.CSStageSwitch.parseFrom(s);
			case CSStageMove:
				return Msg.CSStageMove.parseFrom(s);
			case CSStageMoveStop:
				return Msg.CSStageMoveStop.parseFrom(s);
			case CSStageMove2:
				return Msg.CSStageMove2.parseFrom(s);
			case CSSceneTrigger:
				return Msg.CSSceneTrigger.parseFrom(s);
			case CSSceneEvent:
				return Msg.CSSceneEvent.parseFrom(s);
			case CSFightAtk:
				return Msg.CSFightAtk.parseFrom(s);
			case CSFightRevive:
				return Msg.CSFightRevive.parseFrom(s);
			case CSSkillInterrupt:
				return Msg.CSSkillInterrupt.parseFrom(s);
			case CSSkillAddGeneral:
				return Msg.CSSkillAddGeneral.parseFrom(s);
			case CSSkillRemoveGeneral:
				return Msg.CSSkillRemoveGeneral.parseFrom(s);
			case CSSkillAddGeneralToUnion:
				return Msg.CSSkillAddGeneralToUnion.parseFrom(s);
			case CSUnionFightStart:
				return Msg.CSUnionFightStart.parseFrom(s);
			case CSUnionFightAIPause:
				return Msg.CSUnionFightAIPause.parseFrom(s);
			case CSUnionFightAIUnpause:
				return Msg.CSUnionFightAIUnpause.parseFrom(s);
			case CSUnionFightSpecial:
				return Msg.CSUnionFightSpecial.parseFrom(s);
			case CSUnionFightAuto:
				return Msg.CSUnionFightAuto.parseFrom(s);
			case CSUseItem:
				return Msg.CSUseItem.parseFrom(s);
			case CSArrangeBag:
				return Msg.CSArrangeBag.parseFrom(s);
			case CSItemBachSell:
				return Msg.CSItemBachSell.parseFrom(s);
			case CSBagExpand:
				return Msg.CSBagExpand.parseFrom(s);
			case CSInformChat:
				return Msg.CSInformChat.parseFrom(s);
			case CSBuffDispelByHuman:
				return Msg.CSBuffDispelByHuman.parseFrom(s);
			case CSGeneralList:
				return Msg.CSGeneralList.parseFrom(s);
			case CSGeneralInfo:
				return Msg.CSGeneralInfo.parseFrom(s);
			case CSGeneralRecruit:
				return Msg.CSGeneralRecruit.parseFrom(s);
			case CSGeneralExpAdd:
				return Msg.CSGeneralExpAdd.parseFrom(s);
			case CSGeneralStarUp:
				return Msg.CSGeneralStarUp.parseFrom(s);
			case CSGeneralQualityUp:
				return Msg.CSGeneralQualityUp.parseFrom(s);
			case CSGeneralEquipUp:
				return Msg.CSGeneralEquipUp.parseFrom(s);
			case CSFragInfo:
				return Msg.CSFragInfo.parseFrom(s);
			case CSOneFragInfo:
				return Msg.CSOneFragInfo.parseFrom(s);
			case CSSellFrag:
				return Msg.CSSellFrag.parseFrom(s);
			case CSGenTaskFightTimes:
				return Msg.CSGenTaskFightTimes.parseFrom(s);
			case CSEnterGeneralTask:
				return Msg.CSEnterGeneralTask.parseFrom(s);
			case CSGeneralToAttIng:
				return Msg.CSGeneralToAttIng.parseFrom(s);
			case CSGeneralInfoAttIng:
				return Msg.CSGeneralInfoAttIng.parseFrom(s);
			case CSGeneralChooseFirst:
				return Msg.CSGeneralChooseFirst.parseFrom(s);
			case CSSignIn:
				return Msg.CSSignIn.parseFrom(s);
			case CSCommitQuestNormal:
				return Msg.CSCommitQuestNormal.parseFrom(s);
			case CSCommitQuestDaily:
				return Msg.CSCommitQuestDaily.parseFrom(s);
			case CSOpenQuest:
				return Msg.CSOpenQuest.parseFrom(s);
			case CSOpenQuestDaily:
				return Msg.CSOpenQuestDaily.parseFrom(s);
			case CSOpenQuestInstDaily:
				return Msg.CSOpenQuestInstDaily.parseFrom(s);
			case CSCommitQuestInstDaily:
				return Msg.CSCommitQuestInstDaily.parseFrom(s);
			case CSInstanceEnter:
				return Msg.CSInstanceEnter.parseFrom(s);
			case CSInstanceLeave:
				return Msg.CSInstanceLeave.parseFrom(s);
			case CSInstanceEnd:
				return Msg.CSInstanceEnd.parseFrom(s);
			case CSInstanceAuto:
				return Msg.CSInstanceAuto.parseFrom(s);
			case CSBoxAward:
				return Msg.CSBoxAward.parseFrom(s);
			case CSInstanceLottery:
				return Msg.CSInstanceLottery.parseFrom(s);
			case CSChangeName:
				return Msg.CSChangeName.parseFrom(s);
			case CSChangeNameRandom:
				return Msg.CSChangeNameRandom.parseFrom(s);
			case CSMailList:
				return Msg.CSMailList.parseFrom(s);
			case CSReadMail:
				return Msg.CSReadMail.parseFrom(s);
			case CSPickupMail:
				return Msg.CSPickupMail.parseFrom(s);
			case CSPickupAllMail:
				return Msg.CSPickupAllMail.parseFrom(s);
			case CSClearMail:
				return Msg.CSClearMail.parseFrom(s);
			case CSCheckMailRemoveable:
				return Msg.CSCheckMailRemoveable.parseFrom(s);
			case CSRsvLivenessAwards:
				return Msg.CSRsvLivenessAwards.parseFrom(s);
			case CSOpenLivenessUI:
				return Msg.CSOpenLivenessUI.parseFrom(s);
			case CSEquipUp:
				return Msg.CSEquipUp.parseFrom(s);
			case CSEquipDown:
				return Msg.CSEquipDown.parseFrom(s);
			case CSEquipReclaim:
				return Msg.CSEquipReclaim.parseFrom(s);
			case CSPartQianghua:
				return Msg.CSPartQianghua.parseFrom(s);
			case CSPartChongxing:
				return Msg.CSPartChongxing.parseFrom(s);
			case CSGemUp:
				return Msg.CSGemUp.parseFrom(s);
			case CSGemUpAll:
				return Msg.CSGemUpAll.parseFrom(s);
			case CSGemDown:
				return Msg.CSGemDown.parseFrom(s);
			case CSGemDownAll:
				return Msg.CSGemDownAll.parseFrom(s);
			case CSGemComposite:
				return Msg.CSGemComposite.parseFrom(s);
			case CSGemCompositeTop:
				return Msg.CSGemCompositeTop.parseFrom(s);
			case CSGemCompositeAllTop:
				return Msg.CSGemCompositeAllTop.parseFrom(s);
			case CSGatcha:
				return Msg.CSGatcha.parseFrom(s);
			case CSGatcha10:
				return Msg.CSGatcha10.parseFrom(s);
			case CSSkillInit:
				return Msg.CSSkillInit.parseFrom(s);
			case CSSkillLevelup:
				return Msg.CSSkillLevelup.parseFrom(s);
			case CSInbornLevelup:
				return Msg.CSInbornLevelup.parseFrom(s);
			case CSChangeInborn:
				return Msg.CSChangeInborn.parseFrom(s);
			case CSAddRelSkill:
				return Msg.CSAddRelSkill.parseFrom(s);
			case CSDelRelSkill:
				return Msg.CSDelRelSkill.parseFrom(s);
			case CSOpenMall:
				return Msg.CSOpenMall.parseFrom(s);
			case CSBuyMallGoods:
				return Msg.CSBuyMallGoods.parseFrom(s);
			case CSRequestShopTags:
				return Msg.CSRequestShopTags.parseFrom(s);
			case CSOpenShop:
				return Msg.CSOpenShop.parseFrom(s);
			case CSBuyShopGoods:
				return Msg.CSBuyShopGoods.parseFrom(s);
			case CSRefreshShop:
				return Msg.CSRefreshShop.parseFrom(s);
			case CSBuyShop:
				return Msg.CSBuyShop.parseFrom(s);
			case CSInstanceRank:
				return Msg.CSInstanceRank.parseFrom(s);
			case CSLevelRank:
				return Msg.CSLevelRank.parseFrom(s);
			case CSCombatRank:
				return Msg.CSCombatRank.parseFrom(s);
			case CSLayerEnter:
				return Msg.CSLayerEnter.parseFrom(s);
			case CSLayerEnd:
				return Msg.CSLayerEnd.parseFrom(s);
			case CSLayerLeave:
				return Msg.CSLayerLeave.parseFrom(s);
			case CSLayerAward:
				return Msg.CSLayerAward.parseFrom(s);
			case CSLayerRefreshBuff:
				return Msg.CSLayerRefreshBuff.parseFrom(s);
			case CSRefreshTower:
				return Msg.CSRefreshTower.parseFrom(s);
			case CSTeamReqOpenUI:
				return Msg.CSTeamReqOpenUI.parseFrom(s);
			case CSTeamReqCloseUI:
				return Msg.CSTeamReqCloseUI.parseFrom(s);
			case CSTeamReqJoin:
				return Msg.CSTeamReqJoin.parseFrom(s);
			case CSTeamReqLaunch:
				return Msg.CSTeamReqLaunch.parseFrom(s);
			case CSTeamRepCreate:
				return Msg.CSTeamRepCreate.parseFrom(s);
			case CSTeamRepCall:
				return Msg.CSTeamRepCall.parseFrom(s);
			case CSTeamRepLeave:
				return Msg.CSTeamRepLeave.parseFrom(s);
			case CSTeamRepLeaveQueue:
				return Msg.CSTeamRepLeaveQueue.parseFrom(s);
			case CSTeamRepKick:
				return Msg.CSTeamRepKick.parseFrom(s);
			case CSTeamInvite:
				return Msg.CSTeamInvite.parseFrom(s);
			case CSTeamInviteConfirm:
				return Msg.CSTeamInviteConfirm.parseFrom(s);
			case CSTeamApply:
				return Msg.CSTeamApply.parseFrom(s);
			case CSTeamApplyConfirm:
				return Msg.CSTeamApplyConfirm.parseFrom(s);
			case CSTeamMine:
				return Msg.CSTeamMine.parseFrom(s);
			case CSTeamLeave:
				return Msg.CSTeamLeave.parseFrom(s);
			case CSCompetitionEnter:
				return Msg.CSCompetitionEnter.parseFrom(s);
			case CSCompetitionEnd:
				return Msg.CSCompetitionEnd.parseFrom(s);
			case CSCompetitionLeave:
				return Msg.CSCompetitionLeave.parseFrom(s);
			case CSRefreshEnemy:
				return Msg.CSRefreshEnemy.parseFrom(s);
			case CSResetCDTime:
				return Msg.CSResetCDTime.parseFrom(s);
			case CSOpenRank:
				return Msg.CSOpenRank.parseFrom(s);
			case CSCompetitionBattleLog:
				return Msg.CSCompetitionBattleLog.parseFrom(s);
			case CSRefreshCount:
				return Msg.CSRefreshCount.parseFrom(s);
			case CSGetSeflRankPage:
				return Msg.CSGetSeflRankPage.parseFrom(s);
			case CSFriendList:
				return Msg.CSFriendList.parseFrom(s);
			case CSRecommendFriend:
				return Msg.CSRecommendFriend.parseFrom(s);
			case CSRequestFriend:
				return Msg.CSRequestFriend.parseFrom(s);
			case CSAcceptFriend:
				return Msg.CSAcceptFriend.parseFrom(s);
			case CSRefuseFriend:
				return Msg.CSRefuseFriend.parseFrom(s);
			case CSSearchFriend:
				return Msg.CSSearchFriend.parseFrom(s);
			case CSRemoveFriend:
				return Msg.CSRemoveFriend.parseFrom(s);
			case CSToBlackList:
				return Msg.CSToBlackList.parseFrom(s);
			case CSRemoveBlackList:
				return Msg.CSRemoveBlackList.parseFrom(s);
			case CSBulletinOpenUI:
				return Msg.CSBulletinOpenUI.parseFrom(s);
			case CSTest:
				return Msg.CSTest.parseFrom(s);
			case CSTestAddMoney:
				return Msg.CSTestAddMoney.parseFrom(s);
			case CSTestAddItem:
				return Msg.CSTestAddItem.parseFrom(s);
			case CSTestGiveGeneral:
				return Msg.CSTestGiveGeneral.parseFrom(s);
			case CSTestWhoIsMyDad:
				return Msg.CSTestWhoIsMyDad.parseFrom(s);
			case CSTestEnterRep:
				return Msg.CSTestEnterRep.parseFrom(s);
			case CSTestGiveAllGeneral:
				return Msg.CSTestGiveAllGeneral.parseFrom(s);
			case CSTestSendSysMail:
				return Msg.CSTestSendSysMail.parseFrom(s);
			case CSTestVIP:
				return Msg.CSTestVIP.parseFrom(s);
			case CSTestInstanceStar:
				return Msg.CSTestInstanceStar.parseFrom(s);
			case CSTestUpdateTime:
				return Msg.CSTestUpdateTime.parseFrom(s);
			case CSInformToAll:
				return Msg.CSInformToAll.parseFrom(s);
		}
		return null;
	}
}

