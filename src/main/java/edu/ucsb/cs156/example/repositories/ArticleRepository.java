package main.java.edu.ucsb.cs156.example.repositories;

import main.java.edu.ucsb.cs156.example.entities.Article;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends CrudRepository<Article, Long> {
    
}
