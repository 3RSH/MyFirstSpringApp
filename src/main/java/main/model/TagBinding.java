package main.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tag2post")
public class TagBinding {

  @Id
  @SequenceGenerator(name = "tagBindIdSeq", sequenceName = "tag2post_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tagBindIdSeq")
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