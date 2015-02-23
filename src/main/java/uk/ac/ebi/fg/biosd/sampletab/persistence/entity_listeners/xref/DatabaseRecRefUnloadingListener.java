/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.xref.DatabaseRecordRef;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * Unloading pre/post processor for {@link ReferenceSource} entities.
 *
 * <dl><dt>date</dt><dd>Apr 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DatabaseRecRefUnloadingListener extends UnloadingListener<DatabaseRecordRef>
{
	public DatabaseRecRefUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}


	/**
	 * Removes all dangling {@link ReferenceSource} records.
	 */
	@Override
	public long postRemoveGlobally ()
	{
		// We previosuly used HQL, however this seems to have bad performance, due to temporary tables 
		// (http://in.relation.to/Bloggers/MultitableBulkOperations)
		
		String sql = "DELETE FROM db_rec_ref WHERE\n" +
			"id NOT IN ( SELECT db_rec_id FROM msi_db_rec_ref )\n" +
			"AND id NOT IN ( SELECT db_rec_id FROM sample_db_rec_ref )\n" +
			"AND id NOT IN ( SELECT db_rec_id FROM sg_db_rec_ref )\n";

		long result = entityManager.createNativeQuery ( sql ).executeUpdate (); 

		// TODO: AOP
		log.trace ( String.format ( "%s.postRemoveGlobally(): returning %d", this.getClass().getSimpleName (), result ));
		return result;
	}

}
