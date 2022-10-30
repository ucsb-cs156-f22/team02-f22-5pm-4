package edu.ucsb.cs156.example.entities;
<<<<<<< HEAD:src/main/java/edu/ucsb/cs156/example/entities/Article.java

=======
import java.lang.annotation.Inherited;
>>>>>>> c2bc7eee7405d6277d1b3282a8fe3fcab174feff:src/main/java/edu/ucsb/cs156/example/entities/UCSBOrganization.java
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "ucsborganization")
public class UCSBOrganization {
    @Id
<<<<<<< HEAD:src/main/java/edu/ucsb/cs156/example/entities/Article.java
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
=======
    private String orgCode;
>>>>>>> c2bc7eee7405d6277d1b3282a8fe3fcab174feff:src/main/java/edu/ucsb/cs156/example/entities/UCSBOrganization.java

    private String orgTranslationShort;
    private String orgTranslation;
    private boolean inactive;
}
