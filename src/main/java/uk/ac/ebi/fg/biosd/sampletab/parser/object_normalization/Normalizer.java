/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization;

import uk.ac.ebi.arrayexpress2.sampletab.datamodel.MSI;
import uk.ac.ebi.fg.core_model.toplevel.Identifiable;

import com.google.common.collect.Table;


/**
 * Object normalisers are meant to take an instance of the BioSD object model and merge those objects that have to be
 * reused. This can be done in-memory, typically after having parsed a SampleTab submission, where duplicates created by
 * the underlining Limpopo-based parser are unified, or it can be done just before saving a {@link MSI a SampleTab Submission}
 * into a relational database, where objects that already exist in the DB replace the ones in memory (created from scratch).
 * 
 * The two type of normalisation are achieved using the same normaliser components (i.e., the subclasses of this class)
 * by simply using two different {@link Store} implementations, {@link MemoryStore one} that keeps/lookup objects in a 
 * {@link Table double-key hash table} and the {@link DBStore other one} that lookup existing objects into a relational database.  
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class Normalizer<I extends Identifiable>
{
	/** 
	 * This is used to check for the existence of an object and the normalisers are independent on specific stores against
	 * which object duplication is checked.
	 */
	protected Store store;
		
	/** 
	 * We have a hierarchy of components (reflecting the object model) that is initialised in tree-visit fashion from here. 
	 * All the components receive the same store from the top-level one.
	 */
	public Normalizer ( Store store )
	{
		super ();
		this.store = store;
	}
	
	/**
	 * This does the job. It checks those objects linked to the target that should be re-used. If a linked object already exists
	 * in the store, a new link is created to the already-existing object, replacing the orginal one.
	 * 
	 * @return usually true, if the target was new and not already in {@link #getStore()}, or if the target is null, or 
	 * if it has a non-null ID (i.e., we assume it's already in the DB and normalised). A sub-class implementation
	 * can decide whether to continue or not, based on its super call to this method. When it's not possible to search 
	 * for an object in the store (e.g., it hasn't an accession), this method should always return true.
	 * 
	 */
	public abstract boolean normalize ( I target );
	
	public Store getStore ()
	{
		return store;
	}
}
