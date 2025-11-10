package com.javaprgraming.javaproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.javaprgraming.javaproject.table.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
}