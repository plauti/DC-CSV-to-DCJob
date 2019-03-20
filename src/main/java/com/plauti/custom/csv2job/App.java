package com.plauti.custom.csv2job;


import com.opencsv.CSVReader;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;

@SpringBootApplication
public class App implements CommandLineRunner {

    @Value("${username}")
    private String sfUsername;

    @Value("${password}")
    private String sfPassword;

    @Value("${token}")
    private String sfToken;

    @Value("${endpoint}")
    private String sfEndpoint;

    @Value("${namespace}")
    private String sfNamepace;

    @Value("${filename}")
    private String fileName;

    @Value("${sourceobject}")
    private String sourceObject;

    @Value("${matchobject}")
    private String matchObject;

    @Autowired
    private ResourceLoader resourceLoader;

    public static void main(String[] args) throws Exception {
        
        try {
            SpringApplication app = new SpringApplication(App.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        
    }

    @Override
    public void run(String... args) throws Exception {

        ConnectorConfig sfConfig = new ConnectorConfig();
        sfConfig.setUsername(sfUsername);
        sfConfig.setPassword(sfPassword + sfToken);
        sfConfig.setAuthEndpoint(sfEndpoint + "/services/Soap/u/40.0");


        Map<String, Group> groupMap = new HashMap<String, Group>();
        String dcJobId = "A";

        System.out.println(resourceLoader.getResource("classpath:" + fileName).getFile());

        CSVReader reader = new CSVReader(new FileReader(resourceLoader.getResource("classpath:" + fileName).getFile()), ',');
        String [] nextLine;


        while ((nextLine = reader.readNext()) != null) {

            if (StringUtils.isEmpty(nextLine[0])) {
                continue;
            }


            String sourceId = nextLine[0];
            String matchId = nextLine[1];

            if (!groupMap.containsKey(sourceId)) {
                groupMap.put(sourceId, new Group(sourceId));
            }
            groupMap.get(sourceId).getMatchedRecords().add(matchId);

        }

        PartnerConnection connection;
        try {
            connection = Connector.newConnection(sfConfig);
        } catch (ConnectionException e) {
            System.out.println(e);
            System.out.println("Connection Error");
            exit(1);
            return;
        }

        System.out.println("GROUPS : " + groupMap.size());

        if (groupMap.size() > 0) {

            SObject sfJob = new SObject();
            sfJob.setType(sfNamepace + "dcJob__c");
            sfJob.setField(sfNamepace + "name__c", "csv2job : " + fileName);
            sfJob.setField(sfNamepace + "type__c", "search");
            sfJob.setField(sfNamepace + "sourceobject__c", sourceObject);
            sfJob.setField(sfNamepace + "status__c", "Completed");

            if (!sourceObject.equals(matchObject)) {
                sfJob.setField(sfNamepace + "matchobject__c", matchObject);
            }

            SaveResult[] saveResults = connection.create(new ArrayList<SObject>(Arrays.asList(sfJob)).stream().toArray(SObject[]::new));
            if (!saveResults[0].isSuccess()) {
                System.out.println("Could not create job" + saveResults[0].getErrors()[0].getMessage());
                exit(1);
                return;
            }
            dcJobId = saveResults[0].getId();

        }

        Integer groupCount = 0;

        List<SObject> groupList = new ArrayList<SObject>();

        for (Group g : groupMap.values()) {
            System.out.println(g.toString());

            SObject sfGroup = new SObject();
            sfGroup.setType(sfNamepace + "dcGroup__c");
            sfGroup.setField(sfNamepace + "dcJob__c", dcJobId);
            sfGroup.setField(sfNamepace + "group__c", g.getGroupNumber());
            sfGroup.setField(sfNamepace + "MasterRecord__c", g.getMasterId());

            groupList.add(sfGroup);

            if (g.getMatchedRecords().size() > groupCount) {
                groupCount = g.getMatchedRecords().size();
            }

        }

        System.out.println("MAX GROUP COUNT : " + groupCount);
        System.out.println("GROUP SIZE : " + groupMap.size());
        System.out.println("GROUP INSERTS : " + groupMap.size() / 200);

        

        int groupCounter = 0;
        for (List<SObject>  batch : getBatches(groupList, 200)) {
            System.out.println(groupCounter++ + " - Adding Groups : " + batch.size());
            SaveResult[] saveResults = connection.create(batch.stream().toArray(SObject[]::new));
            int a = 0;
            for (SObject sfGroupData : batch) {
                if (saveResults[a].isSuccess()) {
                    String masterId = (String) sfGroupData.getField(sfNamepace + "MasterRecord__c");
                    groupMap.get(masterId).setGroupId(saveResults[a].getId());
                }
                a++;
            }
        }

        List<SObject> duplicateList = new ArrayList<SObject>();
        for (Group g : groupMap.values()) {

            for (String matchId : g.getMatchedRecords()) {
                SObject sfDup = new SObject();
                sfDup.setType(sfNamepace + "dc3Duplicate__c");
                sfDup.setField(sfNamepace + "dcJob__c", dcJobId);
                sfDup.setField(sfNamepace + "dcGroup__c", g.getGroupId());
                sfDup.setField(sfNamepace + "MatchObject__c", matchId);
                sfDup.setField(sfNamepace + "SourceObject__c", g.getMasterId());
                sfDup.setField(sfNamepace + "Score__c", 100);
                duplicateList.add(sfDup);
            }
        }

        System.out.println("DUP SIZE : " + duplicateList.size());
        System.out.println("DUP INSERTS : " + duplicateList.size() / 200);
        int dupCounter = 0;
        for (List<SObject>  batch : getBatches(duplicateList, 200)) {
            System.out.println(dupCounter++ + " - Adding Duplicates : " + batch.size());
            SaveResult[] saveResults = connection.create(batch.stream().toArray(SObject[]::new));

            int ok = 0;
            int error = 0;

            for (SaveResult sr : saveResults) {
                if (sr.isSuccess()) {
                    ok++;
                } else {
                    error++;
                }
            }

            System.out.println("------ Inserted : " + ok + "; Error : " + error);
            System.out.println("Pausing");
            Thread.sleep(5000);
        }
        System.out.println("Ready");
    }

    public static <T> List<List<T>> getBatches(List<T> collection,int batchSize){
        int i = 0;
        List<List<T>> batches = new ArrayList<List<T>>();
        while(i<collection.size()){
            int nextInc = Math.min(collection.size()-i,batchSize);
            List<T> batch = collection.subList(i,i+nextInc);
            batches.add(batch);
            i = i + nextInc;
        }

        return batches;
    }

}
