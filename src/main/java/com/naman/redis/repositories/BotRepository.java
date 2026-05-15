package com.naman.redis.repositories;

import com.naman.redis.entities.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
}
