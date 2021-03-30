package main.model;

import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "posts")
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Getter
  @Setter
  private int id;

  @Column(name = "is_active")
  @Getter
  @Setter
  private int isActive;

  @Column(name = "moderation_status")
  @Getter
  @Setter
  private ModerationStatusType moderationStatus = ModerationStatusType.NEW;

  @Column(name = "moderator_id")
  @Getter
  @Setter
  private int moderatorId;

  @Column(name = "user_id")
  @Getter
  @Setter
  private int userId;

  @Getter
  @Setter
  private Date time;

  @Getter
  @Setter
  private String title;

  @Getter
  @Setter
  private String text;

  @Column(name = "view_count")
  @Getter
  @Setter
  private int viewCount;

  public enum ModerationStatusType {
    NEW,
    ACCEPTED,
    DECLINED
  }
}
