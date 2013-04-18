/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.organizational;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.BioSampleUnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.CVTermUnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.OntologyEntryUnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref.ReferenceSourceUnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref.XRefUnloadingListener;

/**
 * This is the entry point to all the listeners. Several objects linked to the MSI are taken care from here.
 *
 * <dl><dt>date</dt><dd>Apr 8, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MSIUnloadingListener extends UnloadingListener<MSI>
{

	public MSIUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	@Override
	public long preRemove ( MSI msi ) 
	{
		long result = 0;
		AccessibleDAO<BioSampleGroup> sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, entityManager );
		
		for ( BioSampleGroup sg: msi.getSampleGroups () ) {
			// There are other MSIs linked to this
			if ( sg.getMSIs ().size () > 1 ) continue;
			if ( sgDao.delete ( sg ) ) result++;
		}
		
		return result;
		
		// TODO: This would be the smart way of doing it, but it seems Hibernat ignore relations when dealing DML statements.
		
// 	 	String sql = "DELETE FROM BIO_SAMPLE_SAMPLE_GROUP br WHERE br.group_id IN ( SELECT group_id FROM msi_sample_group br1 " +
//		"  JOIN msi ON br1.msi_id = msi.id WHERE msi.acc = :msiAcc )";
//		
//		result = entityManager.createNativeQuery ( sql ).setParameter ( "msiAcc", msi.getAcc () ).executeUpdate ();
//		
//		// TODO: the same for sg->propertyValue and who knows what else. Maybe a better strategy is to work via Java 
//		
//		String hql = String.format ( 
//			"DELETE FROM %1$s AS sg WHERE sg IN ( SELECT sgA.id FROM %1$s sgA JOIN sgA.MSIs msiA WHERE msiA.acc = :msiAcc )", 
//			BioSampleGroup.class.getName () 
//		);
//		return result += entityManager.createQuery ( hql ).setParameter ( "msiAcc", msi.getAcc () ).executeUpdate ();
	}

	@Override
	public long postRemove ( MSI msi )
	{
		long result = new BioSampleUnloadingListener ( entityManager ).postRemove ( null );
		result += new OntologyEntryUnloadingListener ( entityManager ).postRemove ( null );
		result += new XRefUnloadingListener ( entityManager ).postRemove ( null );
		result += new ReferenceSourceUnloadingListener ( entityManager ).postRemove ( null );
		result += new CVTermUnloadingListener ( entityManager ).postRemove ( null );
		
		return result;
	}
}
