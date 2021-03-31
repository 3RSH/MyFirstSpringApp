package main.model;

import java.sql.Date;
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
  @Column(name = "parent_id")
  @JoinColumn(name = "id")
  @Getter
  @Setter
  private PostComment parentPost;

  @ManyToOne(fetch = FetchType.LAZY)
  @Column(name = "post_id", nullable = false)
  @JoinColumn(name = "id")
  @Getter
  @Setter
  private Post post;

  @OneToOne
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
  private String text;
}
