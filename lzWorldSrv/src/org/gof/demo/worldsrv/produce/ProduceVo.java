package org.gof.demo.worldsrv.produce;

import java.util.List;

import org.gof.demo.worldsrv.item.ItemVO;
import org.gof.demo.worldsrv.msg.Msg.DProduce;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;

public class ProduceVo {
	public int sn;		
	public int num;			//数量
	public boolean isItem = true;		//是否是物品， 可能是货币
	
	public ProduceVo(int sn, int num) {
		this.sn = sn;
		this.num = num;
		if(ProduceMoneyKey.getKeyByType(sn) != null) {
			isItem = false;
		}
	}
	
	public ItemVO toItemVo() {
		if(isItem) {
			return new ItemVO(sn, num);
		} else {
			return null;
		}
	}
	
	public DProduce.Builder toDProduce() {
		DProduce.Builder dPro = DProduce.newBuilder();
		dPro.setSn(sn);
		dPro.setNum(num);
//		dPro.setIsItem(isItem);
		
		return dPro;
	}
	
	
	public static List<ItemVO> addItemVoList(List<ItemVO> voList, ProduceVo item) {
		if(item.isItem) {
			voList.add(item.toItemVo());
		}
		return voList;
	}
	
	
	
	
}
