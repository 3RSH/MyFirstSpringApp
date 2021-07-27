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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post_votes")
public class Vote {

  @Id
  @SequenceGenerator(name = "votesIdSeq", sequenceName = "votes_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "votesIdSeq")
  @Getter
  @Setter
  private int id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  @Getter
  @Setter
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  @Getter
  @Setter
  private Post post;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
  @Getter
  @Setter
  private Timestamp time;

  @Column(nullable = false)
  @Getter
  @Setter
  private short value;
}