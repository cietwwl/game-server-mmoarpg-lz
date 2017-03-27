package org.gof.demo.battlesrv.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.DVector2;

/**
 * 坐标
 */
public class Vector2D implements ISerilizable {
	public double x;			//横坐标
	public double y;			//纵坐标
	
	public Vector2D() { }
	
	/**
	 * 构造函数
	 * @param x
	 * @param y
	 */
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * 将消息位置信息转化本坐标
	 * @param vector2
	 */
	public Vector2D(DVector2 vector2) {
		this(vector2.getX(), vector2.getY());
	}
	
	/**
	 * 将消息位置信息转化本坐标
	 * @param vs
	 * @return
	 */
	public static List<Vector2D> parseFrom(List<DVector2> vs) {
		List<Vector2D> result = new ArrayList<>();
		for(DVector2 v : vs) {
			result.add(new Vector2D(v));
		}
		
		return result;
	}
	
	/**
	 * 将消息位置信息转化本坐标
	 * @param vs
	 * @return
	 */
	public static List<DVector2> toMsgs(List<Vector2D> vs) {
		List<DVector2> result = new ArrayList<>();
		for(Vector2D v : vs) {
			result.add(v.toMsg());
		}
		
		return result;
	}
	
	/**
	 * 转化为消息类型
	 * @return
	 */
	public DVector2 toMsg() {
		Msg.DVector2.Builder msg = Msg.DVector2.newBuilder();
		msg.setX((float)x);	
		msg.setY((float)y);
		
		return msg.build();
	}
	
	/**
	 * 转换为三维float型数组
	 */
	public float[] toFloat3() {
		return new float[]{(float)x, 0, (float)y};
	}
	
	/**
	 * 导航网格的
	 * @return
	 */
	public float[] toDetourFloat3() {
		return new float[]{(float)y, 0, (float)x};
	}
	/**
	 * 设置坐标值
	 * @param vector
	 */
	public void set(Vector2D vector) {
		this.x = vector.x;
		this.y = vector.y;
	}
	
	public Vector2D sub(Vector2D vector) {
		Vector2D result = new Vector2D();
		result.x = this.x - vector.x;
		result.y = this.y - vector.y;
		return result;
	}
	
	public Vector2D sum(Vector2D vector) {
		Vector2D result = new Vector2D();
		result.x = this.x + vector.x;
		result.y = this.y + vector.y;
		return result;
	}
	
	public Vector2D mul(Vector2D vector) {
		Vector2D result = new Vector2D();
		result.x = this.x * vector.x;
		result.y = this.y * vector.y;
		return result;
	}
	
	public Vector2D mul(double a) {
		Vector2D result = new Vector2D();
		result.x = this.x * a;
		result.y = this.y * a;
		return result;
	}
	
	public Vector2D div(double a) {
		Vector2D result = new Vector2D();
		result.x = this.x / a;
		result.y = this.y / a;
		return result;
	}
	
