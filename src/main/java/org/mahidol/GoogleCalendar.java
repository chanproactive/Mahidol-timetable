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
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class GoogleCalendar {
    ConnectDB connectDB = new ConnectDB();
    private Calendar service;
    private static final String APPLICATION_NAME = "Mahidol timetable";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens\\calendarTokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "googleCredentials.json";

    public GoogleCalendar() throws Exception {
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendar.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void getConnection() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    public void insertCalendar(SubjectData subjectData, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) throws Exception {
        Event event = new Event()
                .setSummary(subjectData.getCode())
                .setLocation(subjectData.getRoom())
                .setDescription(subjectData.getCourse());
        String timeZone = String.valueOf(TimeZone.getDefault().toZoneId());
        ZoneOffset offset = LocalDateTime.now().atZone(ZoneId.of(timeZone)).getOffset();
        System.out.println(timeZone);
        DateTime start = DateTime.parseRfc3339(DateTimeFormatter.ISO_DATE_TIME.format(
                LocalDateTime.of(startDate.getYear(),startDate.getMonth(),startDate.getDayOfMonth(),startTime.getHour(),startTime.getMinute()))+offset);
        System.out.println(startTime.getHour());
        System.out.println(startTime.getMinute());
        DateTime end = DateTime.parseRfc3339(DateTimeFormatter.ISO_DATE_TIME.format(
                LocalDateTime.of(startDate.getYear(),startDate.getMonth(),startDate.getDayOfMonth(),endTime.getHour(),endTime.getMinute()))+"+07:00");
        event.setStart(new EventDateTime().setDateTime(start).setTimeZone(timeZone));
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone(timeZone));
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyyMMdd");
        String[] recurrence = new String[] {"RRULE:FREQ=WEEKLY;UNTIL="+dt.format(endDate)+"T000000Z"};
        System.out.println("RRULE:FREQ=WEEKLY;UNTIL="+dt.format(endDate)+"T000000Z");
        event.setRecurrence(Arrays.asList(recurrence));

        String calendarId = "primary";
        event = service.events().insert(calendarId, event).execute();
        connectDB.saveUUID(event.getId());
        System.out.printf("Event created: %s\n", event.getHtmlLink());
    }
    public void deleteCalendar() throws Exception {
        ArrayList<String> UUIDSet = connectDB.deleteUUID();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        for (String UUID: UUIDSet){
            service.events().delete("primary", UUID).execute();
            System.out.println("deleted "+UUID);
        }
    }
}
