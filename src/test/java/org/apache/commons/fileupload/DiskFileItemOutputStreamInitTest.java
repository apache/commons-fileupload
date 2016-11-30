import org.apache.commons.fileupload.disk.DiskFileItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Some of the methods of the {@link DiskFileItem} rely on the {@link java.io.OutputStream} of the underlying {@link File}.
 * Therefore we need to make sure that the output stream is initialized on time, to avoid {@link NullPointerException}s.
 */
public class DiskFileItemOutputStreamInitTest {

    private DiskFileItem diskFileItem;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        File file = new File("");
        diskFileItem = new DiskFileItem(null,
                                        "application/txt",
                                        false,
                                        "file.txt",
                                        1000,
                                        file);

    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetSizeWithoutAccessingOutputStreamBefore() {
        diskFileItem.getSize();
    }

    @Test
    public void testGetInputStreamWithoutAccessingOutputStreamBefore() throws IOException {
        diskFileItem.getInputStream();
    }

    @Test
    public void testIsInMemoryWithoutAccessingOutputStreamBefore() {
        diskFileItem.getSize();
    }

    @Test
    public void testGetWithoutAccessingOutputStreamBefore() {
        diskFileItem.get();
    }

    @Test
    public void testGetterWithAccessingOutputStreamBefore() throws IOException {
        // Have to initiate the output stream, so internal functions in DiskFileItem work.
        diskFileItem.getOutputStream();
        diskFileItem.getSize();
        diskFileItem.getInputStream();
        diskFileItem.isInMemory();
        diskFileItem.get();
    }

}
