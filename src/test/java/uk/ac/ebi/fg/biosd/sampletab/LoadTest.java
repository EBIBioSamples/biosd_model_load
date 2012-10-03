package uk.ac.ebi.fg.biosd.sampletab;

import java.net.URL;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

import ac.uk.ebi.fg.biosd.sampletab.Load;

import static org.junit.Assert.fail;

public class LoadTest {

    @Rule
    public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( Resources.getInstance ().getEntityManagerFactory () );
    private EntityManager em;
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private MSI doTests(String resourcePath){
        URL url = getClass().getClassLoader().getResource(resourcePath);
        
        Load load = new Load();
        
        MSI msi = null;
        
        try {
            msi = load.fromSampleData(url);
        } catch (ParseException e) {
            log.error("Problem parsing", e);
            fail();
        }
        
        em = emProvider.newEntityManager ();
        AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );
        EntityTransaction ts = em.getTransaction ();
        ts.begin ();
        dao.getOrCreate ( msi );
        ts.commit ();
        
        return msi;
    }
    
    @Test
    public void testAE(){
        MSI msi = doTests("GAE-MTAB-27/sampletab.txt");
        //TODO finish writing test
    }

    @Test
    public void testSRA(){
        MSI msi = doTests("GEN-SRP001145/sampletab.txt");
        //TODO finish writing test
    }

    @Test
    public void testPRIDE(){
        MSI msi = doTests("GPR-3218/sampletab.txt");
        //TODO finish writing test
    }

    @Test
    public void testIMSR(){
        MSI msi = doTests("GMS-RBRC/sampletab.txt");
        //TODO finish writing test
    }
    

}
