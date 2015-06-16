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

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

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

        File img1 = FileUtils.getResourceFileFromContext("island.png");
        File img2 = FileUtils.getResourceFileFromContext("island-modif.png");

        FileBlob blob1 = new FileBlob(img1);
        FileBlob blob2 = new FileBlob(img2);

        Blob result;
        DiffPictures dp = new DiffPictures(blob1, blob2);

        result = dp.compare(null, null);
        assertNotNull(result);
        assertTrue(result instanceof FileBlob);
        ((FileBlob) result).getFile().delete();

    }

    @Test
    public void testFuzz() throws Exception {

        File img1 = FileUtils.getResourceFileFromContext("island.png");
        long len1 = img1.length();
        File img2 = FileUtils.getResourceFileFromContext("island-modif.png");
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
        assertNotNull(result);
        assertTrue(result instanceof FileBlob);
        aFile = ((FileBlob) result).getFile();
        assertTrue("Result image with fuzz should be lower than original", aFile.length() < (len1 /2));
        aFile.delete();

    }

}
