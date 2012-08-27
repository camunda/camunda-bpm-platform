package com.camunda.fox.cycle.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.camunda.fox.cycle.entity.Roundtrip;

public interface RoundtripRepository extends JpaRepository<Roundtrip, String> {

}
