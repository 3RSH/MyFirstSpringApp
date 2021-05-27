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

  private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
  private final PostsRepository postsRepository;


  public CalendarServiceImpl(@Qualifier("PostsRepository") PostsRepository postsRepository) {
    this.postsRepository = postsRepository;
  }


  @Override
  public CalendarResponse getCalendar(int year) {
    if (year == 0) {
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
          postDateCount.replace(postDate, postDateCount.get(postDate) + 1);
        } else {
          postDateCount.put(postDate, 1);
        }
      }
    }

    CalendarResponse calendarResponse = new CalendarResponse();

    calendarResponse.setYears(years);
    calendarResponse.setPosts(postDateCount);

    return calendarResponse;
  }
}