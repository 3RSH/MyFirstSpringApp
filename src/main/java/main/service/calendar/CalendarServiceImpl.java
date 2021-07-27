package main.service.calendar;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import main.api.response.CalendarResponse;
import main.repository.posts.PostsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CalendarServiceImpl implements CalendarService {

  private static final int CURRENT_YEAR_MARKER = 0;
  private static final int INCREMENT_VALUE = 1;
  private static final String DATE_FORMAT = "yyyy-MM-dd";

  private final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
  private final PostsRepository postsRepository;


  public CalendarServiceImpl(@Qualifier("PostsRepository") PostsRepository postsRepository) {
    this.postsRepository = postsRepository;
  }


  @Override
  public CalendarResponse getCalendar(int year) {
    if (year == CURRENT_YEAR_MARKER) {
      year = LocalDate.now().getYear();
    }

    List<Timestamp> times = postsRepository.getPublishTimes();
    List<Integer> years = new ArrayList<>();
    Map<String, Integer> postDateCount = new TreeMap<>();

    for (Timestamp time : times) {
      int postYear = time.toLocalDateTime().getYear();

      if (!years.contains(postYear)) {
        years.add(postYear);
      }

      if (postYear == year) {
        String postDate = format.format(time.getTime());

        if (postDateCount.containsKey(postDate)) {
          postDateCount.replace(postDate, postDateCount.get(postDate) + INCREMENT_VALUE);
        } else {
          postDateCount.put(postDate, INCREMENT_VALUE);
        }
      }
    }

    CalendarResponse calendarResponse = new CalendarResponse();

    calendarResponse.setYears(years);
    calendarResponse.setPosts(postDateCount);

    return calendarResponse;
  }
}