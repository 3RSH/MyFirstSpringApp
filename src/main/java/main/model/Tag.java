package main.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tags")
public class Tag {

  @Id
  @SequenceGenerator(name = "tagIdSeq", sequenceName = "tag_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tagIdSeq")
  @Getter
  @Setter
  private int id;

  @Column(nullable = false)
  @Getter
  @Setter
  private String name;


  @ManyToMany
  @JoinTable(name = "tag2post"
      , joinColumns = @JoinColumn(name = "tag_id")
      , inverseJoinColumns = @JoinColumn(name = "post_id"))
  @Getter
  @Setter
  private Set<TagBinding> tagBindings = new HashSet<>();
}