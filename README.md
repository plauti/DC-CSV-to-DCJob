# DC-CSV-to-DCJob
A java command line application to convert csv files into a working DC Job

## Config
Change the values in the application properties located in the resource folder.

username << The Salesforce username
password << The Salesforce password
token << The Salesforce API Token if needed
endpoint << The Salesforce instance url. 
filename << The CSV File with the actial duplicate data
sourceobject << The object type of the master records. This is in Key form. So 003 for Contact, 001 for Account, 00Q Lead. 
matchobject << The object type of the ToMerge records. This is in Key form. So 003 for Contact, 001 for Account, 00Q Lead.

## Duplicate File

Create an CSV file which contains the master record and the duplicate pairs. See the example below. Store this files in the resources folder and give the file name in the application.properties file

```
Master,ToMerge
0032400000eC3AB,00324000014wsHZ
0032400000eC3AB,00324000014wsHb
0032400000Xo4B6,00324000014xOoy
```

## Run via the command line

execute the following command in the root of the directory
```
mvn spring-boot:run
```

Open the DC Job tab in Salesforce to view the results in Salesforce
