package com.minitrello.service;

import com.minitrello.dto.AddMemberRequest;
import com.minitrello.dto.BoardMemberResponse;
import com.minitrello.dto.BoardRequest;
import com.minitrello.dto.BoardResponse;
import com.minitrello.entity.Board;
import com.minitrello.entity.BoardMember;
import com.minitrello.entity.User;
import com.minitrello.exception.*;
import com.minitrello.repository.BoardMemberRepository;
import com.minitrello.repository.BoardRepository;
import com.minitrello.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardsForCurrentUser(String username) {
        User currentUser = getUserByUsername(username);
        return boardRepository.findAllAccessibleByUser(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BoardResponse createBoard(String username, BoardRequest request) {
        User owner = getUserByUsername(username);

        Board board = Board.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        return toResponse(boardRepository.save(board));
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoardById(String username, Long boardId) {
        Board board = getBoardOrThrow(boardId);
        User currentUser = getUserByUsername(username);
        assertHasAccess(board, currentUser);
        return toResponse(board);
    }

    public BoardResponse updateBoard(String username, Long boardId, BoardRequest request) {
        Board board = getBoardOrThrow(boardId);
        User currentUser = getUserByUsername(username);
        assertIsOwner(board, currentUser);

        board.setName(request.getName());
        board.setDescription(request.getDescription());

        return toResponse(boardRepository.save(board));
    }

    public void deleteBoard(String username, Long boardId) {
        Board board = getBoardOrThrow(boardId);
        User currentUser = getUserByUsername(username);
        assertIsOwner(board, currentUser);
        boardRepository.delete(board);
    }

    public BoardResponse addMember(String username, Long boardId, AddMemberRequest request) {
        Board board = getBoardOrThrow(boardId);
        User currentUser = getUserByUsername(username);
        assertIsOwner(board, currentUser);

        User newMember = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("No user found with email " + request.getEmail()));

        if (board.getOwner().getId().equals(newMember.getId())) {
            throw new MemberAlreadyExistsException("This user already owns the board");
        }
        if (boardMemberRepository.existsByBoardIdAndUserId(boardId, newMember.getId())) {
            throw new MemberAlreadyExistsException("This user is already a member of the board");
        }

        BoardMember member = BoardMember.builder()
                .board(board)
                .user(newMember)
                .build();
        boardMemberRepository.save(member);
        board.getMembers().add(member);

        return toResponse(board);
    }

    public void removeMember(String username, Long boardId, Long userId) {
        Board board = getBoardOrThrow(boardId);
        User currentUser = getUserByUsername(username);
        assertIsOwner(board, currentUser);

        boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new BoardMemberNotFoundException(
                        "User " + userId + " is not a member of board " + boardId));

        boardMemberRepository.deleteByBoardIdAndUserId(boardId, userId);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }


    private BoardResponse toResponse(Board board) {
        List<BoardMemberResponse> members = board.getMembers().stream()
                .map(m -> BoardMemberResponse.builder()
                        .userId(m.getUser().getId())
                        .username(m.getUser().getUsername())
                        .email(m.getUser().getEmail())
                        .build())
                .toList();

        return BoardResponse.builder()
                .id(board.getId())
                .name(board.getName())
                .description(board.getDescription())
                .ownerId(board.getOwner().getId())
                .ownerUsername(board.getOwner().getUsername())
                .members(members)
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    private Board getBoardOrThrow(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found with id " + boardId));
    }

    private void assertHasAccess(Board board, User user) {
        boolean isOwner = board.getOwner().getId().equals(user.getId());
        boolean isMember = boardMemberRepository.existsByBoardIdAndUserId(board.getId(), user.getId());
        if (!isOwner && !isMember) {
            throw new ForbiddenOperationException("You do not have access to this board");
        }
    }

    private void assertIsOwner(Board board, User user) {
        if (!board.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Only the board owner can perform this action");
        }
    }
}
