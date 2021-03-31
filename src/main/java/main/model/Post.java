package main.model;

import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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

  @Column(name = "is_active", nullable = false)
  @Getter
  @Setter
  private int isActive;

  @Column(name = "moderation_status", nullable = false)
  @Getter
  @Setter
  private ModerationStatusType moderationStatus = ModerationStatusType.NEW;

  @OneToOne
  @Column(name = "moderator_id")
  @JoinColumn(name = "id")
  @Getter
  @Setter
  private User moderator;

  @ManyToOne
  @Column(name = "user_id", nullable = false)
  @JoinColumn(name = "id")
  @Getter
  @Setter
  private User user;

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