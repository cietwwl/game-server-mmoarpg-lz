package org.gof.demo.worldsrv.team;
                    
import java.util.List;  
import org.gof.core.Port;
import org.gof.core.CallPoint;
import org.gof.core.Service;
import org.gof.core.support.Distr;
import org.gof.core.support.Param;
import org.gof.core.support.log.LogCore;
import org.gof.core.gen.proxy.ProxyBase;
import org.gof.core.support.function.*;
//import org.gof.demo.EnumCall;
import org.gof.core.gen.GofGenFile;
import org.gof.core.entity.EntityBase;
import java.util.List;
import org.gof.demo.worldsrv.human.HumanGlobalInfo;
import java.util.Map;
import org.gof.demo.worldsrv.character.HumanObject;

@GofGenFile
public final class TeamGlobalServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_APPLYCONFIRM_LONG_LONG = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CLOSEREQTEAMUI_LONG_INT = 2;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CLOSEREQTEAMUI_LONG = 3;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CREATEREQTEAM_HUMANOBJECT_INT = 4;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CREATETEAM_HUMANOBJECT = 5;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CREATETEAM_LONG_LONG = 6;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETHUMANREQTEAM_HUMANOBJECT_INT = 7;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAM_LONG = 8;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAM_HUMANOBJECT = 9;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMBYHUMANIDDISTR_LONG = 10;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMMEMBERDISTR_LONG = 11;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMMEMBERSDISTR_LIST = 12;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMMEMBERSWITHOUTTEAMDISTR = 13;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_INVITECONFIRM_LONG_LONG = 14;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_JOINREQTEAM_HUMANOBJECT_INT_LONG = 15;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_JOINTEAM_LONG_LONG = 16;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_KICKMEMBER_LONG_HUMANOBJECT_LONG = 17;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LAUNCHREQTEAM_HUMANOBJECT_INT = 18;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVE_LONG = 19;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVEQUEUE_HUMANOBJECT_INT = 20;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVEREQTEAM_LONG = 21;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVEREQTEAMSYS_LONG_TEAMMEMBEROBJECT = 22;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVETEAMSYS_LONG_LONG = 23;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVETEAMSYS_LONG = 24;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ONHUMANATTRCHANGE_HUMANOBJECT = 25;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ONKILLED_HUMANOBJECT = 26;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ON_LOGIN_HUMANOBJECT = 27;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ON_LOGOUT_HUMANOBJECT = 28;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_OPEMREQTEAMUI_HUMANOBJECT_INT = 29;
		public static final int ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_UPDATETEAM_TEAM = 30;
	}
	private static final String SERV_ID = "team";
	
	private CallPoint remote;
	private Port localPort2;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private TeamGlobalServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		TeamGlobalService serv = (TeamGlobalService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_APPLYCONFIRM_LONG_LONG: {
				return (GofFunction2<Long, Long>)serv::applyConfirm;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CLOSEREQTEAMUI_LONG_INT: {
				return (GofFunction2<Long, Integer>)serv::closeReqTeamUI;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CLOSEREQTEAMUI_LONG: {
				return (GofFunction1<Long>)serv::closeReqTeamUI;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CREATEREQTEAM_HUMANOBJECT_INT: {
				return (GofFunction2<HumanObject, Integer>)serv::createReqTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CREATETEAM_HUMANOBJECT: {
				return (GofFunction1<HumanObject>)serv::createTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CREATETEAM_LONG_LONG: {
				return (GofFunction2<Long, Long>)serv::createTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETHUMANREQTEAM_HUMANOBJECT_INT: {
				return (GofFunction2<HumanObject, Integer>)serv::getHumanReqTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAM_LONG: {
				return (GofFunction1<Long>)serv::getTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAM_HUMANOBJECT: {
				return (GofFunction1<HumanObject>)serv::getTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMBYHUMANIDDISTR_LONG: {
				return (GofFunction1<Long>)serv::getTeamByHumanIdDistr;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMMEMBERDISTR_LONG: {
				return (GofFunction1<Long>)serv::getTeamMemberDistr;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMMEMBERSDISTR_LIST: {
				return (GofFunction1<List>)serv::getTeamMembersDistr;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMMEMBERSWITHOUTTEAMDISTR: {
				return (GofFunction0)serv::getTeamMembersWithOutTeamDistr;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_INVITECONFIRM_LONG_LONG: {
				return (GofFunction2<Long, Long>)serv::inviteConfirm;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_JOINREQTEAM_HUMANOBJECT_INT_LONG: {
				return (GofFunction3<HumanObject, Integer, Long>)serv::joinReqTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_JOINTEAM_LONG_LONG: {
				return (GofFunction2<Long, Long>)serv::joinTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_KICKMEMBER_LONG_HUMANOBJECT_LONG: {
				return (GofFunction3<Long, HumanObject, Long>)serv::kickMember;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LAUNCHREQTEAM_HUMANOBJECT_INT: {
				return (GofFunction2<HumanObject, Integer>)serv::launchReqTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVE_LONG: {
				return (GofFunction1<Long>)serv::leave;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVEQUEUE_HUMANOBJECT_INT: {
				return (GofFunction2<HumanObject, Integer>)serv::leaveQueue;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVEREQTEAM_LONG: {
				return (GofFunction1<Long>)serv::leaveReqTeam;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVEREQTEAMSYS_LONG_TEAMMEMBEROBJECT: {
				return (GofFunction2<Long, TeamMemberObject>)serv::leaveReqTeamSys;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVETEAMSYS_LONG_LONG: {
				return (GofFunction2<Long, Long>)serv::leaveTeamSys;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVETEAMSYS_LONG: {
				return (GofFunction1<Long>)serv::leaveTeamSys;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ONHUMANATTRCHANGE_HUMANOBJECT: {
				return (GofFunction1<HumanObject>)serv::onHumanAttrChange;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ONKILLED_HUMANOBJECT: {
				return (GofFunction1<HumanObject>)serv::onKilled;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ON_LOGIN_HUMANOBJECT: {
				return (GofFunction1<HumanObject>)serv::on_login;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ON_LOGOUT_HUMANOBJECT: {
				return (GofFunction1<HumanObject>)serv::on_logout;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_OPEMREQTEAMUI_HUMANOBJECT_INT: {
				return (GofFunction2<HumanObject, Integer>)serv::opemReqTeamUI;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_UPDATETEAM_TEAM: {
				return (GofFunction1<Team>)serv::updateTeam;
			}
			default: break;
		}
		return null;
	}
	
	/**
	 * 获取实例
	 * 大多数情况下可用此函数获取
	 * @param localPort
	 * @return
	 */
	public static TeamGlobalServiceProxy newInstance() {
		String portId = Distr.getPortId(SERV_ID);
		if(portId == null) {
			LogCore.remote.error("通过servId未能找到查找上级Port: servId={}", SERV_ID);
			return null;
		}
		
		String nodeId = Distr.getNodeId(portId);
		if(nodeId == null) {
			LogCore.remote.error("通过portId未能找到查找上级Node: portId={}", portId);
			return null;
		}
		
		return createInstance(nodeId, portId, SERV_ID);
	}
	
	
	/**
	 * 创建实例
	 * @param localPort
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static TeamGlobalServiceProxy createInstance(String node, String port, Object id) {
		TeamGlobalServiceProxy inst = new TeamGlobalServiceProxy();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Param context) {
		localPort.listenResult(method, context);
	}
	
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Param context) {
		localPort.listenResult(method, context);
	}
	
	
	/**
	 * 等待返回值
	 */
	public Param waitForResult() {
		return localPort.waitForResult();
	}
	
	public void applyConfirm(long mId, long leaderId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_APPLYCONFIRM_LONG_LONG, new Object[]{ mId, leaderId });
	}
	
	public void closeReqTeamUI(long humanId, int reqId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CLOSEREQTEAMUI_LONG_INT, new Object[]{ humanId, reqId });
	}
	
	public void closeReqTeamUI(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CLOSEREQTEAMUI_LONG, new Object[]{ humanId });
	}
	
	public void createReqTeam(HumanObject leader, int reqId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CREATEREQTEAM_HUMANOBJECT_INT, new Object[]{ leader, reqId });
	}
	
	public void createTeam(HumanObject leader) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CREATETEAM_HUMANOBJECT, new Object[]{ leader });
	}
	
	public void createTeam(long leader, long recipient) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_CREATETEAM_LONG_LONG, new Object[]{ leader, recipient });
	}
	
	public void getHumanReqTeam(HumanObject humanObj, int reqId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETHUMANREQTEAM_HUMANOBJECT_INT, new Object[]{ humanObj, reqId });
	}
	
	public void getTeam(long teamId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAM_LONG, new Object[]{ teamId });
	}
	
	public void getTeam(HumanObject humanObj) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAM_HUMANOBJECT, new Object[]{ humanObj });
	}
	
	public void getTeamByHumanIdDistr(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMBYHUMANIDDISTR_LONG, new Object[]{ humanId });
	}
	
	public void getTeamMemberDistr(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMMEMBERDISTR_LONG, new Object[]{ humanId });
	}
	
	public void getTeamMembersDistr(List humanIds) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMMEMBERSDISTR_LIST, new Object[]{ humanIds });
	}
	
	public void getTeamMembersWithOutTeamDistr() {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_GETTEAMMEMBERSWITHOUTTEAMDISTR, new Object[]{  });
	}
	
	public void inviteConfirm(long mId, long leaderId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_INVITECONFIRM_LONG_LONG, new Object[]{ mId, leaderId });
	}
	
	public void joinReqTeam(HumanObject recipient, int reqId, long teamId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_JOINREQTEAM_HUMANOBJECT_INT_LONG, new Object[]{ recipient, reqId, teamId });
	}
	
	public void joinTeam(long recipient, long teamId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_JOINTEAM_LONG_LONG, new Object[]{ recipient, teamId });
	}
	
	public void kickMember(long teamId, HumanObject human, long memberId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_KICKMEMBER_LONG_HUMANOBJECT_LONG, new Object[]{ teamId, human, memberId });
	}
	
	public void launchReqTeam(HumanObject leader, int req) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LAUNCHREQTEAM_HUMANOBJECT_INT, new Object[]{ leader, req });
	}
	
	public void leave(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVE_LONG, new Object[]{ humanId });
	}
	
	public void leaveQueue(HumanObject human, int reqId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVEQUEUE_HUMANOBJECT_INT, new Object[]{ human, reqId });
	}
	
	public void leaveReqTeam(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVEREQTEAM_LONG, new Object[]{ humanId });
	}
	
	public void leaveReqTeamSys(long teamId, TeamMemberObject mmb) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVEREQTEAMSYS_LONG_TEAMMEMBEROBJECT, new Object[]{ teamId, mmb });
	}
	
	public void leaveTeamSys(long teamId, long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVETEAMSYS_LONG_LONG, new Object[]{ teamId, humanId });
	}
	
	public void leaveTeamSys(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_LEAVETEAMSYS_LONG, new Object[]{ humanId });
	}
	
	public void onHumanAttrChange(HumanObject humanObj) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ONHUMANATTRCHANGE_HUMANOBJECT, new Object[]{ humanObj });
	}
	
	public void onKilled(HumanObject humanObj) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ONKILLED_HUMANOBJECT, new Object[]{ humanObj });
	}
	
	public void on_login(HumanObject human) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ON_LOGIN_HUMANOBJECT, new Object[]{ human });
	}
	
	public void on_logout(HumanObject human) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_ON_LOGOUT_HUMANOBJECT, new Object[]{ human });
	}
	
	public void opemReqTeamUI(HumanObject humanObj, int reqId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_OPEMREQTEAMUI_HUMANOBJECT_INT, new Object[]{ humanObj, reqId });
	}
	
	public void updateTeam(Team team) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_TEAM_TEAMGLOBALSERVICE_UPDATETEAM_TEAM, new Object[]{ team });
	}
}
