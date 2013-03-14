/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization;

import uk.ac.ebi.fg.core_model.toplevel.Accessible;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;


/**
 * This interface is used by BioSD {@link Normalizer normalisers}, to search for existing objects that should replace a
 * duplicate in the model instance that is being normalised.
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface Store
{
	/**
	 * targetIds are a set of strings that should be used to search for the existence of newObject (i.e., represent its
	 * search key). For example this may be a single string for an {@link Accessible}, two strings for an accession + version 
	 * when a {@link ReferenceSource} is being searched.
	 * 
	 * The result must return null if the new object was not found inside the store. The implementation can choose whether
	 * to save or not the new object before returning such null result. Of course the result is non-null when an object
	 * that has the same search key already exists in the store (in particular it can be newObject itself). 
	 *  
	 */
	public <T> T find ( T newObject, String... targetIds ); 
}
