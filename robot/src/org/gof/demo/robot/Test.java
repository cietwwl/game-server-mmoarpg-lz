package org.gof.demo.robot;

import org.gof.core.support.Param;
import org.gof.demo.worldsrv.entity.Human;

import com.udpwork.ssdb.Response;
import com.udpwork.ssdb.SSDB;

public class Test {
	private int[][] arr1 = {{-1,-1},{0,-1},{1,-1},{1,0},{1,1},{0,1},{-1,1},{-1,0}};
	private int[][] arr2 = {{-2,-2},{-1,-2},{0,-2},{1,-2},{2,-2},{2,-1},{2,0},{2,1},
			{2,2},{1,2},{0,2},{-1,2},{-2,2},{-2,1},{-2,0},{-2,-1}};
	public static double EPSILON = 1.0e-6;
	SSDB ssdb = null;
	byte[] b;
	Response resp;
	
	public void test() {
		
//		getMoveGridIndex(arr1);
//		double a = getRotateAngle(-1.73, 1, -1, 1.73);
		double a = getRotateAngle(-1, 1.73, -1.73, 1);
		System.out.println(a);
	}
	public String getName() {
		return null;
	}
	public void test(Object... params) {
		String t = getName();
		System.out.println(t);
	}
	
	public void testSSDB() {
		try {           
			ssdb = new SSDB("192.168.30.4", 8889); 
			System.out.println("---- hashmap -----");

			ssdb.hset("n", "a", "123");
			b = ssdb.hget("n", "a");
			System.out.println(new String(b));
			ssdb.hdel("n", "a");
			b = ssdb.hget("n", "a");
			System.out.println(b);
			ssdb.hset("n", "a", "100");
//			ssdb.hincr("n", "a", 100);

			resp = ssdb.hscan("n", "", "", 10);
			resp.print();
			System.out.println("");
			
			/* multi */
			ssdb.multi_set("a", "1", "b", "2");
			resp = ssdb.multi_get("n", "a");
			resp.print();
			resp = ssdb.multi_get("a", "b");
			resp.print();
			System.out.println("");
			
			resp = ssdb.scan("", "", 10);
			resp.print();
		
		} catch (Exception e) {
			if(ssdb != null){
				ssdb.close();
			}           
		}
	}
	
	public void addCharacter(String id) {
		//判断是否在
//		ssdb.hget(Human.tableName, id);
	}
	
	
	public static double getRotateAngle(double x1, double y1, double x2, double y2) {
		double dist, dot, degree, angle;
		if (x1 == x2 && y1 == y2) {
			return 0;
		}

		// normalize，标准化
		dist = Math.sqrt(x1 * x1 + y1 * y1);
		x1 /= dist;
		y1 /= dist;

		dist = Math.sqrt(x2 * x2 + y2 * y2);
		x2 /= dist;
		y2 /= dist;

		// dot product 向量点积
		dot = x1 * x2 + y1 * y2;
		if (Math.abs(dot - 1.0) <= EPSILON) {
			angle = 0.0;
		} else if (Math.abs(dot + 10) <= EPSILON) {
			angle = Math.PI;
		} else {
			double cross;
			angle = Math.acos(dot);

			// cross product 向量x乘
			cross = x1 * y2 - x2 * y1;

			// vector p2 is clockwise from vector p1
			// with respect to the origin (0.0)
			if (cross > 0) {
				angle = 2 * Math.PI - angle;
			}
		}

		degree = angle * 180.0 / Math.PI;
		return degree;
	}
	private int getMoveGridIndex(int[][] arr) {
		int result = 0;
		
		int indexLeft = result % arr.length;   //左边 --
		int indexRight = (result + 1) % arr.length; //右边++
		//从索引开始向2边宽展搜索
		for(int i = 0 ; i < arr.length ; i += 2) {
			//判断区域内是否有怪物
			System.out.println(indexLeft);
			System.out.println(indexRight);
			
			indexLeft = (indexLeft - 1 + arr.length) % arr.length;
			indexRight = (indexRight + 1) % arr.length;
		}
		return result;
	}
}
