package uk.ac.ebi.fg.biosd.sampletab;

import java.net.URL;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.sampletab.comparator.ComparatorSampleData;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

import ac.uk.ebi.fg.biosd.sampletab.Export;
import ac.uk.ebi.fg.biosd.sampletab.Load;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

public class RoundTripTest {
    
    private Logger log = LoggerFactory.getLogger(getClass());

    private MSI doTests(String resourcePath){
        URL url = getClass().getClassLoader().getResource(resourcePath);
               
        SampleTabSaferParser parser = new SampleTabSaferParser();
        SampleData sd = null;
        
        try {
            sd = parser.parse(url);
        } catch (ParseException e) {
            log.error("Problem parsing", e);
            fail();
        }

        Load load = new Load();
        MSI msi = null;
        msi = load.fromSampleData(sd);
        
        Export export = new Export();
        SampleData sdExported = null;
        try {
            sdExported = export.fromMSI(msi);
        } catch (ParseException e) {
            log.error("Problem parsing", e);
            fail();
        }
                
        ComparatorSampleData csd = new ComparatorSampleData();
        assertEquals(csd.compare(sd, sdExported), 0);
        
        return msi;
    }
    

    @Ignore ( "Not currently working" )
    @Test
    public void testAE(){
        MSI msi = doTests("GAE-MTAB-27/sampletab.txt");
    }

    @Ignore ( "Not currently working" )
    @Test
    public void testSRA(){
        MSI msi = doTests("GEN-SRP001145/sampletab.txt");
    }

    @Ignore ( "Not currently working" )
    @Test
    public void testPRIDE(){
        MSI msi = doTests("GPR-3218/sampletab.txt");
    }

    @Ignore ( "Not currently working" )
    @Test
    public void testIMSR(){
        MSI msi = doTests("GMS-RBRC/sampletab.txt");
    }
    

}
