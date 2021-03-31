package main.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
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

  @OneToMany(fetch = FetchType.LAZY)
  @Column(name = "post_id", nullable = false)
  @JoinColumn(name = "id")
  @Getter
  @Setter
  private Post post;

  @OneToMany
  @Column(name = "tag_id", nullable = false)
  @JoinColumn(name = "id")
  @Getter
  @Setter
  private Tag tag;
}
