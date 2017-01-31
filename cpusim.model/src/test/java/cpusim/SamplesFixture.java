package cpusim;

import cpusim.model.Machine;
import cpusim.xml.MachineReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertNotNull;

/**
 * Object hierarchy for loading sample models.
 *
 */
public enum SamplesFixture {

    SIM6502("6502", "sim6502v%VERSION%.cpu", 3),

    COURSE_WOMBAT("course/Wombat%VERSION%", "Wombat%VERSION%.cpu", 6),
    COURSE_JVM("course/JVM%VERSION%/", "JVM%VERSION%.cpu", 4),

    MAXWELL("MaxwellComputer", "Maxwell.cpu", 1),

    WOMBAT("Wombat%VERSION%", "Wombat%VERSION%.cpu", 2)
    ;

    private final String folderUri;
    private final String cpuUri;
    private final int versionCount;

    SamplesFixture(final String folderUri, String cpu, int versionCount) {
        this.folderUri = (!folderUri.endsWith("/") ? folderUri : folderUri.substring(0, folderUri.length() - 1));
        this.cpuUri = this.folderUri + '/' + cpu;
        this.versionCount = versionCount;
    }

    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:([^:]+):)?VERSION%");

    public Optional<Machine> load(final int version) throws ParserConfigurationException, SAXException, IOException {
        String uri = this.cpuUri;
        Matcher m = FORMAT_PATTERN.matcher(this.cpuUri);
        if (m.find()) {
            uri = m.replaceAll(Integer.toString(version));
        }

        return loadCPU(uri);
    }

    public List<Optional<Machine>> loadAll() throws IOException, SAXException, ParserConfigurationException {
        List<Optional<Machine>> out = new ArrayList<>(versionCount);
        for (int i = 0; i < versionCount; ++i) {
            try {
                Optional<Machine> opt = null;

                try {
                    opt = load(i);
                } catch (AssertionError e) {
                    if (!e.getMessage().contains("Could not load stream for path: ")) {
                        throw e;
                    }
                }

                if (opt == null) {
                    continue; // v0 doesnt always exist
                }

                out.add(opt);
            } catch (Exception e) {
                // rethrow it with more info
                throw new IllegalStateException("Failed to load: " + this + " v" + i, e);
            }
        }

        return out;
    }

    /**
     * Reads a {@link Machine} instance from the URI passed.
     * @param uri
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private static Optional<Machine> loadCPU(final String uri) throws IOException, ParserConfigurationException, SAXException {

        checkNotNull(uri);

        final String realPath = "/cpus/" + uri;


        try (InputStream stream = SamplesFixture.class.getResourceAsStream(realPath)) {
            assertNotNull("Could not load stream for path: " + realPath, stream);

            MachineReader reader = new MachineReader();
            reader.parseDataFromStream(stream);

            return reader.getMachine();
        }
    }




}
