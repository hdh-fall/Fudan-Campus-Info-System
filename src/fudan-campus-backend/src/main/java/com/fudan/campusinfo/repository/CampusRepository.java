package com.fudan.campusinfo.repository;

import com.fudan.campusinfo.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Integer> {
}
