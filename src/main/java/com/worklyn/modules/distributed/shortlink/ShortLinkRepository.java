package com.worklyn.modules.distributed.shortlink;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortLinkRepository extends MongoRepository<ShortLink, String> {
    Optional<ShortLink> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
}
