package com.github.goforce.fsmpdfmerge.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.goforce.fsmpdfmerge.model.Param;

import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.*;
import com.sforce.ws.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
//import org.apache.pdfbox.pdmodel.common.PDMetadata;

@RestController
public class Controller {

    @RequestMapping( value = "/merge", method = RequestMethod.POST )
    public ResponseEntity<Object> postCall( @RequestBody Param param ) {


        try {
            ConnectorConfig config = new ConnectorConfig();
            config.setServiceEndpoint( param.getServiceUrl() );
            config.setSessionId( param.getSessionId() );

            PartnerConnection conn = Connector.newConnection( config );

            String[] cdIds = new String[]{ param.getContentDocument1Id(), param.getContentDocument2Id() };
            System.out.println( "ContentDocument Ids submitted: " + Arrays.toString( cdIds ) );
            SObject[] cds = conn.retrieve( "Id,LatestPublishedVersionId", "ContentDocument", cdIds );

            String cv1Id = (String) cds[0].getField( "LatestPublishedVersionId" );
            String cv2Id = (String) cds[1].getField( "LatestPublishedVersionId" );

            SObject[] cvs = conn.retrieve( "Id,VersionData", "ContentVersion", new String[]{ cv1Id, cv2Id } );
            String pdf1 = (String) cvs[0].getField( "VersionData" );
            String pdf2 = (String) cvs[1].getField( "VersionData" );
            InputStream is1 = new ByteArrayInputStream( Base64.getDecoder().decode( pdf1 ) );
            InputStream is2 = new ByteArrayInputStream( Base64.getDecoder().decode( pdf2 ) );

            ByteArrayOutputStream mergedPDFOutputStream = new ByteArrayOutputStream();
            PDFMergerUtility pdfMerger = new PDFMergerUtility();
            pdfMerger.addSources( Arrays.asList( is1, is2 ) );
            pdfMerger.setDestinationStream( mergedPDFOutputStream );

            PDDocumentInformation pdfDocumentInfo = new PDDocumentInformation();
            pdfDocumentInfo.setTitle( "Service Report" );
            pdfDocumentInfo.setCreator( "fsm-pdf-merge" );
            pdfDocumentInfo.setSubject( "merged pdf files" );
            pdfMerger.setDestinationDocumentInformation( pdfDocumentInfo );

            pdfMerger.mergeDocuments( MemoryUsageSetting.setupMainMemoryOnly() );

            // upload new ContentVersion with merged pdf
            SObject content = new SObject( "ContentVersion" );

            // Populate the ContentVersion fields
            content.setField( "PathOnClient", "MergedMegaServiceReport.pdf" );
            content.setField( "Title", "Very Final Service Report" );
            content.setField( "Description", "Service report merged from 1 and 2" );
            //content.setField("VersionData", Base64.getEncoder().encodeToString("this is text message".getBytes()));
            content.setField( "VersionData", mergedPDFOutputStream.toByteArray() );

            // Upload the ContentVerion
            String mergedId = null;
            {
                SaveResult[] srs = conn.create( new SObject[] { content } );
                if ( srs[0].isSuccess() ) {
                    System.out.println( "Merged ContentVersion created : " + srs[0].getId() );
                    mergedId = srs[0].getId();
                } else {
                    System.out.println( "Merged ContentVersion failed --------->" );
                    com.sforce.soap.partner.Error[] errors = srs[0].getErrors();
                    for ( int i = 0; i < errors.length; i++) {
                        System.out.println( errors[i].getMessage() );
                    }
                    System.out.println("<---------" );
                    return new ResponseEntity<>( "FAILED_BIG_WAY", HttpStatus.BAD_REQUEST );
                }
            }

            // add link to work order
            SObject[] cvm = conn.retrieve( "Id,ContentDocumentId", "ContentVersion", new String[]{ mergedId } );
            String cdmId = (String) cvm[0].getField( "ContentDocumentId" );
            SObject cdl = new SObject( "ContentDocumentLink" );
            cdl.setField( "ContentDocumentId", cdmId );
            cdl.setField( "LinkedEntityId", param.getWorkOrderId() );
            cdl.setField( "ShareType", "V" );
            cdl.setField( "Visibility", "AllUsers" );
            {
                SaveResult[] srs = conn.create( new SObject[] { cdl } );
                if ( srs[0].isSuccess() ) {
                    System.out.println( "ContentDocumentLink created : " + srs[0].getId() );
                } else {
                    System.out.println( "ContentDocumentLink failed --------->" );
                    com.sforce.soap.partner.Error[] errors = srs[0].getErrors();
                    for ( int i = 0; i < errors.length; i++) {
                        System.out.println( errors[i].getMessage() );
                    }
                    System.out.println("<---------" );
                    return new ResponseEntity<>( "FAILED_BIG_WAY", HttpStatus.BAD_REQUEST );
                }
            }

            return new ResponseEntity<>( mergedId, HttpStatus.OK );

        } catch ( Exception e ) {
            e.printStackTrace();
            return new ResponseEntity<>( e.toString(), HttpStatus.OK );
        }
    }

    @RequestMapping(value = "/merge")
    public ResponseEntity<Object> getCall() {
        return new ResponseEntity<>( "All Good", HttpStatus.OK );
    }

}
