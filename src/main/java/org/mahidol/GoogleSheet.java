package org.mahidol;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleSheet {
    private static final String APPLICATION_NAME = "Mahidol timetable";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens\\sheetTokens";
    private static Sheets service;
    final String spreadsheetId = "1AmIwwQSCq0OhHggYEg7df0CBp2k8l70tBQEKczasvKA";
    ConnectDB connectDB = new ConnectDB();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "googleCredentials.json";

    public GoogleSheet() throws Exception {
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleSheet.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
          flow, new LocalServerReceiver())
          .authorize("user");
        return credential;
    }

    public static Sheets getSheetService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    public ArrayList<String> readUserInfo() throws Exception {
        ArrayList<String> password = new ArrayList<>();
        Sheets service = getSheetService();
        // Build a new authorized API client service.
        final String range = "userInfo!A1:B";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List<Object> row : values) {
             password.add(row.get(0).toString());
            }
            System.out.println("update successful");
        }
        return password;
    }
    public void readGoogleSheet() throws Exception {
        Sheets service = getSheetService();
        // Build a new authorized API client service.
        final String range = "subject_data!A2:I";

        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            connectDB.deleteAll("subject_data");
            for (List<Object> row : values) {
                if(row.size()==8){
                    row.add("");
                }
                SubjectData sd = new SubjectData(
                        row.get(1).toString(),
                        row.get(2).toString(),
                        Integer.parseInt(row.get(3).toString()),
                        Integer.parseInt(row.get(4).toString()),
                        Integer.parseInt(row.get(5).toString()),
                        row.get(6).toString(),
                        Integer.parseInt(row.get(7).toString()),
                        row.get(8).toString()
                );
                connectDB.insertDB(sd,"subject_data",row.get(0).toString());
            }
            System.out.println("update successful");
        }
    }
    public void writeGoogleSheet() throws Exception {
        Sheets service = getSheetService();
        clearGooglesSheet();
        ResultSet rs = connectDB.readDB("subject_data","");
        List<List<Object>> values = new java.util.ArrayList<>(Collections.emptyList());
        while (rs.next()){
            values.add(Arrays.asList(
                    rs.getString("parameter"),
                    rs.getString("code"),
                    rs.getString("course"),
                    rs.getInt("credits"),
                    rs.getInt("startTime"),
                    rs.getInt("finishTime"),
                    rs.getString("room"),
                    rs.getInt("day"),
                    rs.getString("details")
            ));
        }
        ConnectDB.conn.close();
        ValueRange body = new ValueRange()
                .setValues(values);
        AppendValuesResponse appendResult  = service.spreadsheets().values()
                .append(spreadsheetId,"subject_data",body)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();
        System.out.println(appendResult);
    }
//    public void deleteGoogleSheet() throws  IOException {
//
//        DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
//                .setRange(
//                        new DimensionRange()
//                        .setSheetId(148672829)
//                        .setDimension("ROWS")
//                        .setStartIndex(2)
//                );
//        List<Request> requests = new ArrayList<>();
//        requests.add(new Request().setDeleteDimension(deleteRequest));
//        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
//        service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
//
//    }
    public void clearGooglesSheet() throws IOException, GeneralSecurityException {
        Sheets service = getSheetService();
        String range = "subject_data!A2:I";

        ClearValuesRequest requestBody = new ClearValuesRequest();

        Sheets.Spreadsheets.Values.Clear request =
                service.spreadsheets().values().clear(spreadsheetId, range, requestBody);

        ClearValuesResponse response = request.execute();

        System.out.println(response);

    }
}
