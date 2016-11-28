import org.apache.commons.fileupload.disk.DiskFileItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;


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

    @Test(expected = NullPointerException.class)
    public void testGetSizeWithoutAccessingOutputStreamBefore() {
        diskFileItem.getSize();
    }

    @Test
    public void testGetSizeWithAccessingOutputStreamBefore() throws IOException {
        // Have to initiate the outputstream, so internal functions in DiskFileItem work.
        diskFileItem.getOutputStream();
        diskFileItem.getSize();
    }

}