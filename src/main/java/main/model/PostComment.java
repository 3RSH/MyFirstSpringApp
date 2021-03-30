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
@Table(name = "post_comments")
public class PostComment {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Getter
  @Setter
  private int id;

  @Column(name = "parent_id")
  @Getter
  @Setter
  private int parentId;

  @Column(name = "post_id")
  @Getter
  @Setter
  private int postId;

  @Column(name = "user_id")
  @Getter
  @Setter
  private int userId;

  @Getter
  @Setter
  private Date time;

  @Getter
  @Setter
  private String text;
}