	public Vector2D normalize() {
		Vector2D result = new Vector2D();
		double dis = Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y , 2));
		result.x = this.x / dis;
		result.y = this.y / dis;
		return result;
	}
	
	public double Dot(Vector2D vector) {
		return this.x * vector.x + this.y * vector.y;
	}
	
	/**
	 * 获取易读的坐标字符串
	 * @return
	 */
	public String getPosStr() {
		return new StringBuilder("(").append(x).append(",").append(y).append(")").toString();
	}
	
	/**
	 * 两点之间的距离
	 * @param pos
	 * @return
	 */
	public double distance(Vector2D pos) {
		if(pos == null) return 0;
		
		double t1x = this.x;
		double t1y = this.y;
		
		double t2x = pos.x;
		double t2y = pos.y;
		
		return Math.sqrt(Math.pow((t1x -t2x), 2) + Math.pow((t1y - t2y) , 2));
	}
	
	/**
	 * 从start 指向 end的方向 从org点移动 DIS的距离
	 * @param start
	 * @param end
	 * @param dis
	 * @return
	 */
	public static Vector2D lookAtDis(Vector2D start, Vector2D end, Vector2D org, double dis) {
		Vector2D result = new Vector2D();
		
		if(end.equals(start)) {
			return start;
		}
		double diffX = end.x - start.x;
		double diffY = end.y - start.y;
		
		//实际距离
		double diffTrue = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
		
		//起始至目标的Sin,Cos值
		double tempSin = diffY / diffTrue;
		double tempCos = diffX / diffTrue;
		
		double dX = tempCos * dis;
		double dY = tempSin * dis;
		
		result.x = org.x + dX;
		result.y = org.y + dY;
		
		return result;
	}
	
	/**
	 * 从start 指向 end的方向 从org点移动 DIS的距离 包含攻击，移动距离要减去相对距离
	 * @param start
	 * @param end
	 * @param dis
	 * @return
	 */
	public static Vector2D lookAtAttDis(Vector2D start, Vector2D end, Vector2D org, double attDis) {
		Vector2D result = new Vector2D();
		
		if(end.equals(start)) {
			return start;
		}
		double diffX = end.x - start.x;
		double diffY = end.y - start.y;
		
		//实际距离
		double diffTrue = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
		
		//移动距离不能超过攻击移动距离
		double fixAttDis = attDis - diffTrue;
		fixAttDis = fixAttDis < 0 ? 0D : fixAttDis;
		
		//起始至目标的Sin,Cos值
		double tempSin = diffY / diffTrue;
		double tempCos = diffX / diffTrue;
		
		double dX = tempCos * fixAttDis;
		double dY = tempSin * fixAttDis;
		
		result.x = org.x + dX;
		result.y = org.y + dY;
		
		return result;
	}
	/**
	 * 根据武将站位获取站位点
	 * @param posNow  主角坐标
	 * @param dirNow  主角朝向
	 * @param type    武将站位
	 * @param attDis   站位距离
	 * @return
	 */
	public static Vector2D genFindDis(Vector2D posNow, Vector2D dirNow, int type , double attDis) {
		Vector2D result = new Vector2D();
		Vector2D ht = new Vector2D();  //角色正前方的武将站位点
		ht=lookAtDis(posNow, dirNow, posNow,  attDis);
		List<Vector2D> Alldis=findHumanDis(posNow, ht ,attDis);
		result=Alldis.get(type);
		return result;
	}
	/**
	 * 主角位置周围的八个点坐标
	 * @param human
	 * @param attDis
	 * @return
	 */
	public static List<Vector2D> findHumanDis(Vector2D posNow, Vector2D dirNow ,double attDis) {
//		dirNow=lookAtDis(posNow, dirNow, posNow,  attDis);
		List<Vector2D> result = new ArrayList<>();
		result.add(finddis(45,posNow,dirNow));
		result.add(dirNow);
		result.add(finddis(315,posNow,dirNow));
		result.add(finddis(90,posNow,dirNow));
		result.add(finddis(270,posNow,dirNow));
		result.add(finddis(135,posNow,dirNow));
		result.add(finddis(180,posNow,dirNow));
		result.add(finddis(225,posNow,dirNow));
		return result;
	}
	/**
	 * 
	 * @param a  //角度
	 * @param posNow
	 * @param dirNow
	 * @return
	 */
	public static Vector2D finddis(double a,Vector2D posNow, Vector2D dirNow){
		Vector2D result=new Vector2D();
		result.x=(dirNow.x-posNow.x)*Math.cos(a*Math.PI/180)-(dirNow.y-posNow.y)*Math.sin(a*Math.PI/180)+posNow.x;
		result.y=(dirNow.x-posNow.x)*Math.sin(a*Math.PI/180)+(dirNow.y-posNow.y)*Math.cos(a*Math.PI/180)+posNow.y;
		return result;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(x);
		out.write(y);
	}
	
	@Override
	public void readFrom(InputStream in) throws IOException {
		x = in.read();
		y = in.read();
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if(!(other instanceof Vector2D)) {
			return false;
		}
		
		Vector2D castOther = (Vector2D) other;
		return new EqualsBuilder().append(this.x, castOther.x).append(this.y, castOther.y).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(x).append(y).toHashCode();
	}

	@Override
	public String toString() {
		return new StringBuilder().append("[").append(x).append(",").append(y).append("]").toString();
	}
}
