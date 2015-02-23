package uk.ac.ebi.fg.biosd.sampletab.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.sampletab.comparator.ComparatorSampleData;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.exporter.Exporter;
import uk.ac.ebi.fg.biosd.sampletab.loader.Loader;

/**
 * This test applies to the conversion between Limpopo SampleTab objects and relational objects,
 * but does not write to any database.
 *
 */
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
        log.info("SampleTab file "+resourcePath+" loaded");
        
        SampleTabWriter stwr = new SampleTabWriter (new PrintWriter(System.out));
        
        /*
        try {
            stwr.write(sd);
        } catch (IOException e) {
            log.error("problem writing loaded sampletab");
            fail();
        } finally {
            try {
                stwr.close();
            } catch (IOException e) {
                //do nothing
            }
        }
        */
        
        Loader loader = new Loader();
        MSI msi = null;
        msi = loader.fromSampleData(sd);

        log.info("SampleTab file "+resourcePath+" converted");
        
        Exporter exporter = new Exporter();
        SampleData sdExported = null;
        try {
            sdExported = exporter.fromMSI(msi);
        } catch (ParseException e) {
            log.error("Problem parsing", e);
            fail();
        }

        log.info("SampleTab file "+resourcePath+" reconverted");
        /*
        stwr = new SampleTabWriter (new PrintWriter(System.out));
        try {
            stwr.write(sdExported);
        } catch (IOException e) {
            log.error("problem writing exported sampletab");
            fail();
        } finally {
            try {
                stwr.close();
            } catch (IOException e) {
                //do nothing
            }
        }
        */
        ComparatorSampleData csd = new ComparatorSampleData();
        assertEquals(0, csd.compare(sd, sdExported));
        
        return msi;
    }
    

    @Ignore ( "Not currently working" )
    @Test
    public void testAE(){
        MSI msi = doTests("GAE-MTAB-27/sampletab.toload.txt");
    }

    @Ignore ( "Not currently working" )
    @Test
    public void testSRA(){
        MSI msi = doTests("GEN-SRP001145/sampletab.toload.txt");
    }

    @Ignore ( "Not currently working" )
    @Test
    public void testPRIDE(){
        MSI msi = doTests("GPR-3218/sampletab.toload.txt");
    }

    @Ignore ( "Not currently working" )
    @Test
    public void testIMSR(){
        MSI msi = doTests("GMS-RBRC/sampletab.toload.txt");
    }
    

}
