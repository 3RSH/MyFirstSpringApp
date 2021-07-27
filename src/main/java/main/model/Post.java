package main.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "posts")
public class Post {

  @Id
  @SequenceGenerator(name = "postsIdSeq", sequenceName = "posts_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "postsIdSeq")
  @Getter
  @Setter
  private int id;

  @Column(name = "is_active", nullable = false)
  @Getter
  @Setter
  private short isActive;

  @Column(name = "moderation_status", nullable = false)
  @Enumerated(EnumType.STRING)
  @Getter
  @Setter
  private ModerationStatusType moderationStatus;

  @OneToOne
  @JoinColumn(name = "moderator_id")
  @Getter
  @Setter
  private User moderator;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  @Getter
  @Setter
  private User user;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
  @Getter
  @Setter
  private Timestamp time;

  @Column(nullable = false)
  @Getter
  @Setter
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Getter
  @Setter
  private String text;

  @Column(name = "view_count", nullable = false)
  @Getter
  @Setter
  private int viewCount;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
  @Getter
  @Setter
  private List<PostComment> comments = new ArrayList<>();

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "tag2post"
      , joinColumns = @JoinColumn(name = "post_id")
      , inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @Getter
  @Setter
  private Set<Tag> tags = new HashSet<>();


  public enum ModerationStatusType {
    NEW,
    ACCEPTED,
    DECLINED
  }
}