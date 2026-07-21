package com.minitrello.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.minitrello.dto.AddMemberRequest;
import com.minitrello.dto.BoardRequest;
import com.minitrello.dto.BoardResponse;
import com.minitrello.entity.Board;
import com.minitrello.entity.BoardMember;
import com.minitrello.entity.User;
import com.minitrello.exception.BoardMemberNotFoundException;
import com.minitrello.exception.ForbiddenOperationException;
import com.minitrello.exception.MemberAlreadyExistsException;
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
public class BoardServiceTest {
  @Mock private BoardRepository boardRepository;
  @Mock private BoardMemberRepository boardMemberRepository;
  @Mock private UserRepository userRepository;
  @Mock private BoardAccessService boardAccessService;

  @InjectMocks private BoardService boardService;

  private User owner;
  private User newMember;
  private Board board;

  @BeforeEach
  void setUp() {
    owner =
        User.builder().id(1L).username("owner").email("owner@test.com").passwordHash("h").build();
    newMember =
        User.builder().id(2L).username("jane").email("jane@example.com").passwordHash("h").build();
    board = Board.builder().id(10L).name("Board A").description("desc").owner(owner).build();
  }

  @Test
  void createBoard_savesWithCurrentUserAsOwner() {
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(boardRepository.save(any(Board.class)))
        .thenAnswer(
            inv -> {
              Board b = inv.getArgument(0);
              b.setId(10L);
              return b;
            });

    BoardResponse response = boardService.createBoard("owner", new BoardRequest("Board A", "desc"));

    assertThat(response.getName()).isEqualTo("Board A");
    assertThat(response.getOwnerId()).isEqualTo(1L);
  }

  @Test
  void updateBoard_nonOwner_throwsForbidden_andDoesNotSave() {
    when(boardAccessService.getBoardOrThrow(10L)).thenReturn(board);
    when(boardAccessService.getUserByUsername("jane")).thenReturn(newMember);
    doThrow(new ForbiddenOperationException("Only the board owner can perform this action"))
        .when(boardAccessService)
        .assertIsOwner(board, newMember);

    assertThrows(
        ForbiddenOperationException.class,
        () -> boardService.updateBoard("jane", 10L, new BoardRequest("New name", "desc")));

    verify(boardRepository, never()).save(any());
  }

  @Test
  void addMember_emailNotRegistered_throwsUserNotFoundException() {
    when(boardAccessService.getBoardOrThrow(10L)).thenReturn(board);
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> boardService.addMember("owner", 10L, new AddMemberRequest("ghost@example.com")));
  }

  @Test
  void addMember_ownerEmail_throwsMemberAlreadyExists() {
    when(boardAccessService.getBoardOrThrow(10L)).thenReturn(board);
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

    assertThrows(
        MemberAlreadyExistsException.class,
        () -> boardService.addMember("owner", 10L, new AddMemberRequest("owner@test.com")));
  }

  @Test
  void addMember_alreadyMember_throwsMemberAlreadyExists() {
    when(boardAccessService.getBoardOrThrow(10L)).thenReturn(board);
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(newMember));
    when(boardMemberRepository.existsByBoardIdAndUserId(10L, 2L)).thenReturn(true);

    assertThrows(
        MemberAlreadyExistsException.class,
        () -> boardService.addMember("owner", 10L, new AddMemberRequest("jane@example.com")));

    verify(boardMemberRepository, never()).save(any());
  }

  @Test
  void addMember_validNewMember_savesAndReturnsUpdatedBoard() {
    when(boardAccessService.getBoardOrThrow(10L)).thenReturn(board);
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(newMember));
    when(boardMemberRepository.existsByBoardIdAndUserId(10L, 2L)).thenReturn(false);
    when(boardMemberRepository.save(any(BoardMember.class))).thenAnswer(inv -> inv.getArgument(0));

    BoardResponse response =
        boardService.addMember("owner", 10L, new AddMemberRequest("jane@example.com"));

    assertThat(response.getMembers()).hasSize(1);
    assertThat(response.getMembers().get(0).getUserId()).isEqualTo(2L);
    verify(boardMemberRepository, times(1)).save(any(BoardMember.class));
  }

  @Test
  void removeMember_notAMember_throwsBoardMemberNotFoundException() {
    when(boardAccessService.getBoardOrThrow(10L)).thenReturn(board);
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(boardMemberRepository.findByBoardIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

    assertThrows(
        BoardMemberNotFoundException.class, () -> boardService.removeMember("owner", 10L, 99L));

    verify(boardMemberRepository, never()).deleteByBoardIdAndUserId(any(), any());
  }
}
