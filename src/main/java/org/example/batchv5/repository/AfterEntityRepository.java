package org.example.batchv5.repository;

import org.example.batchv5.entity.AfterEntity;
import org.example.batchv5.entity.BeforeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfterEntityRepository extends JpaRepository<AfterEntity,Long> {
}
