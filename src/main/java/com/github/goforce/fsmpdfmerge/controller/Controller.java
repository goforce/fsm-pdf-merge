package com.github.goforce.fsmpdfmerge.controller;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
//import java.util.HashMap;
//import java.util.Map;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.goforce.fsmpdfmerge.model.*;

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
    public ResponseEntity<Result> postCall( @RequestBody Param param ) {

        Result result = new Result();
        ArrayList<String> createdContentVersionIds = new ArrayList<String>();

        try {
            ConnectorConfig config = new ConnectorConfig();
            config.setServiceEndpoint( param.getServiceUrl() );
            config.setSessionId( param.getSessionId() );

            PartnerConnection conn = Connector.newConnection( config );

            for ( MergeDoc doc : param.getMergeDocs() ) {

                String[] contentVersionIds = doc.getContentVersionIds();
                System.out.println( "ContentVersion Ids submitted: " + Arrays.toString( contentVersionIds ) );
                SObject[] cvs = conn.retrieve( "Id,VersionData", "ContentVersion", contentVersionIds );
                List<InputStream> iss = new ArrayList<InputStream>();
                for ( int i = 0; i < cvs.length; i++ ) {
                    String pdf = (String) cvs[i].getField( "VersionData" );
                    InputStream is = new ByteArrayInputStream( Base64.getDecoder().decode( pdf ) );
                    iss.add( is );
                }

                ByteArrayOutputStream mergedPDFOutputStream = new ByteArrayOutputStream();
                PDFMergerUtility pdfMerger = new PDFMergerUtility();
                pdfMerger.addSources( iss );
                pdfMerger.setDestinationStream( mergedPDFOutputStream );

                PDDocumentInformation pdfDocumentInfo = new PDDocumentInformation();
                pdfDocumentInfo.setTitle( doc.getTitle() );
                pdfDocumentInfo.setCreator( "fsm-pdf-merge" );
                pdfDocumentInfo.setSubject( doc.getDescription() );
                pdfMerger.setDestinationDocumentInformation( pdfDocumentInfo );

                pdfMerger.mergeDocuments( MemoryUsageSetting.setupMainMemoryOnly() );

                // upload new ContentVersion with merged pdf
                SObject content = new SObject( "ContentVersion" );

                // Populate the ContentVersion fields
                if ( doc.getPathOnClient() == null ) {
                    content.setField( "PathOnClient", "ServiceReport.pdf" );
                } else {
                    content.setField( "PathOnClient", doc.getPathOnClient() );
                }
                if ( doc.getTitle() == null ) {
                    content.setField( "Title", "Service Report" );
                } else {
                    content.setField( "Title", doc.getTitle() );
                }
                if ( doc.getDescription() == null ) {
                    content.setField( "Description", "Service Report" );
                } else {
                    content.setField( "Description", doc.getDescription() );
                }
            
                //content.setField("VersionData", Base64.getEncoder().encodeToString("this is text message".getBytes()));
                content.setField( "VersionData", mergedPDFOutputStream.toByteArray() );

                // Upload the ContentVerion
                {
                    SaveResult[] srs = conn.create( new SObject[] { content } );
                    if ( srs[0].isSuccess() ) {
                        System.out.println( "Merged ContentVersion created : " + srs[0].getId() );
                        createdContentVersionIds.add( srs[0].getId() );
                    } else {
                        System.out.println( "Merged ContentVersion failed --------->" );
                        com.sforce.soap.partner.Error[] errors = srs[0].getErrors();
                        for ( int i = 0; i < errors.length; i++) {
                            System.out.println( errors[i].getMessage() );
                        }
                        System.out.println("<---------" );
                        result.setError( "FAILED_BIG_WAY" );
                        return new ResponseEntity<Result>( result, HttpStatus.BAD_REQUEST );
                    }
                }
            }
            result.setContentVersionIds( createdContentVersionIds.toArray( new String[0] ) );
            return new ResponseEntity<Result>( result, HttpStatus.OK );

        } catch ( Exception e ) {
            e.printStackTrace();
            result.setError( e.toString() );
            return new ResponseEntity<Result>( result, HttpStatus.OK );
        }
    }

    @RequestMapping(value = "/merge")
    public ResponseEntity<Object> getCall() {
        return new ResponseEntity<>( "All Good", HttpStatus.OK );
    }

}
