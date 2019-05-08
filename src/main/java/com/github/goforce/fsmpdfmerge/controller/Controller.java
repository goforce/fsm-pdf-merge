package com.github.goforce.fsmpdfmerge.controller;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

                String mergedContentVersionId;

                // Upload the ContentVersion
                {
                    SaveResult[] srs = conn.create( new SObject[] { content } );
                    if ( srs[0].isSuccess() ) {
                        mergedContentVersionId = srs[0].getId();
                        System.out.println( "Merged ContentVersion created : " + mergedContentVersionId );
                        createdContentVersionIds.add( mergedContentVersionId );
                    } else {
                        String err = formatError( srs[0].getErrors() );
                        System.out.println( "Merged ContentVersion failed --------->\n" + err + "<---------" );
                        result.setErrorMessage( err );
                        return new ResponseEntity<Result>( result, HttpStatus.BAD_REQUEST );
                    }
                }

                // add link to target objects
                if ( doc.getTargetObjectIds().length > 0 ) {
                    SObject[] cvm = conn.retrieve( "Id,ContentDocumentId", "ContentVersion", new String[]{ mergedContentVersionId } );
                    String cdmId = (String) cvm[0].getField( "ContentDocumentId" );
                    SObject[] cdls = new SObject[doc.getTargetObjectIds().length];
                    for ( int j = 0; j < doc.getTargetObjectIds().length; j++ ) {
                        SObject cdl = new SObject( "ContentDocumentLink" );
                        cdl.setField( "ContentDocumentId", cdmId );
                        cdl.setField( "LinkedEntityId", doc.getTargetObjectIds()[j] );
                        cdl.setField( "ShareType", "V" );
                        cdl.setField( "Visibility", "AllUsers" );
                        cdls[j] = cdl;
                    }
                    {
                        SaveResult[] srs = conn.create( cdls );
                        for ( int j = 0; j < srs.length; j++ ) {
                            if ( srs[j].isSuccess() ) {
                                System.out.println( "ContentDocumentLink created : " + srs[j].getId() );
                            } else {
                                String err = formatError( srs[0].getErrors() );
                                System.out.println( "Insert ContentDocumentLink failed --------->\n" + err + "<---------" );
                                result.setErrorMessage( err );
                                return new ResponseEntity<Result>( result, HttpStatus.BAD_REQUEST );
                            }
                        }
                    }
                }

            }
            result.setContentVersionIds( createdContentVersionIds.toArray( new String[0] ) );
            return new ResponseEntity<Result>( result, HttpStatus.OK );

        } catch ( Exception e ) {
            e.printStackTrace();
            result.setErrorMessage( e.toString() );
            return new ResponseEntity<Result>( result, HttpStatus.OK );
        }
    }

    @RequestMapping(value = "/merge")
    public ResponseEntity<Object> getCall() {
        return new ResponseEntity<>( "All Good", HttpStatus.OK );
    }

    private String formatError( com.sforce.soap.partner.Error[] errors ) {
        String s = "";
        for ( int k = 0; k < errors.length; k++) {
            s += errors[k].getMessage() + "\n";
        }
        return s;
    }

}
