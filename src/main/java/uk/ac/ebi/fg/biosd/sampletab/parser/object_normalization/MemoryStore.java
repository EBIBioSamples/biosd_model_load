package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization;

import uk.ac.ebi.fg.core_model.toplevel.Annotation;

import com.google.common.collect.ForwardingTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * An in-memory store implementation that is used to normalise the result spawn by the SampleTab parser. This is based
 * on a {@link Table two-level key table}, where valued are keyed by type (i.e., Java class) and primary key (e.g., accession).  
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@SuppressWarnings ( "rawtypes" )
public class MemoryStore extends ForwardingTable<Class, Object, Object> implements Store
{
	
	/**
	 * The backing two-level key map.
	 */
	private Table<Class, Object, Object> base = HashBasedTable.create ();
	
	public MemoryStore () {
		super ();
	}

	/**
	 * @return {@link #base}.
	 */
	@Override
	protected Table<Class, Object, Object> delegate () {
		return base;
	}

	/**
	 * A wrapper that search for this class and this identifier, casting the result to an instance of the targetClass. 
	 */
	@SuppressWarnings ( "unchecked" )
	private <T> T getFromClass ( Class<? extends T> targetClass, Object targetId ) {
		return (T) super.get ( targetClass, targetId );
	} 

	/**
	 * Wraps {@link #get(Class, String)} with target.getClass().
	 */
	@SuppressWarnings ( "unchecked" )
	private <T> T getFromObjectClass ( Object targetId, T target ) {
		return (T) getFromClass ( target.getClass (), targetId );
	} 

	
	/**
	 * A wrapper of {@link #getOrPut(Class, String, Object)} that uses newObject.getClass(). 
	 */
	private <T> T getOrPut ( Object targetId, T newObject ) 
	{
		T result = getFromObjectClass ( targetId, newObject );
		if ( result == null ) this.put ( targetId, newObject );
		return result;
	}

	/**
	 * Implements {@link Store#find(Object, String...)} using {@link #getOrPut(String, Object)}.
	 */
	@Override
	public <T> T find ( T newObject, String... targetIds  ) 
	{
		if ( newObject instanceof Annotation )
			return getOrPut ( newObject, newObject );

		// When IDs are not specified, use the object itself
		if ( targetIds == null )
			return getOrPut ( newObject, newObject );
		
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
	private <T> T put ( Object targetId, T target ) {
		return (T) super.put ( target.getClass (), targetId, target );
	}
}
