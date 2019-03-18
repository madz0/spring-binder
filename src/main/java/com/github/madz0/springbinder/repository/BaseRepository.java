package com.github.madz0.springbinder.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends
        EntityGraphJpaRepository<T, ID>, EntityGraphJpaSpecificationExecutor<T> {

    default T findOne(ID id){
        return findById(id).orElse(null);
    }
}
