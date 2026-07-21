package com.minitrello.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.minitrello.entity.Board;
import com.minitrello.entity.User;
import com.minitrello.exception.BoardNotFoundException;
import com.minitrello.exception.ForbiddenOperationException;
import com.minitrello.exception.UserNotFoundException;
import com.minitrello.repository.BoardMemberRepository;
import com.minitrello.repository.BoardRepository;
import com.minitrello.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardAccessServiceTest {

  @Mock private BoardRepository boardRepository;
  @Mock private BoardMemberRepository boardMemberRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private BoardAccessService boardAccessService;

  private User owner;
  private User member;
  private User stranger;
  private Board board;

  @BeforeEach
  void setUp() {
    owner = User.builder().id(1L).username("owner").email("o@test.com").passwordHash("h").build();
    member = User.builder().id(2L).username("member").email("m@test.com").passwordHash("h").build();
    stranger =
        User.builder().id(3L).username("stranger").email("s@test.com").passwordHash("h").build();
    board = Board.builder().id(10L).name("Board A").owner(owner).build();
  }

  @Test
  void getUserByUsername_notFound_throwsUserNotFoundException() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> boardAccessService.getUserByUsername("ghost"));
  }

  @Test
  void getBoardOrThrow_notFound_throwsBoardNotFoundException() {
    when(boardRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(BoardNotFoundException.class, () -> boardAccessService.getBoardOrThrow(999L));
  }

  @Test
  void isOwner_trueForOwner_falseForOthers() {
    assertThat(boardAccessService.isOwner(board, owner)).isTrue();
    assertThat(boardAccessService.isOwner(board, stranger)).isFalse();
  }

  @Test
  void isMember_delegatesToRepository() {
    when(boardMemberRepository.existsByBoardIdAndUserId(10L, 2L)).thenReturn(true);
    when(boardMemberRepository.existsByBoardIdAndUserId(10L, 3L)).thenReturn(false);

    assertThat(boardAccessService.isMember(board, member)).isTrue();
    assertThat(boardAccessService.isMember(board, stranger)).isFalse();
  }

  @Test
  void assertHasAccess_ownerAllowed_doesNotThrow() {
    boardAccessService.assertHasAccess(board, owner);
  }

  @Test
  void assertHasAccess_memberAllowed_doesNotThrow() {
    when(boardMemberRepository.existsByBoardIdAndUserId(10L, 2L)).thenReturn(true);

    boardAccessService.assertHasAccess(board, member);
  }

  @Test
  void assertHasAccess_strangerDenied_throwsForbidden() {
    when(boardMemberRepository.existsByBoardIdAndUserId(10L, 3L)).thenReturn(false);

    assertThrows(
        ForbiddenOperationException.class,
        () -> boardAccessService.assertHasAccess(board, stranger));
  }

  @Test
  void assertIsOwner_nonOwner_throwsForbidden() {
    assertThrows(
        ForbiddenOperationException.class, () -> boardAccessService.assertIsOwner(board, member));
  }

  @Test
  void assertIsOwner_owner_doesNotThrow() {
    boardAccessService.assertIsOwner(board, owner);
  }
}
