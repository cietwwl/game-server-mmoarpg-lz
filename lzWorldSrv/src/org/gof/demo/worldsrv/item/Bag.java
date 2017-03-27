package org.gof.demo.worldsrv.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;

public class Bag<E> implements ISerilizable {

	private ArrayList<E> list = new ArrayList<E>();

	
	public ArrayList<E> getList() {
		return list;
	}

	public void add(E element) {
		list.add(element);
	}
	
	public void add(int index, E element) {
		int size = list.size();
		if (index > size) {
			list.addAll(Collections.nCopies(index - size, null));
		}
		list.add(index, element);
	}

	public boolean addAll(int index, Collection<? extends E> coll) {
		int size = list.size();
		boolean result = false;
		if (index > size) {
			list.addAll(Collections.nCopies(index - size, null));
			result = true;
		}
		return (list.addAll(index, coll) | result);
	}

	public E set(int index, E element) {
		int size = list.size();
		if (index >= size) {
			list.addAll(Collections.nCopies(index - size + 1, null));
		}
		return list.set(index, element);
	}

	public E get(int index) {
		int size = list.size();
		if (index >= size) {
			return null;
		}
		return list.get(index);
	}

	public int size() {
		return list.size();
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(list);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		list = in.read();
	}

}
