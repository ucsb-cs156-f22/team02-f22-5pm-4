import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "recommendation")
public class Recommendation {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private String requesterEmail;
  private String professorEmail;
  private String explination;
  LocalDateTime dateRequested;
  LocalDateTime dateNeeded;
  boolean done;   
}