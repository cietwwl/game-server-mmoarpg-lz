package org.gof.demo.battlesrv.skill;

public class SkillParamVO {
	public double hurtAddPct;		//伤害加成百分比
	public double hurtAdd;			//伤害加成纯数值
	public double ignoreDefPct;	//忽视防御百分比
	public double hurtAvoidPct;	//减伤百分比
	public double bloodSuckPct;	//吸血百分比
	
	public double skillShakePct = 1;	//前摇的百分比
	
	public SkillParamVO() {
		super();
	}

	public SkillParamVO(int hurtAddPct, int hurtAdd, int ignoreDefPct, int hurtAvoidPct, int bloodSuckPct) {
		super();
		this.hurtAddPct = hurtAddPct;
		this.hurtAdd = hurtAdd;
		this.ignoreDefPct = ignoreDefPct;
		this.hurtAvoidPct = hurtAvoidPct;
		this.bloodSuckPct = bloodSuckPct;
	}
}
