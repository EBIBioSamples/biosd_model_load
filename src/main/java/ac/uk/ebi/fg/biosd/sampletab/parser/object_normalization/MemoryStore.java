/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization;

import com.google.common.collect.ForwardingTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@SuppressWarnings ( "rawtypes" )
public class MemoryStore extends ForwardingTable<Class, String, Object> implements Store
{
	private Table<Class, String, Object> base = HashBasedTable.create ();
	
	public MemoryStore () {
		super ();
	}

	@Override
	protected Table<Class, String, Object> delegate () {
		return base;
	}

	/**
	 * A wrapper that search for this class and this identifier, casting the result to an instance of the targetClass. 
	 */
	@SuppressWarnings ( "unchecked" )
	public <T> T get ( Class<? extends T> targetClass, String targetId ) {
		return (T) super.get ( targetClass, targetId );
	} 

	/**
	 * Wraps {@link #get(Class, String)} with target.getClass().
	 */
	@SuppressWarnings ( "unchecked" )
	public <T> T get ( String targetId, T target ) {
		return (T) get ( target.getClass (), targetId );
	} 

	/**
	 * Invokes {@link #get(Class, String)} and, if the target object is not in the table, saves newObject (returning null). 
	 */
	public <T> T getOrPut ( Class<? extends T> targetClass, String targetId, T newObject ) 
	{
		T result = get ( targetClass, targetId );
		if ( result == null )
			put ( targetClass, targetId, newObject );
		return result;
	}
	
	/**
	 * A wrapper of {@link #getOrPut(Class, String, Object)} that uses newObject.getClass(). 
	 */
	public <T> T getOrPut ( String targetId, T newObject ) 
	{
		T result = get ( targetId, newObject );
		if ( result == null ) this.put ( targetId, newObject );
		return result;
	}

	/**
	 * Implements the interface using {@link #getOrPut(String, Object)}.
	 */
	@Override
	public <T> T find ( T newObject, String... targetIds  ) 
	{
		StringBuilder encodedId = new StringBuilder ();
		for ( String id: targetIds ) {
			if ( encodedId.length () > 0 ) encodedId.append ( ':' );
			encodedId.append ( id );
		}
		return getOrPut ( encodedId.toString (), newObject );
	} 
	
	
	/**
	 * Wraps {@link Table#put(Object, Object, Object)} with target.getClass(). 
	 */
	@SuppressWarnings ( "unchecked" )
	public <T> T put ( String targetId, T target ) {
		return (T) super.put ( target.getClass (), targetId, target );
	}
}
