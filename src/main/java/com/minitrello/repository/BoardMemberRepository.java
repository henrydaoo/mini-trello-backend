package com.minitrello.repository;

import com.minitrello.entity.BoardMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

  boolean existsByBoardIdAndUserId(Long boardId, Long userId);

  Optional<BoardMember> findByBoardIdAndUserId(Long boardId, Long userId);

  void deleteByBoardIdAndUserId(Long boardId, Long userId);
}
