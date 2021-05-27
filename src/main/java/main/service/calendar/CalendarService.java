package main.service.calendar;

import main.api.response.CalendarResponse;

public interface CalendarService {

  CalendarResponse getCalendar(int year);
}