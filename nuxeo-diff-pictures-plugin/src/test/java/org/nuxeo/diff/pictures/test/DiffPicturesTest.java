/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     thibaud
 */

package org.nuxeo.diff.pictures.test;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.diff.pictures.DiffPictures;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * 
 */

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@Deploy({ "org.nuxeo.ecm.platform.picture.core", "nuxeo-diff-pictures",
        "org.nuxeo.ecm.platform.commandline.executor" })
public class DiffPicturesTest {

    protected static final Log log = LogFactory.getLog(DiffPicturesTest.class);

    protected static final String ISLAND_PNG = "island.png";

    protected static final int ISLAND_W = 400;

    protected static final int ISLAND_H = 282;

    protected static final String ISLAND_MODIF_PNG = "island-modif.png";

    @Inject
    CoreSession coreSession;

    @Before
    public void setUp() {
        // . . .
    }

    @After
    public void cleanup() {
        // . . .
    }

    @Test
    public void testDefaultValues() throws Exception {

        File img1 = FileUtils.getResourceFileFromContext(ISLAND_PNG);
        File img2 = FileUtils.getResourceFileFromContext(ISLAND_MODIF_PNG);

        FileBlob blob1 = new FileBlob(img1);
        FileBlob blob2 = new FileBlob(img2);

        Blob result;
        DiffPictures dp = new DiffPictures(blob1, blob2);

        result = dp.compare(null, null);

        BufferedImage bi = checkIsImage(result);
        assertEquals(bi.getWidth(), ISLAND_W);
        assertEquals(bi.getHeight(), ISLAND_H);

        deleteFile(result);

    }

    @Test
    public void testFuzz() throws Exception {

        File img1 = FileUtils.getResourceFileFromContext(ISLAND_PNG);
        long len1 = img1.length();
        File img2 = FileUtils.getResourceFileFromContext(ISLAND_MODIF_PNG);
        File aFile;

        FileBlob blob1 = new FileBlob(img1);
        FileBlob blob2 = new FileBlob(img2);

        Blob result;
        HashMap<String, Serializable> params;
        DiffPictures dp = new DiffPictures(blob1, blob2);

        // With these island.png, a blur of 10% reduce the comparison result
        // (when using the default color values). Reduces it a lot.
        // WARNING: This is a 100% dependence on ImageMagic
        params = new HashMap<String, Serializable>();
        params.put("fuzz", "10%");
        result = dp.compare(null, params);

        BufferedImage bi = checkIsImage(result);
        assertEquals(bi.getWidth(), ISLAND_W);
        assertEquals(bi.getHeight(), ISLAND_H);

        aFile = ((FileBlob) result).getFile();
        assertTrue("Result image with fuzz should be lower than original",
                aFile.length() < (len1 / 2));

        deleteFile(result);

    }
    
    @Test
    public void testSamePicture() throws Exception {

        File img1 = FileUtils.getResourceFileFromContext(ISLAND_PNG);
        File img2 = FileUtils.getResourceFileFromContext(ISLAND_PNG);

        FileBlob blob1 = new FileBlob(img1);
        FileBlob blob2 = new FileBlob(img2);

        Blob result;
        HashMap<String, Serializable> params;
        DiffPictures dp = new DiffPictures(blob1, blob2);

        // We make the whole thing red
        params = new HashMap<String, Serializable>();
        params.put("highlightColor", "Red");
        params.put("lowlightColor", "Red");
        result = dp.compare(null, params);

        BufferedImage bi = checkIsImage(result);
        assertEquals(bi.getWidth(), ISLAND_W);
        assertEquals(bi.getHeight(), ISLAND_H);
        
        // Test a 40x40 rectangle in the middle, where every pixel should be red
        int start_i = (ISLAND_W / 2) - 20;
        int max_i = (ISLAND_W / 2) + 20;
        int start_j = (ISLAND_H / 2) - 20;
        int max_j = (ISLAND_H / 2) + 20;

        for (int i = start_i; i < max_i; i++) {
            for (int j = start_j; j < max_j; j++) {
                int pixel = bi.getRGB(i, j);
                int r = pixel >> 16 & 0xff;
                int g = pixel >> 8 & 0xff;
                int b = pixel & 0xff;
                
                assertTrue("r should be 255", r == 255);
                assertTrue("g should be 0", g == 0);
                assertTrue("b should be 0", b == 0);
            }
        }
        
        deleteFile(result);

    }

    protected BufferedImage checkIsImage(Blob inBlob) throws Exception {

        assertTrue(inBlob instanceof FileBlob);
        return checkIsImage((FileBlob) inBlob);
    }

    protected BufferedImage checkIsImage(FileBlob inBlob) throws Exception {

        assertNotNull(inBlob);

        File f = inBlob.getFile();
        assertNotNull(f);
        assertTrue(f.length() > 0);

        BufferedImage bi;
        try {
            bi = ImageIO.read(f);
            assertNotNull(bi);

        } catch (IOException e) {
            throw new Exception("Error reading the file", e);
        }

        return bi;

    }

    protected void deleteFile(Blob inBlob) {

        if (inBlob instanceof FileBlob) {
            File f = inBlob.getFile();
            if (f != null) {
                f.delete();
            }
        }

    }

}
