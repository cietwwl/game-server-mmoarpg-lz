package org.gof.demo.worldsrv.competition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.entity.CompetitionHuman;
import org.gof.demo.worldsrv.entity.CompetitionMirror;

public class CompetitionHumanObj implements ISerilizable {
	public boolean isFight = false;
	public CompetitionHuman human;                                      // 人物基础数据
	public CompetitionMirror humanMirror;                               // 人物镜像数据
	public List<CompetitionMirror> gensMirror = new ArrayList<>();      // 伙伴镜像数据
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(isFight);
		out.write(human);
		out.write(humanMirror);
		out.write(gensMirror);
	}
	
	@Override
	public void readFrom(InputStream in) throws IOException {
		isFight = in.read();
		human = in.read();
		humanMirror = in.read();
		gensMirror = in.read();
	}
}
