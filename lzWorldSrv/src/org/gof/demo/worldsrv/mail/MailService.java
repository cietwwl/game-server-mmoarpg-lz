package org.gof.demo.worldsrv.mail;

import java.util.List;

import org.gof.core.Port;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.scheduler.ScheduleMethod;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.common.GamePort;
import org.gof.demo.worldsrv.common.GameServiceBase;
import org.gof.demo.worldsrv.entity.Mail;
import org.gof.demo.worldsrv.support.D;

import com.google.protobuf.Message;

@DistrClass(
	servId = D.SERV_MAIL,
	importClass = {List.class, Message.class} 
)
public class MailService extends GameServiceBase {
	
	public MailService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() {
		
	}

	/**
	 * 发送邮件
	 */
	@DistrMethod
	public void sendMail() {
		
	}
	
	/**
	 * 定时删除邮件 每2分钟一次
	 */
	@ScheduleMethod("0 0/2 * * * ?")
	public void deleteTimeoutMail() {
		DBServiceProxy proxy = DBServiceProxy.newInstance();
		String sql = Utils.createStr("delete from {} where `{}` < ?", Mail.tableName, Mail.K.etime);
		long time = Port.getTime();
		proxy.execute(false, false, sql, time);
	}
}