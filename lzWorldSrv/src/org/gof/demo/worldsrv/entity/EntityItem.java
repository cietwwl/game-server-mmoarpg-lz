package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Entity;
import org.gof.demo.worldsrv.item.AbstractItem;

@Entity(entityName="Item", tableName="demo_item", superEntity=EntityItemBase.class, superClass=AbstractItem.class)
public enum EntityItem {

}