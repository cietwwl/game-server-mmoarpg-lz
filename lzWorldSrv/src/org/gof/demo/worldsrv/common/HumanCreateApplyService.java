
package org.gof.demo.worldsrv.common;

import java.util.HashSet;
import java.util.Set;

import org.gof.core.Record;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.scheduler.ScheduleTask;
import org.gof.core.support.Param;
import org.gof.core.support.Time;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.name.NameServiceProxy;
import org.gof.demo.worldsrv.support.C;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.I18n;

@DistrClass(
	servId = D.SERV_HUMAN_CREATE_APPLY
)
public class HumanCreateApplyService extends GameServiceBase {
	//创建中的账号
	private final Set<String> accounts = new HashSet<>();
	//创建中的角色名
	private final Set<String> names = new HashSet<>();
	
	public HumanCreateApplyService(GamePort port) {
		super(port);
	}

	@Override
	protected void init() {
		
	}
	
	/**
	 * 申请创建新角色
	 * @param serverId
	 * @param account
	 * @param name
	 */
	@DistrMethod
	public void apply(int serverId, String account, String name) {
		//验证账号是否在创建中
		String accountkey = serverId + account;
		if(accounts.contains(accountkey)) {
			port.returns("result", false, "reason", I18n.get("account.create.sameAccount"));
			return;
		}
		
		//验证角色名是否在创建中
		if(names.contains(name)) {
			port.returns("result", false, "reason", I18n.get("account.create.sameName"));
			return;
		}
		
		//记录角色正在创建中
		accounts.add(accountkey);
		names.add(name);
		
		//创建一个异步返回
		long pid = port.createReturnAsync();
		
		//先查询验证是否能创建角色
		String sql = Utils.createStr("where (`{}`=? and `{}`=? and`{}`=?)", Human.K.account, Human.K.serverId, Human.K.name);
		DBServiceProxy db = DBServiceProxy.newInstance();
		db.getByQuery(false, Human.tableName, sql, account, C.GAME_SERVER_ID, "a6");
		db.listenResult(this::_result_apply,  "pid", pid, "serverId", serverId, "account", account, "name", name);
	}
	
	/**
	 * 角色创建请求 查询角色信息 是否可以被创建
	 * @param results
	 * @param context
	 */
	public void _result_apply(Param results, Param context) {
		long pid = context.get("pid");
		final int serverId = context.get("serverId");
		final String account = context.get("account");
		final String name = context.get("name");
		
		//查询结果
		Record record = results.get();
		
		//如果没有命中证明这个角色符合创建条件
		
		boolean succeed = (record == null);
		
		String reason = I18n.get("account.create.sameName");
		//如果失败了 就将刚刚添加的创建中状态取消
		if(!succeed) {
			accounts.remove(serverId + account);
			names.remove(name);
		
			//验证是否唯一的serverId +　account
			Human human = new Human(record);
			String accountkey = serverId + account;
			String humanAccountKey = human.getServerId() + human.getAccount();
			if(accountkey.equals(humanAccountKey)) {
				reason = I18n.get("account.create.sameAccount");
			}
		//如果可创建 那么保留5秒 时间应该够后续操作的了 
		} else {
			// 添加名字
			NameServiceProxy prx = NameServiceProxy.newInstance();
			prx.add(name);
			
			scheduleOnce(new ScheduleTask() {
				@Override
				public void execute() {
					accounts.remove(serverId + account);
					names.remove(name);
				}
			}, 5 * Time.SEC);
		}
		
		//这里只考虑角色重名的情况，对于账号已注册过这种低概率事件就不做独立提示了
		port.returnsAsync(pid, "result", succeed, "reason", succeed ? "" : reason);
	}
}
