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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.LocaleSelector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.restAPI.BaseNuxeoRestlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.OutputRepresentation;

/**
 * 
 * 
 * @since 7.3
 */
@Name("diffPicturesResultRestlet")
@Scope(ScopeType.EVENT)
public class DiffPicturesResultRestlet extends BaseNuxeoRestlet {

    private static final Log log = LogFactory.getLog(DiffPicturesResultRestlet.class);

    @In(create = true)
    protected NavigationContext navigationContext;

    protected CoreSession documentManager;

    protected DocumentModel leftDoc;

    protected DocumentModel rightDoc;
    
    @Override
    public void handle(Request req, Response res) {
        
        String leftDocId = (String) req.getAttributes().get("leftDocId");
        String rightDocId = (String) req.getAttributes().get("rightDocId");
        
        if(StringUtils.isBlank(leftDocId)) {
            handleError(res, "you must specify a left document as origin");
            return;
        }
        if(StringUtils.isBlank(rightDocId)) {
            handleError(res, "you must specify 'right' used for comparison against the left document.");
            return;
        }
        
        leftDoc = documentManager.getDocument(new IdRef(leftDocId));
        rightDoc = documentManager.getDocument(new IdRef(rightDocId));
        
        Blob bLeft, bRight, bResult;
        bLeft = (Blob) leftDoc.getPropertyValue("file:content");
        bRight = (Blob) rightDoc.getPropertyValue("file:content");
        DiffPictures dp = new DiffPictures(bLeft, bRight);

        try {
            bResult = dp.compare(null, null);
        } catch (CommandNotAvailable | IOException e) {
            log.error("Unable to compare the pictures", e);
            handleError(res, "Unable to compare the pictures");
            return;
        }
        
        HttpServletResponse response = getHttpResponse(res);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        
        try {
            handleCompareResult(res, bResult, "");
        } catch (IOException e) {
            log.error("Unable to handleCompareResult", e);
            handleError(res, "Unable to return the result");
            return;
        }
        
    }
    
    protected void handleCompareResult(Response res, final Blob blob, String mimeType) throws IOException {
        // blobs are always persistent, and temporary blobs are GCed only when not referenced anymore
        res.setEntity(new OutputRepresentation(null) {
            @Override
            public void write(OutputStream outputStream) throws IOException {
                try (InputStream stream = blob.getStream()) {
                    IOUtils.copy(stream, outputStream);
                }
            }
        });
        HttpServletResponse response = getHttpResponse(res);

        response.setHeader("Content-Disposition", "inline");
        response.setContentType(mimeType);
    }
    
}
