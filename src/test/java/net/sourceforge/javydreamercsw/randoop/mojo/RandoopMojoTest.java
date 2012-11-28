package net.sourceforge.javydreamercsw.randoop.mojo;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import static org.junit.Assert.*;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class RandoopMojoTest extends AbstractMojoTestCase {

    public RandoopMojoTest() {
    }

    @Override
    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
    }

    /**
     * @throws Exception
     */
    public void testMojoConfig() throws Exception {
        File testPom = new File(getBasedir(),
                "src/test/resources/net/sourceforge/javydreamercsw/randoop/mojo/"
                + "my-dummy-maven-project/Dummy/pom.xml");
        GenerateAllTestsMojo mojo = (GenerateAllTestsMojo) lookupMojo("generate-all-tests", testPom);
        assertNotNull(mojo);
        //Load the dummy project
        Model model = null;
        FileReader reader;
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        try {
            reader = new FileReader(testPom);
            model = mavenreader.read(reader);
        } catch (Exception ex) {
            fail();
        }
        MavenProject dummy = new MavenProject(model);
        mojo.setPluginContext(new HashMap());
        mojo.getPluginContext().put("project", dummy);
        mojo.execute();
    }
}
