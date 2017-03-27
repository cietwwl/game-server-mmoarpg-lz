package org.gof.demo.worldsrv.team;

import java.io.IOException;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.demo.worldsrv.support.ReasonResult;


public class TeamResult extends ReasonResult{
	protected boolean ignore = false;	//忽略
	
	private Team team;
	
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		out.write(ignore);
		out.write(team);
	}
	
	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		ignore = in.read();
		team = in.read();
	}
	
	public void setTeam(Team team){
		this.team = team;
	}
	
	public Team getTeam(){
		return this.team;
	}
	
	public int getResult(){
		return this.success?1:0;
	}
	
	public String getInfo() {
		return reason;
	}
	
	public void setInfo(String info) {
		reason = info;
	}
	
	public boolean isSuccess(){
		return this.success;
	}
	
	public TeamResult success(){
		this.success = true;
		return this;
	}
	
	public TeamResult failure(){
		this.success = false;
		return this;
	}
	
	public TeamResult ignore(){
		this.ignore = false;
		return this;
	}
	
	public boolean isIgnore() {
		return ignore;
	}
	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}
}

