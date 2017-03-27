package org.gof.demo.worldsrv.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;

/**
 * A decorated version of {@code ArrayList}, so that executing
 * {@code add(int, E}, {@code addAll(int Collection<? super E},
 * {@code set(int, E)} and {@code get(int)} won't throw
 * {@code IndexOutOfBoundsException}, unless the given index is negative.
 * 
 * @author zhangbo
 *
 * @param <E>
 */
public class GetSetGrowthList<E> extends ArrayList<E> implements ISerilizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4729542404439067406L;

	/**
	 * @throws IndexOutOfBoundsException
	 *             - if the index is out of left range (index < 0)
	 */
	@Override
	public void add(int index, E element) {
		int size = super.size();
		if (index > size) {
			super.addAll(Collections.nCopies(index - size, null));
		}
		super.add(index, element);
	}

	/**
	 * @throws IndexOutOfBoundsException
	 *             - if the index is out of left range (index < 0)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> coll) {
		int size = super.size();
		boolean result = false;
		if (index > size) {
			super.addAll(Collections.nCopies(index - size, null));
			result = true;
		}
		return (super.addAll(index, coll) | result);
	}

	/**
	 * @throws IndexOutOfBoundsException
	 *             - if the index is out of left range (index < 0)
	 */
	@Override
	public E set(int index, E element) {
		int size = super.size();
		if (index >= size) {
			super.addAll(Collections.nCopies(index - size + 1, null));
		}
		return super.set(index, element);
	}

	/**
	 * @throws IndexOutOfBoundsException
	 *             - if the index is out of left range (index < 0)
	 */
	@Override
	public E get(int index) {
		int size = super.size();
		if (index >= size) {
			return null;
		}
		return super.get(index);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
