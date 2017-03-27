package test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@FunctionalInterface
interface Func1{
	double apply(float a, float b, float c);
}
@FunctionalInterface
interface Func2{
	double apply(int a, int b, int c);
}
@FunctionalInterface
interface Func3{
	double apply(double a, double b, double c);
}
public class Main {

//	public static Object getMethod(Test test){
//		return (GofFunction3<Float, Float, Float>)test::mul;
//	}
	
	public static void main(String args[]) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		int count = 100;
		Test t = new Test();
		Method meth = null;
		for(Method m :t.getClass().getMethods()){
			if(m.getName().equals("mul")){
				meth = m;
				break;
			}
		}
		float a = (float)100.8;
		float b = (float)10000.232;
		float c = (float)10000.232;
		mul(a,b,c,count,meth,t);
	}
	
	public static void mul(float a,float b,float c,int count,Method meth,Test t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		double result = 0.0;
		Func3 f2 = t::mul2;
//		f.apply(a+1,b,c);
		long time = System.currentTimeMillis();
//		Func1 f = t::mul;
		for(int i=0;i<count;i++){
			Func1 f = t::mul;
			Func2 f1 = t::mul1;
//			Func3 f2 = t::mul2;
			result += f.apply(a+i,b,c);
			result += f1.apply((int)a+i,(int)b,(int)c);
//			result += f2.apply(a+i,b,c);
		}
		System.out.println("function-->"+(System.currentTimeMillis() - time));
		System.out.println(result);
		time = System.currentTimeMillis();
		result = 0;
		for(int i=0;i<count;i++){
			result += t.mul(a, b, c+i);
		}
		System.out.println("interface-->"+(System.currentTimeMillis() - time));
		System.out.println(result);
		time = System.currentTimeMillis();
		result = 0;
		for(int i=0;i<count;i++){
			result  += (double)meth.invoke(t, a,b+i,c);			
		}
		System.out.println("invoke-->"+(System.currentTimeMillis() - time));
		System.out.println(result);
	}
}
