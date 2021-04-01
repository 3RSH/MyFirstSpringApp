package main.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tag2post")
public class TagBinding {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Getter
  @Setter
  private int id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  @Getter
  @Setter
  private Post post;

  @ManyToOne
  @JoinColumn(name = "tag_id", nullable = false)
  @Getter
  @Setter
  private Tag tag;
}