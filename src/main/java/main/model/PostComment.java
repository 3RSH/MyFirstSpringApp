package main.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "post_comments")
public class PostComment {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Getter
  @Setter
  private int id;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  @Getter
  @Setter
  private PostComment parentPost;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  @Getter
  @Setter
  private Post post;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  @Getter
  @Setter
  private User user;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
  @Getter
  @Setter
  private Timestamp time;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Getter
  @Setter
  private String text;
}