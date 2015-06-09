/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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
package org.nuxeo.diff.pictures;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.runtime.api.Framework;

/**
 * 
 *
 * @since 7.3
 */
public class DiffPictures {

    private static final Log log = LogFactory.getLog(DiffPictures.class);

    public static final String DIFF_PICTURES_DEFAULT_COMMAND = "diff-pictures-default";

    protected static final String TEMP_DIR_PATH = System.getProperty("java.io.tmpdir");

    Blob b1;

    Blob b2;

    String commandLine;

    Map<String, Serializable> clParameters;

    public DiffPictures(Blob inB1, Blob inB2) {
        b1 = inB1;
        b2 = inB2;
    }

    public Blob compare(String inCommandLine, Map<String, Serializable> inParams)
            throws CommandNotAvailable, IOException {

        Blob result = null;
        String finalName;

        commandLine = StringUtils.isBlank(inCommandLine) ? DIFF_PICTURES_DEFAULT_COMMAND
                : inCommandLine;

        clParameters = inParams == null ? new HashMap<String, Serializable>()
                : inParams;

        finalName = (String) clParameters.get("targetFileName");
        if (StringUtils.isBlank(finalName)) {
            finalName = "comp-" + b1.getFilename();
        }

        // Assume the blob is backed by a File
        CmdParameters params = new CmdParameters();
        String sourceFilePath = b1.getFile().getAbsolutePath();
        params.addNamedParameter("file1", sourceFilePath);

        sourceFilePath = b2.getFile().getAbsolutePath();
        params.addNamedParameter("file2", sourceFilePath);

        checkDefaultParametersValues();
        for (Entry<String, Serializable> entry : clParameters.entrySet()) {
            params.addNamedParameter(entry.getKey(), (String) entry.getValue());
        }

        String destFilePath = TEMP_DIR_PATH + "/" + System.currentTimeMillis() + "-"
                + finalName;
        
        File tempDestFile = new File(destFilePath);
        tempDestFile.createNewFile();
        /*
          tempDestFile = File.createTempFile(TEMP_FILE_PREFIX,
                        destFileExtension);
                tempDestFile.deleteOnExit();
                Framework.trackFile(tempDestFile, this);
                params.addNamedParameter("targetFilePath",
                        tempDestFile.getAbsolutePath());
         */
        params.addNamedParameter("targetFilePath", destFilePath);

        CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
        ExecResult execResult = cles.execCommand(commandLine, params);

        /*
        if(execResult.getError() != null) {
            throw new ClientException("Failed to execute the command <"
                    + commandLine + ">", execResult.getError());
        }
        */
        /*
        if (!execResult.isSuccessful()) {
            throw new ClientException("Failed to execute the command <"
                    + commandLine + ">. Final command [ "
                    + execResult.getCommandLine()
                    + " ] returned with error "
                    + execResult.getReturnCode());
        }
        */
        
        File fileResult = new File(destFilePath);
        result = new FileBlob(fileResult);

        if (result != null) {
            result.setFilename(finalName);
        }
        return result;
    }

    /*
     * Adds the default values if a parameter is missing.
     * 
     * This applies for all command lines (and some will be unused)
     */
    protected void checkDefaultParametersValues() {

        if (clParameters.get("fuzz") == null) {
            clParameters.put("fuzz", "0");
        }

        if (clParameters.get("highlightColor") == null) {
            clParameters.put("highlightColor", "Red");
        }

        if (clParameters.get("lowlightColor") == null) {
            clParameters.put("lowlightColor", "White");
        }

    }

}
