package ch.mas.tacy.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Some confidence methods handling Lists
 *
 * @author P.Büttiker
 */
public class Lists {


	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * Creates a new ArrayList from the given iterable
	 * @param items
	 * @return
	 */
	public static <T> List<T> newList(Iterable<T> items){
		ArrayList<T> list;

		if(items instanceof Collection<?>)
			list = new ArrayList<T>((Collection<T>)items);
		else{
			list = new ArrayList<T>();
			if(items != null)
			{
				for (T item : items) {
					list.add(item);
				}
			}
		}
		return list;
	}

	public static boolean isEmpty(Iterable<?> items){
		return Lists.toList(items).isEmpty();
	}


	/**
	 * Cast the iterable to an arraylist if possible. If not, a new arraylist is created
	 * @param items
	 * @return
	 */
	public static <T> List<T> toList(Iterable<T> items){
		List<T> list;

		if(items instanceof ArrayList<?>)
			list = (ArrayList<T>)items;
		else 
			list = newList(items);

		return list;
	}

	/**
	 * Type-safe list casting
	 * Cast the list down to the given type
	 * @param items
	 * @return
	 */
	public static <A, T extends A> List<A> asList(Class<A> clazz, Iterable<T> items){
		List<A> list = new ArrayList<A>();
		for (T item : items) {
			list.add(item);
		}
		return list;
	}





	/**
	 * Convert the given Array in a List. Null values are skiped!
	 * @param items
	 * @return
	 */
	public static <T> List<T> asNoNullList(T[] items){
		List<T> list = new ArrayList<T>();
		for(T iT : items){
			if(iT != null)
				list.add(iT);
		}
		return list;
	}


	/**
	 * 
	 * @param items
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> asTypedList(Iterable<?> items){	
		ArrayList<T> list = new ArrayList<T>();
		for(Object object : items)
		{
			list.add((T)object);
		}
		return list;
	}


	public static <T> List<T> asList(T... items){
		return Lists.toList(Arrays.asList(items));
	}



	public static <T> T getFirst(Iterable<T> list) {
		return getFirst(toList(list));
	}

	public static <T> T getLast(Iterable<T> list) {
		return getLast(toList(list));
	}

	/**
	 * Returns the first element from the list     
	 * @param list
	 * @return
	 */
	public static <T> T getFirst(List<T> list) {
		T first	= null;
		if(!list.isEmpty())
		{
			first = list.get(0);
		}
		return first;
	}

	/**
	 * Returns the last element form the list
	 * @param list
	 * @return
	 */
	public static <T> T getLast(List<T> list) {
		T last	= null;
		if(!list.isEmpty())
		{
			last = list.get(list.size()-1);
		}
		return last;
	}



}
