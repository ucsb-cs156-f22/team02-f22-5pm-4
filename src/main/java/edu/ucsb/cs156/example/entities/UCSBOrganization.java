package edu.ucsb.cs156.example.entities;
import java.lang.annotation.Inherited;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    String orgCode;
    String orgTranslationShort;
    String orgTranslation;
    boolean inactive;
}
