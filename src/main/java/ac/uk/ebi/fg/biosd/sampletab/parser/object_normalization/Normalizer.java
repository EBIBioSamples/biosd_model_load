/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization;

import com.google.common.collect.Table;

import uk.ac.ebi.arrayexpress2.sampletab.datamodel.MSI;
import uk.ac.ebi.fg.core_model.toplevel.Identifiable;


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
	protected Store store;
	
	public Normalizer ( Store store )
	{
		super ();
		this.store = store;
	}

	public abstract void normalize ( I target );
	
	public Store getStore ()
	{
		return store;
	}
}
